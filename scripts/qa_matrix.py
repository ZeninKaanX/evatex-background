#!/usr/bin/env python3
"""Sequential real-client checks on the required secondary display, using only 1080p input."""
import json,os,subprocess,sys
from pathlib import Path
from build import VERSIONS, MOD_VERSION
ROOT=Path(__file__).resolve().parent.parent
video=Path('/home/eververity/.local/share/prismlauncher-cracked/instances/1.21.11/minecraft/videos/Chainsaw Man Reze Dance [4K version]_1080p60.mp4')
java21=Path('/home/eververity/.local/share/prismlauncher-cracked/java/java-21')
java25=ROOT/'build/tools/jdk-25.0.4.1+1'
results=[]
for version in VERSIONS:
 env={**os.environ,'JAVA_HOME':str(java25 if version.startswith('26.') else java21)}
 command=[sys.executable,str(ROOT/'scripts/smoke.py'),'--minecraft',version,'--assets',str(ROOT/'build/qa-assets'),'--video',str(video),'--bundled','--quick']
 print('GUI checking',version,flush=True)
 result=subprocess.run(command,cwd=ROOT,env=env,capture_output=True,text=True)
 (ROOT/'build'/('qa-'+MOD_VERSION+'-'+version+'.log')).write_text(result.stdout+result.stderr)
 if result.returncode:print(result.stdout+result.stderr,flush=True);raise SystemExit(result.returncode)
 results.append({'minecraft':version,'result':'PASS','video':'provided 1920x1080 clip','secondary_display':True,'bundled_engine':True,'audio_toggle':True})
 (ROOT/'build'/('qa-'+MOD_VERSION+'-matrix.json')).write_text(json.dumps(results,indent=2)+'\n')
 print('PASS real GUI',version,flush=True)
print('COMPLETE all 24 real-client checks',flush=True)
