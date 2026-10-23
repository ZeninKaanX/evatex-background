#!/usr/bin/env python3
"""Check the feature-specific publisher config, logo and classes in each release JAR."""
import argparse,hashlib,json,zipfile
from pathlib import Path
root=Path(__file__).resolve().parent.parent
p=argparse.ArgumentParser();p.add_argument('files',nargs='+',type=Path);args=p.parse_args()
icon=hashlib.sha256((root/'assets/videomenu/icon.png').read_bytes()).hexdigest()
results=[]
for file in args.files:
 with zipfile.ZipFile(file) as jar:
  config=jar.read('assets/evatexbackgroundvideo/discord.properties').decode()
  assert 'applicationId=1563256751623508028' in config,file
  assert hashlib.sha256(jar.read('assets/videomenu/icon.png')).hexdigest()==icon,file
  for name in ('DiscordActivityCoordinator','EvatexDiscord','EvatexDiscordPresence'):
   assert 'com/videomenu/'+name+'.class' in jar.namelist(),(file,name)
  for lang in ('en_us','tr_tr'):
   assert 'evatex.discord_activity' in json.loads(jar.read('assets/evatexbackgroundvideo/lang/'+lang+'.json')),file
  data=jar.read('com/videomenu/EvatexDiscordPresence.class')
  for value in (b'View on Modrinth',b'https://modrinth.com/mod/evatex-background',b'Using EVATEX Background',b'large_url',b'details_url'):
   assert value in data,(file,value)
 results.append(dict(file=file.name,publisher_application_id='1563256751623508028',icon_sha256=icon,status='discord-package-verified'))
 print('PASS Discord package',file.name,flush=True)
out=root/'build/discord-package-validation.json';out.write_text(json.dumps(results,indent=2)+'\n')
