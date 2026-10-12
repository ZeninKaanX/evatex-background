#!/usr/bin/env python3
"""Launch an isolated offline GUI smoke test; never touches the user's instance."""
import argparse
import json
import os
from pathlib import Path
import shutil
import subprocess
import zipfile
from build import VERSIONS, MOD_VERSION
from named_port import symbols, translate
ROOT=Path(__file__).resolve().parent.parent
parser=argparse.ArgumentParser()
parser.add_argument('--assets',required=True,type=Path)
parser.add_argument('--atlas',type=Path)
parser.add_argument('--video',type=Path,help='Use supplied video as-is, without generating/resizing a fixture')
parser.add_argument('--gui-scale',type=int,default=3)
parser.add_argument('--width',type=int,default=1280)
parser.add_argument('--height',type=int,default=720)
parser.add_argument('--language',default='tr_tr')
parser.add_argument('--username',default='EvatexQA',help='Offline display name for screenshots')
parser.add_argument('--quick',action='store_true',help='Shorter per-stage dwell time for the version matrix')
parser.add_argument('--bundled',action='store_true')
parser.add_argument('--extra-mods',type=Path,help='Additional verified jars in the isolated test only')
parser.add_argument('--sodium',type=Path,help='Optional installed Sodium jar for reported-menu QA')
parser.add_argument('--reported-menus',action='store_true',help='Check packs, credits, rules, video settings and minimized-window playback')
parser.add_argument('--extended-menus',action='store_true',help='Verify wallpaper continuity and independent world/options/meteor switches with muted 10% audio')
parser.add_argument('--hidden-window',action='store_true',help='Render a real hidden GL window when the locked desktop cannot map new windows')
parser.add_argument('--home-menu',action='store_true',help='Toggle optional Astral home through its actual setting, test native actions, resize and disable')
parser.add_argument('--collision-layout',action='store_true',help='Inject competing title controls, hide them, resize, and activate the shortcut')
parser.add_argument('--shortcut-only',action='store_true',help='Check and capture the title shortcut without replaying audio/settings regression scenes')
parser.add_argument('--atlas-dependencies',type=Path)
parser.add_argument('--minecraft',default='1.21.11',choices=VERSIONS)
args=parser.parse_args()
args.assets=args.assets.resolve()
version=args.minecraft
java=Path(os.environ['JAVA_HOME'])/'bin/java';javac=java.with_name('javac')
run=ROOT/'build'/('smoke-'+version+('-atlas' if args.atlas else '-standalone'))
mods=run/'mods';mods.mkdir(parents=True,exist_ok=True)
for pattern in ('stage-*.png','menu-*.png'):
    for previous in run.glob(pattern): previous.unlink()
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
if version not in ('1.21.11','26.1','26.1.1','26.1.2','26.2','26.3'):
    # SplashRenderer accepted plain strings before 1.21.11.
    if source==ROOT/'tests/smoke/SmokeMixin.java':
        source=ROOT/'build'/('qa-source-'+version)/'SmokeMixin.java';source.parent.mkdir(parents=True,exist_ok=True)
        source.write_text((ROOT/'tests/smoke/SmokeMixin.java').read_text())
    source.write_text(source.read_text().replace('new net.minecraft.class_8519(net.minecraft.class_2561.method_43470("C₄H₉N₃O₂ · H₂O"))','new net.minecraft.class_8519("C₄H₉N₃O₂ · H₂O")'))
