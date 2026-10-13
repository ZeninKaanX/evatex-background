#!/usr/bin/env python3
"""Native-loader QA in isolated profiles, with a PID-specific secondary-window guard."""
import argparse,json,os,signal,subprocess,time
from pathlib import Path
from build_native import ROOT,generate
p=argparse.ArgumentParser()
p.add_argument('--loader',choices=('forge','neoforge'),required=True)
p.add_argument('--video',type=Path,required=True)
p.add_argument('--timeout',type=int,default=210)
a=p.parse_args()
if not a.video.is_file():raise SystemExit('Video not found')
project=generate(a.loader,'1.21.11',qa=True)
run=project/'run';config=run/'config';config.mkdir(parents=True,exist_ok=True)
(config/'fml.toml').write_text('earlyWindowControl=false\n')
(config/'evatexbackgroundvideo.json').write_text(json.dumps(dict(videoPath=str(a.video.resolve()),fps=30,audioEnabled=False,audioVolume=.1,scale=1.,darkening=0,showVignette=False)))
(run/'options.txt').write_text('lang:en_us\nguiScale:3\nmaxFps:30\nsoundCategory_master:0.0\nsoundCategory_music:0.0\n')
log=ROOT/'build'/f'{a.loader}-smoke.log'
guards=[]; guarded=set();start=time.monotonic()
with log.open('w') as out:
 task=subprocess.Popen(['nice','-n','19',str(project/'gradlew'),'-p',str(project),'runClient','-Pqa','--console','plain'],stdout=out,stderr=subprocess.STDOUT,start_new_session=True)
 try:
  while task.poll() is None:
   if time.monotonic()-start>a.timeout:raise TimeoutError('Native QA timed out')
   for item in Path('/proc').iterdir():
    if not item.name.isdigit() or int(item.name) in guarded:continue
    try:
     if Path(os.readlink(item/'cwd'))!=run:continue
     if 'java' not in os.readlink(item/'exe').split('/')[-1]:continue
     pid=int(item.name);os.kill(pid,signal.SIGSTOP)
     try:
      name=f'evatex-native-qa-{pid}';guard=ROOT/'build'/f'{name}.js'
      guard.write_text('workspace.windowAdded.connect(function(w){if(w.pid!=='+str(pid)+')return;w.frameGeometry={x:2020,y:100,width:1280,height:720};w.minimized=true;});\n')
      subprocess.run(['qdbus6','org.kde.KWin','/Scripting','org.kde.kwin.Scripting.loadScript',str(guard),name],check=True,stdout=subprocess.DEVNULL,timeout=10)
      subprocess.run(['qdbus6','org.kde.KWin','/Scripting','org.kde.kwin.Scripting.start'],check=True,stdout=subprocess.DEVNULL,timeout=10)
      guards.append(name);guarded.add(pid)
     finally:os.kill(pid,signal.SIGCONT)
    except (FileNotFoundError,PermissionError,ProcessLookupError):continue
   time.sleep(.1)
  if task.returncode:raise RuntimeError(f'{a.loader} QA failed; see {log}')
  text=log.read_text(errors='replace')
  if 'EVATEX_NATIVE_QA COMPLETE' not in text:raise RuntimeError('QA completion marker missing')
  print('PASS',a.loader,'native Minecraft menus, video, focus pause/resume, Astral home, native mods screen; muted 10%')
 finally:
  if task.poll() is None:
   os.killpg(task.pid,signal.SIGTERM)
  for name in guards:
   subprocess.run(['qdbus6','org.kde.KWin','/Scripting','org.kde.kwin.Scripting.unloadScript',name],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
