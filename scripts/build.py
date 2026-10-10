#!/usr/bin/env python3
"""Compile exact Minecraft APIs: intermediary through 1.21.11, native names on 26.x."""
import argparse
from concurrent.futures import ThreadPoolExecutor
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import urllib.request
import zipfile
from ports import sources, API

ROOT = Path(__file__).resolve().parent.parent
CACHE = ROOT / 'build' / 'dependencies'
VERSIONS = {v: ('src120' if v.startswith('1.20.') or v == '1.20' else 'src', 17 if v in ('1.20','1.20.1','1.20.2','1.20.3','1.20.4') else 21) for v in ('1.20','1.20.1','1.20.2','1.20.3','1.20.4','1.20.5','1.20.6','1.21','1.21.1','1.21.2','1.21.3','1.21.4','1.21.5','1.21.6','1.21.7','1.21.8','1.21.9','1.21.10','1.21.11')}

VERSIONS.update({v: ('src',25) for v in ('26.1','26.1.1','26.1.2','26.2','26.3')})

def fetch(url, path, sha1=None):
    path = Path(path)
    if path.exists() and (not sha1 or hashlib.sha1(path.read_bytes()).hexdigest() == sha1):
        return path
    path.parent.mkdir(parents=True, exist_ok=True)
    print('Downloading', path.name, flush=True)
    request = urllib.request.Request(url, headers={'User-Agent': 'EVATEX-build/1.8.0'})
    temp = path.with_suffix(path.suffix + '.part')
    with urllib.request.urlopen(request, timeout=120) as src, temp.open('wb') as dst:
        shutil.copyfileobj(src, dst)
    if sha1 and hashlib.sha1(temp.read_bytes()).hexdigest() != sha1:
        raise RuntimeError('Checksum mismatch: ' + str(path))
    temp.replace(path)
    return path

def metadata(url, name):
    return json.loads(fetch(url, CACHE / name).read_text())

