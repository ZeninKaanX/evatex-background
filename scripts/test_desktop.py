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
if system=='Windows':
    filters=subprocess.check_output(['ffmpeg','-hide_banner','-filters'],text=True,stderr=subprocess.STDOUT)
    accelerators=subprocess.check_output(['ffmpeg','-hide_banner','-hwaccels'],text=True,stderr=subprocess.STDOUT)
    assert 'scale_d3d11' in filters and 'd3d11va' in accelerators,'Pinned Windows engine must expose the GPU path'
    scaler_help=subprocess.check_output(['ffmpeg','-hide_banner','-h','filter=scale_d3d11'],text=True,stderr=subprocess.STDOUT)
    import re
    assert all(re.search(r'^\s+'+option+r'\s+<',scaler_help,re.M) for option in ('width','height','format')),'Pinned D3D11 scaler option names must match the production command'
    print('PASS pinned Windows D3D11 decoder/scaler capabilities (physical GPU still required)',flush=True)

result=subprocess.run([sys.executable,str(ROOT/'scripts/test.py'),'--minecraft','1.21.11'],capture_output=True,text=True)
print(result.stdout+result.stderr,flush=True)
if result.returncode:
    lines=[line for line in (result.stdout+result.stderr).splitlines() if any(word in line for word in ('AssertionError','Exception','timing diagnostics','ERROR','PASS'))]
    message=' | '.join(lines)[-6000:].replace('%','%25').replace('\r','%0D').replace('\n','%0A')
    print('::error title=Desktop decoder diagnostic::'+message,flush=True)
    # Separate native decoder throughput from JVM-paced pipe reads on a failed host.
    import time
    fixture=ROOT/'build/tests-1.21.11/timing.mp4'
    for fps in (15,60):
        command=['ffmpeg','-hide_banner','-loglevel','error','-nostdin','-filter_threads','1','-stream_loop','-1','-threads','2','-i',str(fixture),'-map','0:v:0','-an','-vf',f'fps={fps},scale=160:90:flags=lanczos,setsar=1,format=rgba','-f','rawvideo','-pix_fmt','rgba','-threads','1','-flush_packets','1','-avioflags','direct','pipe:1']
        start=time.monotonic();process=subprocess.Popen(command,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
        try:
            remaining=160*90*4*fps*3
            while remaining:
                data=process.stdout.read(min(65536,remaining))
                if not data:break
                remaining-=len(data)
            elapsed=time.monotonic()-start
            print(f'::error title=Native throughput diagnostic::fps={fps} elapsed={elapsed:.3f}s missing_bytes={remaining}',flush=True)
        finally:process.kill();process.wait()
    raise SystemExit(result.returncode)
print('PASS host-native decoder:',system,arch)
