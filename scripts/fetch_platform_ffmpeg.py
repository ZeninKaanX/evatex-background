#!/usr/bin/env python3
"""Fetch pinned, checksum-verified offline macOS and Linux ARM64 FFmpeg engines."""
import argparse,hashlib,json,urllib.request,shutil
from pathlib import Path
ROOT=Path(__file__).resolve().parent.parent
BASE='https://github.com/eugeneware/ffmpeg-static/releases/download/b6.1.1/'
PINS={
 'darwin-arm64':('8923876afa8db5585022d7860ec7e589af192f441c56793971276d450ed3bbfa','a90e3db6a3fd35f6074b013f948b1aa45b31c6375489d39e572bea3f18336584'),
 'darwin-x64':('929b375c1182d956c51f7ac25e0b2b0411fb01f6f407aa15c9758efeb4242106','ebdddc936f61e14049a2d4b549a412b8a40deeff6540e58a9f2a2da9e6b18894'),
 'linux-arm64':('754a678672298bc68156adff58aa7385a592c2b30b1d0ae8750c45c915c4bac0','6bb182d0d75d23028db82e9e4f723ca69b853d055698486e6984ddb2c06fb8ce')}
parser=argparse.ArgumentParser();parser.add_argument('--platform',choices=PINS);args=parser.parse_args()
root=ROOT/'assets/videomenu/ffmpeg';manifest={}
for platform,(compressed,extracted) in PINS.items():
    if args.platform and args.platform!=platform:continue
    dest=root/platform;dest.mkdir(parents=True,exist_ok=True)
    for name in ('ffmpeg-'+platform+'.gz',platform+'.LICENSE',platform+'.README'):
        path=dest/('ffmpeg.gz' if name.endswith('.gz') else name.split('.')[-1]+'.txt')
        if not path.exists():
            request=urllib.request.Request(BASE+name,headers={'User-Agent':'EVATEX-build/1.8.1'})
            with urllib.request.urlopen(request,timeout=120) as response,path.with_suffix('.part').open('wb') as output:shutil.copyfileobj(response,output)
            path.with_suffix('.part').replace(path)
        if name.endswith('.gz') and hashlib.sha256(path.read_bytes()).hexdigest()!=compressed:raise RuntimeError('Checksum mismatch: '+platform)
    manifest[platform]={'url':BASE+'ffmpeg-'+platform+'.gz','sha256':compressed,'binary_sha256':extracted,'source':'https://github.com/eugeneware/ffmpeg-static','license_file':platform+'/LICENSE.txt','build_info':platform+'/README.txt'}
    print('Verified offline engine:',platform,flush=True)
(root/'PLATFORMS.json').write_text(json.dumps(manifest,indent=2)+'\n')
