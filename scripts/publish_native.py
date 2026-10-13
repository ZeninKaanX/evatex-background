#!/usr/bin/env python3
"""Publish explicitly selected, validated native packages; reconcile by version/hash.

Requires MODRINTH_TOKEN or MODRINTH_TOKEN_FILE. Never logs credentials.
"""
import argparse, hashlib, http.client, json, os, subprocess, sys, urllib.request, uuid
from pathlib import Path
from build import ROOT, MOD_VERSION

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('files', nargs='+', type=Path)
    args=parser.parse_args()
    token=os.environ.get('MODRINTH_TOKEN')
    if not token and os.environ.get('MODRINTH_TOKEN_FILE'):
        token=Path(os.environ['MODRINTH_TOKEN_FILE']).read_text().strip()
    if not token:raise SystemExit('Set MODRINTH_TOKEN_FILE or MODRINTH_TOKEN')
    def api(path):
        req=urllib.request.Request('https://api.modrinth.com/v2/'+path,headers={'Authorization':token,'User-Agent':'EVATEX-publisher/'+MOD_VERSION})
        req.full_url += ('&' if '?' in req.full_url else '?')+'verify='+uuid.uuid4().hex
        with urllib.request.urlopen(req,timeout=60) as response:return json.load(response)
    remote=api('project/RWtUJXS3/version')
    fabric_batch=all(file.name.startswith(f'evatex-background-video-{MOD_VERSION}-mc') for file in args.files)
    kind='fabric' if fabric_batch else 'native'
    journal=ROOT/'build/native-loaders'/f'modrinth-{kind}-{MOD_VERSION}-matrix.json'
    results=json.loads(journal.read_text()) if journal.exists() else []
    for jar in args.files:
        jar=jar.resolve()
        # Static package validation is a prerequisite, not a runtime-test claim.
        if jar.name.startswith(f'evatex-background-video-{MOD_VERSION}-mc'):
            loader='fabric';mc=jar.name.split('-mc',1)[1].removesuffix('.jar')
            if '-system-ffmpeg' in mc:raise RuntimeError('Development JAR cannot be published')
            check=[sys.executable,str(ROOT/'scripts/check_packages.py'),'--minecraft',mc]
        else:
            loader='neoforge' if jar.name.endswith('-neoforge.jar') else 'forge'
            mc=jar.name.split('-mc',1)[1].rsplit('-'+loader+'.jar',1)[0]
            check=[sys.executable,str(ROOT/'scripts/check_native_packages.py'),str(jar)]
        receipt_path=jar.with_suffix('.jar.validation.json')
        receipt=json.loads(receipt_path.read_text()) if receipt_path.exists() else {}
        with jar.open('rb') as stream:sha256=hashlib.file_digest(stream,'sha256').hexdigest()
        if not (receipt.get('status')=='package-verified' and receipt.get('loader')==loader and receipt.get('minecraft')==mc and receipt.get('version')==MOD_VERSION and receipt.get('sha256')==sha256):
            subprocess.run(check,check=True)
        else:print('Reused unchanged package verification',loader,mc,flush=True)
        number=f'{MOD_VERSION}+mc{mc}.{loader}'
        found=next((item for item in remote if item['version_number']==number),None)
        # Hold the same inode through hashing and upload. Collection may
        # atomically replace the path while this publication is in progress.
        payload=jar.open('rb')
        digest=hashlib.file_digest(payload,'sha512').hexdigest()
        payload.seek(0)
        if found and found['files'][0]['hashes']['sha512']!=digest:
            raise RuntimeError('Existing published version has a different hash: '+number)
        if found is None:
            name={'neoforge':'NeoForge','forge':'Forge','fabric':'Fabric'}[loader]
            data={'project_id':'RWtUJXS3','name':f'EVATEX-Background {MOD_VERSION} / Minecraft {mc} / {name}','version_number':number,'changelog':f'{name} build for Minecraft {mc}. Video, GIF and photo backgrounds, volume controls and focus pause. Optional backgrounds in supported in-game settings screens are disabled by default; returning to gameplay stops decoding. Exact-version compilation and package checks passed. Runtime test coverage is documented in VALIDATION.md. Install one JAR matching your loader and Minecraft version.','dependencies':[],'game_versions':[mc],'version_type':'release','loaders':[loader],'featured':mc=='1.21.11','file_parts':['file'],'primary_file':'file','status':'listed'}
            boundary=uuid.uuid4().hex
            prefix=(f'--{boundary}\r\nContent-Disposition: form-data; name="data"\r\nContent-Type: application/json\r\n\r\n'+json.dumps(data)+f'\r\n--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="{jar.name}"\r\nContent-Type: application/java-archive\r\n\r\n').encode()
            suffix=f'\r\n--{boundary}--\r\n'.encode()
            conn=http.client.HTTPSConnection('api.modrinth.com',timeout=300)
            conn.putrequest('POST','/v2/version')
            headers={'Authorization':token,'User-Agent':'EVATEX-publisher/'+MOD_VERSION,'Content-Type':'multipart/form-data; boundary='+boundary,'Content-Length':str(len(prefix)+jar.stat().st_size+len(suffix))}
            for key,value in headers.items():conn.putheader(key,value)
            conn.endheaders();conn.send(prefix)
            print('Uploading',number,flush=True)
            while chunk:=payload.read(1024*1024):conn.send(chunk)
            conn.send(suffix);response=conn.getresponse();body=response.read();conn.close()
            if response.status not in (200,201):
                print('Publication stopped:',response.status,body.decode(errors='replace')[:600],flush=True)
                return 1
            found=json.loads(body);remote.append(found)
        payload.close()
        verified=api('version/'+found['id'])
        assert verified['files'][0]['hashes']['sha512']==digest
        assert verified['game_versions']==[mc] and verified['loaders']==[loader]
        entry={'minecraft':mc,'loader':loader,'id':verified['id'],'sha512':digest,'url':'https://modrinth.com/mod/evatex-background/version/'+verified['id']}
        results=[item for item in results if item['id']!=entry['id']]+[entry]
        journal.parent.mkdir(parents=True,exist_ok=True);journal.write_text(json.dumps(results,indent=2)+'\n')
        print('Verified',number,verified['id'],flush=True)
    return 0

if __name__=='__main__':sys.exit(main())
