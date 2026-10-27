#!/usr/bin/env python3
"""Create an explicitly external-engine CurseForge package; never disguise binaries."""
import argparse,hashlib,io,json,zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parent.parent
MARKER='assets/evatexbackgroundvideo/external-engine.properties'
def verify_archive(archive,depth=0):
 if depth>3:raise ValueError('Unexpected nested archive depth')
 for entry in archive.infolist():
  name=entry.filename.lower()
  if name.endswith(('.exe','.bat','.cmd','.com','.dll','.so','.dylib','.gz','.xz','.7z')):raise ValueError('Executable/native archive in CurseForge package: '+entry.filename)
  if entry.filename.startswith('assets/videomenu/ffmpeg/'):raise ValueError('Bundled engine resource remains')
  if name.endswith('.jar'):
   with zipfile.ZipFile(io.BytesIO(archive.read(entry))) as nested:verify_archive(nested,depth+1)
  elif not name.endswith('.class'):
   with archive.open(entry) as stream:magic=stream.read(4)
   if magic.startswith(b'MZ') or magic in (b'\x7fELF',b'\xcf\xfa\xed\xfe',b'\xfe\xed\xfa\xcf'):raise ValueError('Executable payload: '+entry.filename)
def package(source):
 source=Path(source).resolve()
 with zipfile.ZipFile(source) as original:
  if MARKER in original.namelist():raise ValueError('Already a CurseForge package')
  meta=json.loads(original.read('fabric.mod.json')) if 'fabric.mod.json' in original.namelist() else None
  if meta:mc=meta['depends']['minecraft'];loader='fabric';version=meta['version']
  else:
   receipt=json.loads(source.with_suffix('.jar.validation.json').read_text());mc=receipt['minecraft'];loader=receipt['loader'];version=receipt['version']
  assert 'assets/evatexbackgroundvideo/engine-manifest.json' in original.namelist()
  target=ROOT/'build/curseforge-dist'/source.name.replace('-system-ffmpeg','');target.parent.mkdir(parents=True,exist_ok=True)
  temporary=target.with_suffix('.tmp')
  with zipfile.ZipFile(temporary,'w',zipfile.ZIP_DEFLATED) as out:
   for entry in original.infolist():
    if not entry.filename.startswith('assets/videomenu/ffmpeg/') and entry.filename not in ('README.md','CHANGELOG.md','VALIDATION.md','MODRINTH_RELEASE.md'):out.writestr(entry,original.read(entry))
   out.writestr(MARKER,'distribution=curseforge\nengine=external-opt-in\n')
   out.write(ROOT/'docs/VIDEO-ENGINE.md','VIDEO-ENGINE.md')
   for doc in ('README.md','CHANGELOG.md','VALIDATION.md','MODRINTH_RELEASE.md'):out.write(ROOT/doc,doc)
  with zipfile.ZipFile(temporary) as checked:
   verify_archive(checked)
   assert MARKER in checked.namelist() and 'com/videomenu/video/ExternalVideoEngine.class' in checked.namelist()
   assert len(checked.namelist())==len(set(checked.namelist()))
  temporary.replace(target)
 digest=hashlib.file_digest(target.open('rb'),'sha256').hexdigest()
 target.with_suffix('.jar.validation.json').write_text(json.dumps(dict(status='package-verified',distribution='curseforge-external-engine',loader=loader,minecraft=mc,version=version,sha256=digest,executables_bundled=False),indent=2)+'\n')
 print('Verified external-engine CurseForge package:',target,flush=True);return target
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('files',type=Path,nargs='+');a=p.parse_args()
 for file in a.files:package(file)
