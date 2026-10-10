#!/usr/bin/env python3
"""Launch an isolated offline GUI smoke test; never touches the user's instance."""
import argparse
import json
import os
from pathlib import Path
import shutil
import subprocess
import zipfile
from build import VERSIONS
from named_port import symbols, translate
ROOT=Path(__file__).resolve().parent.parent
parser=argparse.ArgumentParser()
parser.add_argument('--assets',required=True,type=Path)
parser.add_argument('--atlas',type=Path)
parser.add_argument('--quick',action='store_true',help='Shorter per-stage dwell time for the version matrix')
parser.add_argument('--bundled',action='store_true')
parser.add_argument('--atlas-dependencies',type=Path)
parser.add_argument('--minecraft',default='1.21.11',choices=VERSIONS)
args=parser.parse_args()
args.assets=args.assets.resolve()
version=args.minecraft
java=Path(os.environ['JAVA_HOME'])/'bin/java';javac=java.with_name('javac')
run=ROOT/'build'/('smoke-'+version+('-atlas' if args.atlas else '-standalone'))
mods=run/'mods';mods.mkdir(parents=True,exist_ok=True)
for previous in run.glob('stage-*.png'): previous.unlink()
classes=ROOT/'build'/('classes-'+version)
cp=(ROOT/'build'/('classpath-'+version+'.txt')).read_text()
helper=ROOT/'build'/('qa-classes-'+version);helper.mkdir(parents=True,exist_ok=True)
source=ROOT/'tests/smoke/SmokeMixin.java'
if version in ('1.20','1.20.1','1.20.2','1.20.3','1.20.4','1.20.5','1.20.6','1.21','1.21.1','1.21.2','1.21.3','1.21.4'):
    source=ROOT/'build'/('qa-source-'+version)/'SmokeMixin.java'
    source.parent.mkdir(parents=True,exist_ok=True)
    # Legacy screenshot API is synchronous and returns the NativeImage.
    text=(ROOT/'tests/smoke/SmokeMixin.java').read_text()
    start=text.index('        class_318.method_1663(')
    text=text[:start]+'''        var image=class_318.method_1663(client.method_1522());
        try { image.method_4314(file); } catch (Exception e) { throw new RuntimeException(e); }
        finally { image.close(); }
    }
}
'''
    source.write_text(text)
if version.startswith('26.'):
    source=ROOT/'build'/('qa-source-'+version)/'SmokeMixin.java'
    source.parent.mkdir(parents=True,exist_ok=True)
    text=translate((ROOT/'tests/smoke/SmokeMixin.java').read_text(),symbols(ROOT/'build/dependencies'))
    if version in ('26.2','26.3'):
        text=text.replace('client.getMainRenderTarget()','client.gameRenderer.mainRenderTarget()')
        text=text.replace('client.getOverlay()', 'client.gui.overlay()').replace('client.screen','client.gui.screen()').replace('client.setScreen(', 'client.gui.setScreen(')
    source.write_text(text)
subprocess.run([str(javac),'-proc:none','--release',str(VERSIONS[version][1]),'-cp',str(classes)+os.pathsep+cp,'-d',str(helper),str(source)],check=True)
with zipfile.ZipFile(mods/'evatex-qa.jar','w') as z:
    z.writestr('fabric.mod.json',json.dumps({'schemaVersion':1,'id':'evatex-qa','version':'1.0','name':'EVATEX QA','environment':'client','mixins':['evatex-qa.mixins.json']}))
    z.writestr('evatex-qa.mixins.json',json.dumps({'required':True,'package':'evatexqa','compatibilityLevel':'JAVA_'+str(VERSIONS[version][1]),'client':['SmokeMixin'],'injectors':{'defaultRequire':1}}))
    for file in helper.rglob('*.class'): z.write(file,file.relative_to(helper))
suffix='' if args.bundled else '-system-ffmpeg'
shutil.copyfile(ROOT/'build/dist'/f'evatex-background-video-1.8.0-mc{version}{suffix}.jar',mods/'evatex.jar')
if args.atlas:
    shutil.copyfile(args.atlas,mods/'atlas-client.jar')
    for pattern in ('fabric-api-*.jar','PlayerAnimationLibFabric-*.jar'):
        file=next(args.atlas_dependencies.glob(pattern));shutil.copyfile(file,mods/file.name)
videos=run/'videos';videos.mkdir(exist_ok=True)
video=videos/'Astral demo.mp4'
# Regenerate a monochrome clip; do not reuse older coloured QA videos.
subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-f','lavfi','-i',"nullsrc=s=960x540:r=30,geq=r='28+25*sin(X/180+T)':g='30+24*sin(Y/240+T/2)':b='80+40*sin((X+Y)/220+T)',format=yuv420p",'-t','3','-vf','hue=s=0','-c:v','libx264','-crf','18',str(video)],check=True)
config=run/'config';config.mkdir(exist_ok=True)
if args.atlas:
    for atlas_config in (config/'atlasclient.json', config/'atlasclient.json.bak'):
        if atlas_config.exists():
            values=json.loads(atlas_config.read_text()); values['vanillaMode']=False
            atlas_config.write_text(json.dumps(values,indent=2))
(config/'evatexbackgroundvideo.json').write_text(json.dumps({'videoPath':str(video),'fps':30,'scale':1.0,'darkening':.15,'showVignette':True}))
(run/'options.txt').write_text('lang:tr_tr\nguiScale:3\nfullscreen:false\nmaxFps:60\nskipMultiplayerWarning:true\n')
game=json.loads((ROOT/'build/dependencies'/f'{version}.json').read_text())
index=game['assetIndex']['id']
# Official client is needed on the runtime classpath. Fabric remaps it in the isolated run folder.
runtime_cp=cp.replace(str(ROOT/'build/dependencies'/f'{version}-intermediary.jar'),str(ROOT/'build/dependencies'/f'{version}-official.jar'))
command=[str(java),'-Xmx2G','-Dfabric.log.disableAnsi=true','-Devatex.qa.stageTicks='+('30' if args.quick else '100'),'-cp',runtime_cp,'net.fabricmc.loader.impl.launch.knot.KnotClient','--username','EvatexQA','--version',version,'--gameDir',str(run),'--assetsDir',str(args.assets),'--assetIndex',index,'--accessToken','0','--uuid','00000000000000000000000000000000','--userType','legacy','--versionType','release','--width','1280','--height','720']
log=run/'launch.log'
print('Launching isolated test:',run,flush=True)
with log.open('w') as output:
    process=subprocess.Popen(command,cwd=run,stdout=output,stderr=subprocess.STDOUT)
    try: code=process.wait(timeout=180)
    except subprocess.TimeoutExpired: process.terminate();process.wait(timeout=20);raise SystemExit('Smoke test timed out; see '+str(log))
print('Game exited:',code,'log:',log)
print('\n'.join(line for line in log.read_text(errors='replace').splitlines() if 'EVATEX_QA' in line or 'Mixin apply' in line or 'Cannot render video' in line or 'bridge disabled' in line))
if code!=0 or 'EVATEX_QA COMPLETE' not in log.read_text(errors='replace'): raise SystemExit(1)
