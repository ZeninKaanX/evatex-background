"""CurseForge packages must not contain embedded executable payloads, even nested."""
import io,sys,zipfile
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent.parent/'scripts'))
from package_curseforge import verify_archive

def archive(name,content):
 b=io.BytesIO()
 with zipfile.ZipFile(b,'w') as z:z.writestr(name,content)
 return b.getvalue()
for name,content in [('ffmpeg.exe',b'data'),('bin/run.bat',b'echo test'),('payload.dat',b'MZxx'),('engine.gz',b'data'),('nested.jar',archive('bin/tool.exe',b'data'))]:
 try:
  with zipfile.ZipFile(io.BytesIO(archive(name,content))) as z:verify_archive(z)
  raise AssertionError('Executable accepted: '+name)
 except ValueError:pass
with zipfile.ZipFile(io.BytesIO(archive('Good.class',b'\xca\xfe\xba\xbe\x00\x00\x00\x3d'))) as z:verify_archive(z)
print('PASS CurseForge packaging rejects executables, hidden payloads, compressed engines and nested executables')
