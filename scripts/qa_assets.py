"""Download official UI assets for isolated tests, skipping audio and unused languages."""
from concurrent.futures import ThreadPoolExecutor
import hashlib
import json
from pathlib import Path
import urllib.request
import sys
ROOT=Path(__file__).resolve().parent.parent
folder=ROOT/'build/qa-assets'
folder.mkdir(parents=True,exist_ok=True)
for version in sys.argv[1:]:
    game=json.loads((ROOT/'build/dependencies'/f'{version}.json').read_text())
    index=game['assetIndex'];target=folder/'indexes'/(index['id']+'.json');target.parent.mkdir(exist_ok=True)
    if not target.exists(): target.write_bytes(urllib.request.urlopen(index['url'],timeout=60).read())
    assert hashlib.sha1(target.read_bytes()).hexdigest()==index['sha1']
    objects=json.loads(target.read_text())['objects']
    wanted={v['hash']:v for k,v in objects.items() if not k.endswith('.ogg') and ('/lang/' not in k or k.endswith('/tr_tr.json') or k.endswith('/en_us.json'))}
    def download(h):
        file=folder/'objects'/h[:2]/h
        if file.exists() and hashlib.sha1(file.read_bytes()).hexdigest()==h: return
        file.parent.mkdir(parents=True,exist_ok=True)
        data=urllib.request.urlopen('https://resources.download.minecraft.net/'+h[:2]+'/'+h,timeout=60).read()
        assert hashlib.sha1(data).hexdigest()==h
        file.write_bytes(data)
    with ThreadPoolExecutor(max_workers=8) as pool: list(pool.map(download,wanted))
    print('UI assets ready',version,len(wanted),flush=True)
