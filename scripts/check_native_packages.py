#!/usr/bin/env python3
"""Check native release metadata, dependencies, QA exclusion and bundled engines."""
import argparse,hashlib,json,tomllib,zipfile,re
from pathlib import Path
from build import ROOT,MOD_VERSION,VERSIONS
p=argparse.ArgumentParser();p.add_argument('files',nargs='*',type=Path);args=p.parse_args()
files=args.files or [ROOT/'build/dist'/f'evatex-background-{MOD_VERSION}-mc1.21.11-{loader}.jar' for loader in ('forge','neoforge')]
report=[]
for file in files:
 loader='neoforge' if file.name.endswith('-neoforge.jar') else 'forge'
 version=re.search(r'-mc([0-9.]+)-',file.name)[1]
 with zipfile.ZipFile(file) as archive:
  names=archive.namelist()
  assert len(names)==len(set(names)),'Duplicate ZIP entries'
  assert 'fabric.mod.json' not in names,'Fabric metadata in native build'
  assert not any(n.startswith('evatexqa/') or 'evatexqa.mixins' in n or 'ModMenuBridge.class' in n for n in names),'QA or Fabric API helper leaked'
  assert not any(b'net/fabricmc/' in archive.read(n) for n in names if n.endswith('.class')),'Fabric bytecode dependency'
  for name in names:
   if name.endswith('.class'):
    assert int.from_bytes(archive.read(name)[6:8],'big')==44+VERSIONS[version][1], 'Wrong Java target: '+name
  meta='META-INF/neoforge.mods.toml' if 'META-INF/neoforge.mods.toml' in names else 'META-INF/mods.toml'
  config=tomllib.loads(archive.read(meta).decode())
  assert config['mods'][0]['version']==MOD_VERSION
  assert config['dependencies']['evatexbackgroundvideo'][0]['versionRange']=='['+version+']'
  from native_projects import target
  expected_loader='forge' if version in ('1.20','1.20.1') else loader
  assert config['dependencies']['evatexbackgroundvideo'][1]['modId']==expected_loader
  assert config['dependencies']['evatexbackgroundvideo'][1]['versionRange']=='['+target(loader,version)+',)'
  mixins=json.loads(archive.read('videomenu.mixins.json'))
  assert 'plugin' not in mixins and all(not n.startswith('atlas.') for n in mixins['client'])
  if 'refmap' in mixins:
   assert mixins['refmap'] in names,'Missing production refmap'
   assert json.loads(archive.read(mixins['refmap'])).get('mappings'),'Empty production refmap'
  for asset in ('assets/videomenu/icon.png','assets/evatexbackgroundvideo/font/ui.ttf','assets/videomenu/ffmpeg/COPYING.GPLv3','assets/videomenu/ffmpeg/PROVENANCE.json','assets/videomenu/ffmpeg/PLATFORMS.json'):
   assert asset in names,asset
  engines={}
  for path in ('linux64/ffmpeg','win64/ffmpeg.exe','darwin-x64/ffmpeg.gz','darwin-arm64/ffmpeg.gz','linux-arm64/ffmpeg.gz'):
   asset='assets/videomenu/ffmpeg/'+path
   with archive.open(asset) as stream:bundled=hashlib.file_digest(stream,'sha256').hexdigest()
   with (ROOT/asset).open('rb') as stream:assert bundled==hashlib.file_digest(stream,'sha256').hexdigest(),asset
   engines[path]=bundled
 with file.open('rb') as stream:checksum=hashlib.file_digest(stream,'sha256').hexdigest()
 assert file.with_suffix('.jar.sha256').read_text().split()[0]==checksum
 entry=dict(loader=loader,minecraft=version,version=MOD_VERSION,file=str(file),bytes=file.stat().st_size,sha256=checksum,qaHelpers=False,fabricClasses=False,engines=engines,status='package-verified')
 report.append(entry)
 file.with_suffix('.jar.validation.json').write_text(json.dumps(entry,indent=2)+'\n')
 print('PASS native package',loader,file.name)
path=ROOT/'build/native-loaders/package-validation.json';path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(report,indent=2)+'\n')