def build(version, system_ffmpeg, prepare_only=False):
    source, release = VERSIONS[version]
    java_home = os.environ.get('JAVA_HOME')
    java = str(Path(java_home) / 'bin/java') if java_home else shutil.which('java')
    javac = str(Path(java_home) / 'bin/javac') if java_home else shutil.which('javac')
    if not java or not javac: raise RuntimeError('Set JAVA_HOME to JDK '+str(release)+' or later')
    manifest = metadata('https://piston-meta.mojang.com/mc/game/version_manifest_v2.json', 'manifest.json')
    entry = next(item for item in manifest['versions'] if item['id'] == version)
    game = metadata(entry['url'], version + '.json')
    client = fetch(game['downloads']['client']['url'], CACHE / (version+'-official.jar'), game['downloads']['client']['sha1'])
    # Fabric supplies its required ASM revision; the vanilla profile may include an older one.
    artifacts = [lib['downloads']['artifact'] for lib in game['libraries'] if 'artifact' in lib.get('downloads', {}) and not lib['name'].startswith('org.ow2.asm:')]
    with ThreadPoolExecutor(max_workers=8) as pool:
        libraries = list(pool.map(lambda a: fetch(a['url'], CACHE/a['path'], a['sha1']), artifacts))
    profile = metadata(f'https://meta.fabricmc.net/v2/versions/loader/{version}/0.19.5/profile/json', version+'-fabric.json')
    for lib in profile['libraries']:
        group, artifact, revision = lib['name'].split(':')
        path = group.replace('.', '/')+'/'+artifact+'/'+revision+'/'+artifact+'-'+revision+'.jar'
        libraries.append(fetch(lib.get('url', 'https://maven.fabricmc.net/')+path, CACHE/path))
    maven = 'https://maven.fabricmc.net/'
    if version.startswith('26.'):
        mapped = client
    else:
        remapper = fetch(maven+'net/fabricmc/tiny-remapper/0.11.2/tiny-remapper-0.11.2-fat.jar', CACHE/'tiny-remapper.jar')
        mappings = fetch(maven+f'net/fabricmc/intermediary/{version}/intermediary-{version}-v2.jar', CACHE/(version+'-mappings.jar'))
        tiny = CACHE/(version+'.tiny')
        with zipfile.ZipFile(mappings) as z: tiny.write_bytes(z.read('mappings/mappings.tiny'))
        mapped = CACHE/(version+'-intermediary.jar')
        if not mapped.exists():
            subprocess.run([java,'-jar',str(remapper),str(client),str(mapped),str(tiny),'official','intermediary',*map(str,libraries)], check=True)
    cp = os.pathsep.join(map(str,[mapped,*libraries]))
    (ROOT/'build'/('classpath-'+version+'.txt')).write_text(cp)
    if prepare_only:
        print('Prepared',version)
        return
    if version.startswith('26.'):
        reference_entry=next(item for item in manifest['versions'] if item['id']=='1.21.11')
        reference_game=metadata(reference_entry['url'],'1.21.11.json')
        reference=reference_game['downloads']['client_mappings']
        fetch(reference['url'], CACHE/'1.21.11-mojang-mappings.txt',reference['sha1'])
        reference_tiny=fetch(maven+'net/fabricmc/intermediary/1.21.11/intermediary-1.21.11-v2.jar',CACHE/'1.21.11-mappings.jar')
        with zipfile.ZipFile(reference_tiny) as archive: (CACHE/'1.21.11.tiny').write_bytes(archive.read('mappings/mappings.tiny'))
    source_dir, mixin_file = sources(ROOT, version)
    classes = ROOT/'build'/('classes-'+version)
    if classes.exists(): shutil.rmtree(classes)
    classes.mkdir(parents=True,exist_ok=True)
    cp = os.pathsep.join(map(str,[mapped,*libraries]))
    subprocess.run([javac,'-encoding','UTF-8','-proc:none','--release',str(release),'-cp',cp,'-d',str(classes),*map(str,source_dir.rglob('*.java'))],check=True)
    (ROOT/'build'/('classpath-'+version+'.txt')).write_text(cp)
    binary_dir = ROOT/'assets/videomenu/ffmpeg'
    if not system_ffmpeg:
        for binary in ('win64/ffmpeg.exe','linux64/ffmpeg','COPYING.GPLv3'):
            if not (binary_dir/binary).is_file(): raise RuntimeError('Run scripts/fetch-ffmpeg.sh before packaging: missing '+binary)
    api_version = API[version]
    api = fetch(maven+f'net/fabricmc/fabric-api/fabric-api/{api_version}/fabric-api-{api_version}.jar', CACHE/f'fabric-api-{api_version}.jar')
    resource_module = 'fabric-resource-loader-v1' if version in ('1.21.9','1.21.10','1.21.11') or version.startswith('26.') else 'fabric-resource-loader-v0'
    resource_modules = [resource_module]
    if version in ('1.21.9','1.21.10'): resource_modules.append('fabric-resource-loader-v0')
    nested = {}
    with zipfile.ZipFile(api) as archive:
        for name in archive.namelist():
            if name.startswith('META-INF/jars/') and (name.split('/')[-1].startswith('fabric-api-base-') or any(name.split('/')[-1].startswith(module+'-') for module in resource_modules)):
                nested[name] = archive.read(name)
    if len(nested) != 1+len(resource_modules): raise RuntimeError('Missing Fabric resource modules')
    config=json.loads((ROOT/'fabric.mod.json').read_text())
    config['depends']['minecraft']=version
    config['depends']['java']='>='+str(release)
    config['depends']['fabricloader']='>=0.19.5'
    config['depends'][resource_module]='*'
    config['jars']=[{'file':name} for name in nested]
    dist=ROOT/'build/dist'; dist.mkdir(parents=True,exist_ok=True)
    suffix='-system-ffmpeg' if system_ffmpeg else ''
    output=dist/f'evatex-background-video-1.8.0-mc{version}{suffix}.jar'
    with zipfile.ZipFile(output,'w',zipfile.ZIP_DEFLATED) as z:
        for name, data in nested.items(): z.writestr(name,data)
        z.writestr('fabric.mod.json',json.dumps(config,indent=2)+'\n')
        mixin_config=json.loads(mixin_file.read_text())
        mixin_config['compatibilityLevel']='JAVA_'+str(release)
        z.writestr('videomenu.mixins.json',json.dumps(mixin_config,indent=2)+'\n')
        for name in ('LICENSE','README.md','CHANGELOG.md','VALIDATION.md','MODRINTH_RELEASE.md','COMPATIBILITY.md','docs/astral-interface.png'): z.write(ROOT/name,name)
        for folder in (classes,ROOT/'assets'):
            for file in sorted(folder.rglob('*')):
                if file.is_file() and not (system_ffmpeg and binary_dir in file.parents):
                    z.write(file,file.relative_to(classes if folder==classes else ROOT))
    output.with_suffix('.jar.sha256').write_text(hashlib.sha256(output.read_bytes()).hexdigest()+'  '+output.name+'\n')
    print('Built',output)

if __name__ == '__main__':
    parser=argparse.ArgumentParser()
    target=parser.add_mutually_exclusive_group()
    target.add_argument('--minecraft',choices=VERSIONS,default=None)
    target.add_argument('--all',action='store_true',help='Build all supported stable Minecraft releases; use JDK 25')
    parser.add_argument('--prepare-only',action='store_true',help='Download and remap dependencies without declaring or packaging compatibility')
    parser.add_argument('--system-ffmpeg',action='store_true',help='Development build without bundled binaries')
    args=parser.parse_args()
    for version in (VERSIONS if args.all else [args.minecraft or '1.21.11']):
        build(version,args.system_ffmpeg,args.prepare_only)
