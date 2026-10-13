#!/usr/bin/env python3
"""Collect successful pinned GitHub builds, verify archive digests and bundle locally.

No remote artifact is executed. Runtime tests are recorded separately.
"""
import argparse,hashlib,json,subprocess,sys,urllib.request,zipfile
from pathlib import Path
from build import ROOT,MOD_VERSION
from build_native import package_native
from package_fabric_ci import package_fabric
REPO='ZeninKaanX/evatex-background-video-1.8.2'

def get_json(url):
    request=urllib.request.Request(url,headers={'User-Agent':'EVATEX-CI-collector/'+MOD_VERSION})
    with urllib.request.urlopen(request,timeout=60) as response:return json.load(response)

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--run',required=True,type=int);parser.add_argument('--commit',required=True);parser.add_argument('--fabric',action='store_true');args=parser.parse_args()
    base='https://api.github.com/repos/'+REPO+'/actions/runs/'+str(args.run)
    run=get_json(base)
    if run['head_sha']!=args.commit:raise RuntimeError('Unexpected GitHub source revision')
    jobs=get_json(base+'/jobs?per_page=100')['jobs']
    successful={job['name'] for job in jobs if job['conclusion']=='success'}
    artifacts=get_json(base+'/artifacts?per_page=100')['artifacts']
    remote=get_json('https://api.modrinth.com/v2/project/RWtUJXS3/version')
    published={item['version_number'] for item in remote}
    kind='fabric' if args.fabric else 'native'
    report=ROOT/'build/native-loaders'/f'cloud-collection-{args.run}-{kind}.json'
    receipts=json.loads(report.read_text()) if report.exists() else {}
    for item in artifacts:
        prefix='fabric-development-' if args.fabric else 'development-'
        if not item['name'].startswith(prefix) or item['expired']:continue
        if args.fabric:loader='fabric';version=item['name'][len(prefix):]
        else:_,loader,version=item['name'].split('-',2)
        key=version+'/'+loader
        job=f'compile-and-decoder ({version})' if args.fabric else f'compile ({version}, {loader})'
        if job not in successful:continue
        if f'{MOD_VERSION}+mc{version}.{loader}' in published:
            print('Already published',key,flush=True);continue
        if receipts.get(key,{}).get('artifact_id')==item['id']:continue
        expected=item.get('digest','')
        if not expected.startswith('sha256:'):raise RuntimeError('GitHub artifact digest missing')
        folder=ROOT/'build/native-loaders/cloud';folder.mkdir(parents=True,exist_ok=True)
        archive_path=folder/(item['name']+'.zip')
        request=urllib.request.Request(f'https://nightly.link/{REPO}/actions/artifacts/{item["id"]}.zip',headers={'User-Agent':'EVATEX-CI-collector/'+MOD_VERSION})
        with urllib.request.urlopen(request,timeout=60) as response,archive_path.open('wb') as stream:
            while chunk:=response.read(1024*1024):stream.write(chunk)
        with archive_path.open('rb') as stream:digest=hashlib.file_digest(stream,'sha256').hexdigest()
        if 'sha256:'+digest!=expected:raise RuntimeError('GitHub artifact digest mismatch: '+key)
        name=f'evatex-background-video-{MOD_VERSION}-mc{version}-system-ffmpeg.jar' if args.fabric else f'evatex-background-{loader}-mc{version}-{MOD_VERSION}.jar'
        with zipfile.ZipFile(archive_path) as archive:
            candidates=[entry for entry in archive.infolist() if Path(entry.filename).name==name]
            if len(candidates)!=1:raise RuntimeError('Exact development JAR not found: '+key)
            if candidates[0].file_size>10*1024*1024:raise RuntimeError('Unexpected development JAR size')
            artifact=folder/name;artifact.write_bytes(archive.read(candidates[0]))
        if args.fabric:
            release=package_fabric(version,artifact)
            check=[sys.executable,str(ROOT/'scripts/check_packages.py'),'--minecraft',version]
        else:
            package_native(loader,version,artifact)
            release=ROOT/'build/dist'/f'evatex-background-{MOD_VERSION}-mc{version}-{loader}.jar'
            check=[sys.executable,str(ROOT/'scripts/check_native_packages.py'),str(release)]
        subprocess.run(check,check=True)
        receipts[key]={'artifact_id':item['id'],'github_run':args.run,'source_commit':args.commit,'archive_sha256':digest,'status':'compiled-and-package-verified','runtime_test':False,'file':str(release)}
        report.write_text(json.dumps(receipts,indent=2)+'\n')
        print('Collected verified native build',key,flush=True)
    print('GitHub run status:',run['status'],run['conclusion'],flush=True)

if __name__=='__main__':main()
