"""Translate only symbols used by EVATEX from cached Mojang/intermediary mappings.
Mappings are development inputs, never included in distribution jars.
"""
from collections import defaultdict
import re

def symbols(cache):
    lines=(cache/'1.21.11-mojang-mappings.txt').read_text().splitlines()
    classes={}
    for line in lines:
        if line and not line[0].isspace() and ' -> ' in line and line.endswith(':'):
            named,obf=line[:-1].split(' -> ');classes[named]=obf
    def desc(t):
        dims=0
        while t.endswith('[]'): dims+=1;t=t[:-2]
        return '['*dims+({'void':'V','boolean':'Z','byte':'B','short':'S','char':'C','int':'I','long':'J','float':'F','double':'D'}.get(t) or 'L'+classes.get(t,t).replace('.','/')+';')
    members={};owner=None
    for line in lines:
        if line.startswith('#'): continue
        if not line.startswith(' ') and ' -> ' in line and line.endswith(':'):
            owner=line[:-1].split(' -> ')[1];continue
        if not owner or ' -> ' not in line: continue
        left,obf=line.strip().split(' -> ')
        left=re.sub(r'^\d+:\d+:','',left)
        if '(' in left:
            match=re.match(r'(\S+) (\S+)\((.*?)\)',left)
            if not match: continue
            typ,name,args=match.groups()
            signature='('+''.join(desc(t) for t in args.split(',') if t)+')'+desc(typ)
        else:
            typ,name=left.split();signature=desc(typ)
        members[(owner,obf,signature)]=name
    result={}; candidates=defaultdict(set);owner=None
    reverse={v:k for k,v in classes.items()}
    for line in (cache/'1.21.11.tiny').read_text().splitlines()[1:]:
        row=line.split('\t')
        if row[0]=='c':
            owner=row[1]
            if owner in reverse: result[row[2].split('/')[-1]]=reverse[owner].replace('$','.')
        elif row[0]=='' and row[1] in ('f','m'):
            _,kind,signature,obf,inter=row[:5]
            if (owner,obf,signature) in members: candidates[inter].add(members[(owner,obf,signature)])
    for inter,names in candidates.items():
        if len(names)==1: result[inter]=next(iter(names))
    for key,value in list(result.items()):
        if "$class_" in key: result[key.split("$")[-1]]=value.split(".")[-1]
    return result

def translate(content,mapping):
    for inter,named in mapping.items():
        if inter.startswith('class_'):
            content=content.replace('Lnet/minecraft/'+inter+';', 'L'+named.replace('.','/')+';')
    content=re.sub(r'\b(class_\d+(?:\$class_\d+)?|method_\d+|field_\d+)\b',lambda m:mapping.get(m[0],m[0]),content)
    content=content.replace('net.minecraft.com.mojang.','com.mojang.').replace('net.minecraft.net.minecraft.','net.minecraft.')
    return content