if version.startswith('26.'):
    source=ROOT/'build'/('qa-source-'+version)/'SmokeMixin.java'
    source.parent.mkdir(parents=True,exist_ok=True)
    text=translate((ROOT/'tests/smoke/SmokeMixin.java').read_text(),symbols(ROOT/'build/dependencies'))
    text=text.replace('client.getWindow().getWindow()', 'client.getWindow().handle()').replace('EditGameRulesScreen','WorldCreationGameRulesScreen')
    if version=='26.3':
        text=text.replace('org.lwjgl.glfw.GLFW.glfwIconifyWindow','org.lwjgl.sdl.SDLVideo.SDL_MinimizeWindow').replace('org.lwjgl.glfw.GLFW.glfwRestoreWindow','org.lwjgl.sdl.SDLVideo.SDL_RestoreWindow')
        text=text.replace('org.lwjgl.glfw.GLFW.glfwGetWindowAttrib(client.getWindow().handle(),org.lwjgl.glfw.GLFW.GLFW_ICONIFIED)', '(org.lwjgl.sdl.SDLVideo.SDL_GetWindowFlags(client.getWindow().handle()) & org.lwjgl.sdl.SDLVideo.SDL_WINDOW_MINIMIZED)')
        start=text.index('        if (!evatexqa$positioned) {')
        end=text.index('        if (client.getOverlay()',start)
        text=text[:start]+'''        if (!evatexqa$positioned) {
            long window=client.getWindow().handle();
            var displays=org.lwjgl.sdl.SDLVideo.SDL_GetDisplays();
            int selected=0,rightmost=Integer.MIN_VALUE,x=0,y=0;
            try(var stack=org.lwjgl.system.MemoryStack.stackPush()) {
                var bounds=org.lwjgl.sdl.SDL_Rect.malloc(stack);
                if(displays!=null)for(int i=0;i<displays.limit();i++) {
                    int display=displays.get(i);
                    if(display!=org.lwjgl.sdl.SDLVideo.SDL_GetPrimaryDisplay() && org.lwjgl.sdl.SDLVideo.SDL_GetDisplayBounds(display,bounds) && bounds.x()>rightmost) {selected=display;rightmost=bounds.x();x=bounds.x();y=bounds.y();}
                }
            }
            if(selected==0)throw new AssertionError("Secondary display required for QA");
            if(!org.lwjgl.sdl.SDLVideo.SDL_SetWindowPosition(window,x+100,y+100))throw new AssertionError("Cannot position secondary-display window");
            System.out.println("EVATEX_QA secondary display="+org.lwjgl.sdl.SDLVideo.SDL_GetDisplayName(selected)+" position="+(x+100)+","+(y+100));
            evatexqa$positioned=true;
        }
''' + text[end:]
    if version in ('26.2','26.3'):
        text=text.replace('client.getSplashManager()','client.gui.splashManager()')
        text=text.replace('client.getMainRenderTarget()','client.gameRenderer.mainRenderTarget()')
        text=text.replace('client.getOverlay()', 'client.gui.overlay()').replace('client.screen','client.gui.screen()').replace('client.setScreen(', 'client.gui.setScreen(')
    source.write_text(text)
custom_source=ROOT/'tests/smoke/MainMenuScreen.java'
if version.startswith('26.'):
    custom_source=ROOT/'build'/('qa-custom-'+version)/'MainMenuScreen.java'
    custom_source.parent.mkdir(parents=True,exist_ok=True)
    custom_source.write_text(translate((ROOT/'tests/smoke/MainMenuScreen.java').read_text(),symbols(ROOT/'build/dependencies')))
hidden_source=ROOT/'tests/smoke/HiddenWindowMixin.java'
if version.startswith('26.'):
    hidden_source=ROOT/'build'/('qa-custom-'+version)/'HiddenWindowMixin.java'
    hidden_source.parent.mkdir(parents=True,exist_ok=True)
    hidden_source.write_text(translate((ROOT/'tests/smoke/HiddenWindowMixin.java').read_text(),symbols(ROOT/'build/dependencies')))
for previous in helper.rglob('*.class'): previous.unlink()
subprocess.run([str(javac),'-proc:none','--release',str(VERSIONS[version][1]),'-cp',str(classes)+os.pathsep+cp,'-d',str(helper),str(source),str(custom_source),str(hidden_source)],check=True)
with zipfile.ZipFile(mods/'evatex-qa.jar','w') as z:
    z.writestr('fabric.mod.json',json.dumps({'schemaVersion':1,'id':'evatex-qa','version':'1.0','name':'EVATEX QA','environment':'client','mixins':['evatex-qa.mixins.json']}))
    z.writestr('evatex-qa.mixins.json',json.dumps({'required':True,'package':'evatexqa','compatibilityLevel':'JAVA_'+str(VERSIONS[version][1]),'client':['SmokeMixin','HiddenWindowMixin'],'injectors':{'defaultRequire':1}}))
    for file in helper.rglob('*.class'): z.write(file,file.relative_to(helper))
suffix='' if args.bundled else '-system-ffmpeg'
shutil.copyfile(ROOT/'build/dist'/f'evatex-background-video-{MOD_VERSION}-mc{version}{suffix}.jar',mods/'evatex.jar')
if args.sodium: shutil.copyfile(args.sodium,mods/'sodium.jar')
if args.atlas:
    shutil.copyfile(args.atlas,mods/'atlas-client.jar')
    for pattern in ('fabric-api-*.jar','PlayerAnimationLibFabric-*.jar'):
        file=next(args.atlas_dependencies.glob(pattern));shutil.copyfile(file,mods/file.name)
if args.extra_mods:
    for extra in args.extra_mods.glob('*.jar'):
        if extra.resolve()!=(mods/extra.name).resolve():shutil.copyfile(extra,mods/extra.name)
videos=run/'videos';videos.mkdir(exist_ok=True)
if args.video:
    video=args.video.resolve()
    if not video.is_file(): raise SystemExit('Video file not found')
else:
    video=videos/'Astral demo.mp4'
    # Regenerate a monochrome clip; do not reuse older coloured QA videos.
    subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-f','lavfi','-i',"nullsrc=s=960x540:r=30,geq=r='28+25*sin(X/180+T)':g='30+24*sin(Y/240+T/2)':b='80+40*sin((X+Y)/220+T)',format=yuv420p",'-t','3','-vf','hue=s=0','-c:v','libx264','-crf','18',str(video)],check=True)
