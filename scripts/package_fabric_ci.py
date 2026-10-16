#!/usr/bin/env python3
"""Add checked local media engines to a verified Fabric development JAR."""
import argparse,hashlib,json,shutil,zipfile
from pathlib import Path
from build import ROOT,MOD_VERSION,VERSIONS
from loader_policy import minimum_loader

def package_fabric(version,artifact):
    with zipfile.ZipFile(artifact) as archive:
        metadata=json.loads(archive.read('fabric.mod.json'))
        assert metadata['version']==MOD_VERSION and metadata['depends']['minecraft']==version
        assert metadata['id']=='evatexbackgroundvideo'
        assert metadata['depends']['fabricloader']=='>='+minimum_loader(version)
        assert not any(name.startswith('evatexqa/') for name in archive.namelist())
    files=[file for file in sorted((ROOT/'assets').rglob('*')) if file.is_file()]
    digest=hashlib.sha256()
    for file in files:
        digest.update(str(file.relative_to(ROOT)).encode());digest.update(str(file.stat().st_mtime_ns).encode());digest.update(str(file.stat().st_size).encode())
    cache=ROOT/'build'/('resources-'+digest.hexdigest()+'.zip')
    if not cache.exists():
        with zipfile.ZipFile(cache,'w',zipfile.ZIP_DEFLATED) as archive:
            for file in files:archive.write(file,file.relative_to(ROOT),compress_type=zipfile.ZIP_STORED if file.suffix=='.gz' else zipfile.ZIP_DEFLATED)
    output=ROOT/'build/dist'/f'evatex-background-video-{MOD_VERSION}-mc{version}.jar'
    shutil.copyfile(cache,output)
    with zipfile.ZipFile(output,'a',zipfile.ZIP_DEFLATED) as target,zipfile.ZipFile(artifact) as source:
        for entry in source.infolist():
            if not entry.filename.startswith('assets/'):target.writestr(entry,source.read(entry.filename))
    with output.open('rb') as stream:checksum=hashlib.file_digest(stream,'sha256').hexdigest()
    output.with_suffix('.jar.sha256').write_text(checksum+'  '+output.name+'\n')
    print('Bundled Fabric release',version,flush=True)
    return output

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--minecraft',choices=VERSIONS,required=True);parser.add_argument('artifact',type=Path);args=parser.parse_args()
    package_fabric(args.minecraft,args.artifact)
