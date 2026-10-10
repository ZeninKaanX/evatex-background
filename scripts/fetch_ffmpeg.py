#!/usr/bin/env python3
"""Fetch the pinned FFmpeg bundle without pulling unrelated code into the new mod."""
import hashlib
import json
from pathlib import Path
import shutil
import urllib.request
import zipfile
ROOT=Path(__file__).resolve().parent.parent
VERSION='aW88Gu2P'
META='https://api.modrinth.com/v2/version/'+VERSION
REQUEST_HEADERS={'User-Agent':'EVATEX-build/1.8.0'}
with urllib.request.urlopen(urllib.request.Request(META,headers=REQUEST_HEADERS),timeout=60) as response:
    metadata=json.load(response)
file=next(item for item in metadata['files'] if item['primary'])
cache=ROOT/'build/evatex-1.7.0-reference.jar';cache.parent.mkdir(parents=True,exist_ok=True)
if not cache.exists() or hashlib.sha512(cache.read_bytes()).hexdigest()!=file['hashes']['sha512']:
    temp=cache.with_suffix('.part')
    with urllib.request.urlopen(urllib.request.Request(file['url'],headers=REQUEST_HEADERS),timeout=120) as response, temp.open('wb') as output:
        shutil.copyfileobj(response,output)
    if hashlib.sha512(temp.read_bytes()).hexdigest()!=file['hashes']['sha512']: raise RuntimeError('FFmpeg bundle checksum mismatch')
    temp.replace(cache)
binaries={}
with zipfile.ZipFile(cache) as archive:
    for relative in ['win64/ffmpeg.exe','linux64/ffmpeg','COPYING.GPLv3']:
        name='assets/videomenu/ffmpeg/'+relative
        destination=ROOT/name;destination.parent.mkdir(parents=True,exist_ok=True)
        contents=archive.read(name);destination.write_bytes(contents)
        if relative=='linux64/ffmpeg': destination.chmod(0o755)
        binaries[relative]=hashlib.sha256(contents).hexdigest()
provenance={'origin':file['url'],'origin_sha512':file['hashes']['sha512'],'license':'GPL-3.0','sha256':binaries,
 'upstream_build_sources':['https://github.com/BtbN/FFmpeg-Builds','https://www.gyan.dev/ffmpeg/builds/'],'ffmpeg_source':'https://github.com/FFmpeg/FFmpeg'}
(ROOT/'assets/videomenu/ffmpeg/PROVENANCE.json').write_text(json.dumps(provenance,indent=2)+'\n')
print('Pinned FFmpeg bundle ready:',ROOT/'assets/videomenu/ffmpeg')
