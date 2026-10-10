"""Check distributable metadata, class versions, embedded modules and bundled FFmpeg."""
import argparse
import hashlib
import io
import json
from pathlib import Path
import zipfile
from build import VERSIONS
ROOT=Path(__file__).resolve().parent.parent
parser=argparse.ArgumentParser()
parser.add_argument('--minecraft',choices=VERSIONS)
args=parser.parse_args()
for version in ([args.minecraft] if args.minecraft else VERSIONS):
    jar=ROOT/'build/dist'/f'evatex-background-video-1.8.0-mc{version}.jar'
    with zipfile.ZipFile(jar) as archive:
        names=archive.namelist()
        assert len(names)==len(set(names)), 'Duplicate archive entries'
        meta=json.loads(archive.read('fabric.mod.json'))
        assert meta['version']=='1.8.0'
        assert meta['depends']['minecraft']==version
        assert meta['depends']['java']=='>='+str(VERSIONS[version][1])
        mixins=json.loads(archive.read('videomenu.mixins.json'))
        assert mixins['compatibilityLevel']=='JAVA_'+str(VERSIONS[version][1])
        assert len(meta['jars'])==(3 if version in ('1.21.9','1.21.10') else 2)
        assert not any('evatexqa' in name or name.startswith('net/minecraft/') or name.endswith('atlas-client.jar') for name in names)
        for nested in meta['jars']:
            assert nested['file'] in names
            with zipfile.ZipFile(io.BytesIO(archive.read(nested['file']))) as module:
                for entry in module.namelist():
                    if entry.endswith('.class') and not entry.startswith('META-INF/versions/'):
                        assert int.from_bytes(module.read(entry)[6:8],'big')<=44+VERSIONS[version][1], 'Embedded module needs newer Java'
        for name in names:
            if name.endswith('.class'):
                data=archive.read(name)
                assert data[:4]==b'\xca\xfe\xba\xbe' and int.from_bytes(data[6:8],'big')==44+VERSIONS[version][1]
        for language in ('tr_tr','en_us'):
            assert f'assets/evatexbackgroundvideo/lang/{language}.json' in names
        provenance=json.loads(archive.read('assets/videomenu/ffmpeg/PROVENANCE.json'))
        for binary,checksum in provenance['sha256'].items():
            assert hashlib.sha256(archive.read('assets/videomenu/ffmpeg/'+binary)).hexdigest()==checksum
        assert archive.testzip() is None
    digest=hashlib.sha256(jar.read_bytes()).hexdigest()
    assert jar.with_suffix('.jar.sha256').read_text().split()[0]==digest
    print('PASS bundled package',version,flush=True)
