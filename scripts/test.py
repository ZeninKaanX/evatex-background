#!/usr/bin/env python3
"""Run decoder regression tests against a compiled source tree and real FFmpeg."""
import argparse
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
ROOT=Path(__file__).resolve().parent.parent
java_home=os.environ.get('JAVA_HOME')
java=str(Path(java_home)/'bin/java') if java_home else shutil.which('java')
javac=str(Path(java_home)/'bin/javac') if java_home else shutil.which('javac')
if not java or not javac: raise SystemExit('Set JAVA_HOME to JDK 21')
parser=argparse.ArgumentParser()
parser.add_argument('--minecraft',default='1.21.11')
version=parser.parse_args().minecraft
classes=ROOT/'build'/('classes-'+version)
cp=ROOT/'build'/('classpath-'+version+'.txt')
if not cp.exists(): raise SystemExit('Run scripts/build.py first')
work=ROOT/'build'/('tests-'+version); work.mkdir(parents=True,exist_ok=True)
video=work/'timing.mp4'
subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-f','lavfi','-i','testsrc2=size=160x90:rate=30','-t','1','-c:v','libx264','-pix_fmt','yuv420p',str(video)],check=True)
for extension in ['png','jpg','bmp']:
    subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-i',str(video),'-frames:v','1','-threads','1','-update','1',str(work/('photo %03d Türkçe.'+extension))],check=True)
classpath=os.pathsep.join([str(classes),cp.read_text()])
subprocess.run([javac,'-proc:none','-cp',classpath,'-d',str(work),str(ROOT/'tests/DecoderRegression.java')],check=True)
subprocess.run([java,'-cp',str(work)+os.pathsep+classpath,'DecoderRegression',str(video)],check=True)
subprocess.run([javac,'-proc:none','-cp',classpath,'-d',str(work),str(ROOT/'tests/RenderingFailureCleanupRegression.java')],check=True)
subprocess.run([java,'-cp',str(work)+os.pathsep+classpath,'com.videomenu.video.RenderingFailureCleanupRegression',str(video)],check=True)

subprocess.run([javac,'-proc:none','-cp',classpath,'-d',str(work),str(ROOT/'tests/AudioRegression.java')],check=True)
subprocess.run([java,'-cp',str(work)+os.pathsep+classpath,'com.videomenu.video.AudioRegression'],check=True)
# Check resource namespace and parity; Minecraft supplies built-in gui/options labels.
keys=set(json.loads((ROOT/'assets/evatexbackgroundvideo/lang/en_us.json').read_text()))
assert keys==set(json.loads((ROOT/'assets/evatexbackgroundvideo/lang/tr_tr.json').read_text()))
print('PASS English/Turkish translation key parity')

subprocess.run([javac,'-proc:none','-cp',classpath,'-d',str(work),str(ROOT/'tests/ShortcutLayoutRegression.java')],check=True)
subprocess.run([java,'-cp',str(work)+os.pathsep+classpath,'ShortcutLayoutRegression'],check=True)

subprocess.run([javac,"-proc:none","-cp",classpath,"-d",str(work),str(ROOT/"tests/MenuOwnershipRegression.java")],check=True)
subprocess.run([java,"-cp",str(work)+os.pathsep+classpath,"MenuOwnershipRegression"],check=True)

subprocess.run([javac,"-proc:none","-cp",classpath,"-d",str(work),str(ROOT/"tests/CustomWidgetBoundsRegression.java")],check=True)
subprocess.run([java,"-cp",str(work)+os.pathsep+classpath,"CustomWidgetBoundsRegression"],check=True)

subprocess.run([javac,"-proc:none","-cp",classpath,"-d",str(work),str(ROOT/"tests/LocalMediaPipeRegression.java")],check=True)
subprocess.run([java,"-cp",str(work)+os.pathsep+classpath,"com.videomenu.video.LocalMediaPipeRegression"],check=True)

subprocess.run([javac,"-proc:none","-cp",classpath,"-d",str(work),str(ROOT/"tests/MenuPlaybackRegression.java")],check=True)
subprocess.run([java,"-cp",str(work)+os.pathsep+classpath,"MenuPlaybackRegression"],check=True)

# An audio track is needed to verify that an opaque library holds only visuals.
av=work/'presentation-av.mp4'
subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-f','lavfi','-i','testsrc2=size=160x90:rate=30','-f','lavfi','-i','sine=frequency=440:sample_rate=48000','-t','2','-c:v','libx264','-threads','1','-pix_fmt','yuv420p','-c:a','aac',str(av)],check=True)
subprocess.run([javac,'-proc:none','-cp',classpath,'-d',str(work),str(ROOT/'tests/PresentationRegression.java')],check=True)
subprocess.run([java,'-cp',str(work)+os.pathsep+classpath,'PresentationRegression',str(av)],check=True)

# Fatal JVM errors must not be treated as recoverable mod failures.
subprocess.run([javac,'-proc:none','-cp',classpath,'-d',str(work),str(ROOT/'tests/RecoveryCircuitRegression.java')],check=True)
subprocess.run([java,'-cp',str(work)+os.pathsep+classpath,'RecoveryCircuitRegression'],check=True)

subprocess.run([sys.executable,str(ROOT/'scripts/test_config.py'),'--minecraft',version],check=True)

subprocess.run([javac,"-proc:none","-cp",classpath,"-d",str(work),str(ROOT/"tests/DiscordPresenceRegression.java")],check=True)
subprocess.run([java,"-cp",str(work)+os.pathsep+classpath,"com.videomenu.DiscordPresenceRegression"],check=True)
subprocess.run([javac,'-proc:none','-cp',str(work)+os.pathsep+classpath,'-d',str(work),str(ROOT/'tests/DiscordPresenceFailureRegression.java')],check=True)
subprocess.run([java,'-cp',str(work)+os.pathsep+classpath,'com.videomenu.DiscordPresenceFailureRegression'],check=True)


# High display/video ceilings must retain native cadence without duplicate full RGBA frames.
fixtures=[]
for rate in ('30','60','120','30000/1001'):
    clip=work/('fps-'+rate.replace('/','-')+'.mp4')
    subprocess.run(['ffmpeg','-v','error','-y','-f','lavfi','-i','testsrc2=size=160x90:rate='+rate,'-t','2','-c:v','libx264','-threads','1','-pix_fmt','yuv420p',str(clip)],check=True)
    fixtures.extend([str(clip),str(30000/1001 if '/' in rate else float(rate))])
subprocess.run([javac,'-proc:none','-cp',classpath,'-d',str(work),str(ROOT/'tests/HighFpsRegression.java')],check=True)
subprocess.run([java,'-cp',str(work)+os.pathsep+classpath,'HighFpsRegression',*fixtures],check=True)

subprocess.run([javac,'-proc:none','-cp',classpath,'-d',str(work),str(ROOT/'tests/FrameMailboxRegression.java')],check=True)
subprocess.run([java,'-cp',str(work)+os.pathsep+classpath,'com.videomenu.video.FrameMailboxRegression'],check=True)