config=run/'config';config.mkdir(exist_ok=True)
if args.atlas:
    for atlas_config in (config/'atlasclient.json', config/'atlasclient.json.bak'):
        if atlas_config.exists():
            values=json.loads(atlas_config.read_text()); values['vanillaMode']=False
            atlas_config.write_text(json.dumps(values,indent=2))
(config/'evatexbackgroundvideo.json').write_text(json.dumps({'videoPath':str(video),'fps':30,'audioEnabled':False,'audioVolume':0.1,'scale':1.0,'darkening':.15,'showVignette':True}))
if args.extended_menus or args.reported_menus or args.home_menu:
    # A real summary-only save prevents vanilla auto-forwarding an empty list to world creation.
    # No world is entered/generated; all files live inside this isolated QA game directory.
    import gzip, struct
    def nbt_tag(kind,name,value):
        label=name.encode('utf-8')
        return bytes([kind])+struct.pack('>H',len(label))+label+value
    def nbt_string(value):
        data=value.encode('utf-8');return struct.pack('>H',len(data))+data
    with zipfile.ZipFile(ROOT/'build/dependencies'/f'{version}-official.jar') as archive:
        data_version=json.loads(archive.read('version.json'))['world_version']
    version_tag=nbt_tag(8,'Name',nbt_string(version))+nbt_tag(3,'Id',struct.pack('>i',data_version))+nbt_tag(1,'Snapshot',b'\x00')+b'\x00'
    data=nbt_tag(3,'version',struct.pack('>i',19133))+nbt_tag(8,'LevelName',nbt_string('EVATEX menu preview'))+nbt_tag(3,'DataVersion',struct.pack('>i',data_version))+nbt_tag(4,'LastPlayed',struct.pack('>q',0))+nbt_tag(3,'GameType',struct.pack('>i',1))+nbt_tag(1,'hardcore',b'\x00')+nbt_tag(1,'allowCommands',b'\x01')+nbt_tag(10,'Version',version_tag)+b'\x00'
    save=run/'saves'/'evatex-menu-preview';save.mkdir(parents=True,exist_ok=True)
    (save/'level.dat').write_bytes(gzip.compress(b'\x0a\x00\x00'+nbt_tag(10,'Data',data)+b'\x00'))
(run/'options.txt').write_text(f'lang:{args.language}\nguiScale:{args.gui_scale}\nfullscreen:false\nmaxFps:60\nskipMultiplayerWarning:true\n')
game=json.loads((ROOT/'build/dependencies'/f'{version}.json').read_text())
index=game['assetIndex']['id']
# Official client is needed on the runtime classpath. Fabric remaps it in the isolated run folder.
runtime_cp=cp.replace(str(ROOT/'build/dependencies'/f'{version}-intermediary.jar'),str(ROOT/'build/dependencies'/f'{version}-official.jar'))
command=[str(java),'-Xmx2G','-Dfabric.log.disableAnsi=true','-Devatex.qa.language='+args.language,'-Devatex.qa.extendedMenus='+str(args.extended_menus).lower(),'-Devatex.qa.reportedMenus='+str(args.reported_menus).lower(),'-Devatex.qa.hiddenWindow='+str(args.hidden_window).lower(),'-Devatex.qa.homeMenu='+str(args.home_menu).lower(),'-Devatex.qa.collisionLayout='+str(args.collision_layout).lower(),'-Devatex.qa.shortcutOnly='+str(args.shortcut_only).lower(),'-Devatex.qa.width='+str(args.width),'-Devatex.qa.height='+str(args.height),'-Devatex.qa.stageTicks='+('30' if args.quick else '100'),'-cp',runtime_cp,'net.fabricmc.loader.impl.launch.knot.KnotClient','--username',args.username,'--version',version,'--gameDir',str(run),'--assetsDir',str(args.assets),'--assetIndex',index,'--accessToken','0','--uuid','00000000000000000000000000000000','--userType','legacy','--versionType','release','--width',str(args.width),'--height',str(args.height)]
log=run/'launch.log'
print('Launching isolated test:',run,flush=True)
with log.open('w') as output:
    environment=dict(os.environ)
    if version=='26.3':environment['SDL_VIDEODRIVER']='x11'
    process=subprocess.Popen(command,cwd=run,stdout=output,stderr=subprocess.STDOUT,env=environment)
    try: code=process.wait(timeout=180)
    except subprocess.TimeoutExpired: process.terminate();process.wait(timeout=20);raise SystemExit('Smoke test timed out; see '+str(log))
print('Game exited:',code,'log:',log)
print('\n'.join(line for line in log.read_text(errors='replace').splitlines() if 'EVATEX_QA' in line or 'Mixin apply' in line or 'Cannot render video' in line or 'bridge disabled' in line))
if code!=0 or 'EVATEX_QA COMPLETE' not in log.read_text(errors='replace'): raise SystemExit(1)
