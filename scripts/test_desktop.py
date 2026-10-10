#!/usr/bin/env python3
"""Run actual decoder regressions using the checksum-pinned engine for this host."""
import gzip,os,platform,subprocess,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parent.parent
system=platform.system();arch=platform.machine().lower()
if system=='Darwin':target='darwin-arm64' if arch in ('arm64','aarch64') else 'darwin-x64'
elif system=='Linux' and arch in ('arm64','aarch64'):target='linux-arm64'
else:target='win64' if system=='Windows' else 'linux64'
if target in ('win64','linux64'):
 subprocess.run([sys.executable,str(ROOT/'scripts/fetch_ffmpeg.py')],check=True)
else:subprocess.run([sys.executable,str(ROOT/'scripts/fetch_platform_ffmpeg.py'),'--platform',target],check=True)
folder=ROOT/'assets/videomenu/ffmpeg'/target
if (folder/'ffmpeg.gz').exists():
 binary=folder/'ffmpeg';binary.write_bytes(gzip.decompress((folder/'ffmpeg.gz').read_bytes()));binary.chmod(0o755)
os.environ['PATH']=str(folder)+os.pathsep+os.environ['PATH']
subprocess.run([sys.executable,str(ROOT/'scripts/build.py'),'--minecraft','1.21.11','--system-ffmpeg'],check=True)
subprocess.run(['ffmpeg','-version'],check=True)
result=subprocess.run([sys.executable,str(ROOT/'scripts/test.py'),'--minecraft','1.21.11'],capture_output=True,text=True)
print(result.stdout+result.stderr,flush=True)
if result.returncode:
    lines=[line for line in (result.stdout+result.stderr).splitlines() if any(word in line for word in ('AssertionError','Exception','timing diagnostics','ERROR','PASS'))]
    message=' | '.join(lines)[-6000:].replace('%','%25').replace('\r','%0D').replace('\n','%0A')
    print('::error title=Desktop decoder diagnostic::'+message,flush=True)
    raise SystemExit(result.returncode)
print('PASS host-native decoder:',system,arch)
