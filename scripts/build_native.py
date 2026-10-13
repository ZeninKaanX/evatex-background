#!/usr/bin/env python3
"""Generate native Forge/NeoForge sources from EVATEX's shared version adapters."""
import argparse,json,os,re,shutil,subprocess,zipfile,hashlib
from build import ROOT,CACHE,MOD_VERSION,fetch,metadata
from ports import sources
from named_port import symbols,translate

def method_selectors(cache):
    """Keep precise intermediary overload selectors when translating to Mojang names."""
    mapping=symbols(cache)
    classes={}
    for line in (cache/'1.21.11-mojang-mappings.txt').read_text().splitlines():
        if line and not line[0].isspace() and ' -> ' in line and line.endswith(':'):
            named,obf=line[:-1].split(' -> ');classes[obf]=named.replace('.','/')
    candidates={}
    for line in (cache/'1.21.11.tiny').read_text().splitlines():
        row=line.split('\t')
        if len(row)>=5 and row[0]=='' and row[1]=='m' and row[4] in mapping:
            desc=re.sub(r'L([^;]+);',lambda m:'L'+classes.get(m[1],m[1])+';',row[2])
            candidates.setdefault(row[4],set()).add(mapping[row[4]]+desc)
    return {k:next(iter(v)) for k,v in candidates.items() if len(v)==1}

def precise_injections(content,selectors):
    def annotation(match):
        return re.sub(r'"(method_\d+)"',lambda m:'"'+selectors.get(m[1],m[1])+'"',match[0])
    return re.sub(r'method\s*=\s*(?:"[^"\n]*"|\{[^}]*\})',annotation,content)

def generate(loader,version,qa=False):
    if version!='1.21.11':raise ValueError('Native adapter currently targets Minecraft 1.21.11')
    manifest=metadata('https://piston-meta.mojang.com/mc/game/version_manifest_v2.json','manifest.json')
    entry=next(item for item in manifest['versions'] if item['id']==version)
    game=metadata(entry['url'],version+'.json')
    reference=game['downloads']['client_mappings']
    fetch(reference['url'],CACHE/(version+'-mojang-mappings.txt'),reference['sha1'])
    tiny_jar=fetch(f'https://maven.fabricmc.net/net/fabricmc/intermediary/{version}/intermediary-{version}-v2.jar',CACHE/(version+'-mappings.jar'))
    with zipfile.ZipFile(tiny_jar) as archive:(CACHE/(version+'.tiny')).write_bytes(archive.read('mappings/mappings.tiny'))
    project=ROOT/'loaders'/loader
    target=project/'build/generated/sources'
    if target.exists():shutil.rmtree(target)
    target.mkdir(parents=True)
    source,mixin_file=sources(ROOT,version)
    mapping=symbols(CACHE)
    selectors=method_selectors(CACHE)
    for path in source.rglob('*.java'):
        relative=path.relative_to(source)
        if path.name in ('ModMenuBridge.java','VideoMixinPlugin.java') or 'atlas' in relative.parts:continue
        content=translate(precise_injections(path.read_text(),selectors),mapping)
        content=re.sub(r"\bcomp_\d+\b",lambda m:mapping.get(m[0],m[0]),content)
        if path.name=='VideoMenuClient.java':
            content=content.replace('import net.fabricmc.api.ClientModInitializer;','').replace(' implements ClientModInitializer','').replace('@Override public void onInitializeClient()', 'public void onInitializeClient()')
        output=target/relative;output.parent.mkdir(parents=True,exist_ok=True);output.write_text(content)
    package=target/'com/videomenu'
    ns='net.neoforged' if loader=='neoforge' else 'net.minecraftforge'
    registry=ns+'.fml.ModList'
    (package/'EvatexPlatform.java').write_text('''package com.videomenu;
import java.nio.file.Path;
import %s;
import %s.fml.loading.FMLPaths;
/** Native loader implementation; shared playback has no Fabric dependency. */
public final class EvatexPlatform {
 private EvatexPlatform() {}
 public static Path gameDir(){return FMLPaths.GAMEDIR.get();}
 public static Path configDir(){return FMLPaths.CONFIGDIR.get();}
 public static String modVersion(String id){
  var mods=ModList.get();
  return mods==null ? "unavailable" : mods.getModContainerById(id).map(mod->mod.getModInfo().getVersion().toString()).orElse("unavailable");
 }
 public static int modCount(){var mods=ModList.get();return mods==null ? 0 : mods.getMods().size();}
 public static String loaderName(){return "%s";}
 public static String loaderVersion(){return modVersion("%s");}
}
'''%(registry,ns,('NeoForge' if loader=='neoforge' else 'Forge'),loader))
    if loader=='neoforge':
        entry='''package com.videomenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
@Mod(value="evatexbackgroundvideo",dist=Dist.CLIENT)
public final class EvatexNative {
 public EvatexNative(IEventBus bus,ModContainer container){
  bus.addListener(this::setup);
  container.registerExtensionPoint(IConfigScreenFactory.class,(mod,parent)->new VideoSelectScreen(parent,0));
 }
 private void setup(FMLClientSetupEvent event){event.enqueueWork(()->new VideoMenuClient().onInitializeClient());}
}
'''
    else:
        entry='''package com.videomenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
@Mod("evatexbackgroundvideo")
public final class EvatexNative {
 public EvatexNative(FMLJavaModLoadingContext context){
  if(FMLEnvironment.dist==Dist.CLIENT)Client.setup(context);
 }
 private static final class Client {
  static void setup(FMLJavaModLoadingContext context){
   context.getContainer().registerExtensionPoint(net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
     ()->new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory(parent->new VideoSelectScreen(parent,0)));
   FMLClientSetupEvent.getBus(context.getModBusGroup()).addListener(event->event.enqueueWork(()->new VideoMenuClient().onInitializeClient()));
  }
 }
}
'''
    (package/'EvatexNative.java').write_text(entry)
    resource=project/'build/generated/resources'
    if resource.exists():shutil.rmtree(resource)
    resource.mkdir(parents=True,exist_ok=True)
    for p in (ROOT/'assets').rglob('*'):
        if p.is_file() and 'ffmpeg' not in p.parts:
            dest=resource/'assets'/p.relative_to(ROOT/'assets');dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(p,dest)
    mixins=json.loads(mixin_file.read_text());mixins['client']=[x for x in mixins['client'] if not x.startswith('atlas.')];mixins.pop('plugin',None)
    (resource/'videomenu.mixins.json').write_text(json.dumps(mixins,indent=2)+'\n')
    if qa:
        engine=ROOT/'assets/videomenu/ffmpeg/linux64/ffmpeg'
        if engine.is_file():
            dest=resource/'assets/videomenu/ffmpeg/linux64/ffmpeg';dest.parent.mkdir(parents=True,exist_ok=True);os.link(engine,dest)
        for fixture in (ROOT/'tests/native').glob('*.java'):
            dest=target/'evatexqa'/fixture.name;dest.parent.mkdir(exist_ok=True);shutil.copyfile(fixture,dest)
        # QA uses its own package because the production mixin package is fixed.
        qa_mixins={'required':True,'package':'evatexqa','compatibilityLevel':'JAVA_21','client':['NativeSmokeMixin','SecondaryWindowMixin'],'injectors':{'defaultRequire':1}}
        (resource/'evatexqa.mixins.json').write_text(json.dumps(qa_mixins))
    # Forge/NeoForge include resources natively; no Fabric resource-loader modules.
    (resource/'pack.mcmeta').write_text(json.dumps({'pack':{'description':'EVATEX client resources','pack_format':75,'supported_formats':[75,75],'min_format':75,'max_format':75}}))
    meta=resource/'META-INF';meta.mkdir(exist_ok=True)
    toml='''modLoader="javafml"
loaderVersion="%s"
license="MIT"
issueTrackerURL="https://github.com/ZeninKaanX/evatex-background-video-1.8.2/issues"
[[mods]]
modId="evatexbackgroundvideo"
version="%s"
displayName="EVATEX-Background"
displayURL="https://modrinth.com/mod/evatex-background"
authors="EverVerity"
logoFile="assets/videomenu/icon.png"
description="Video, GIF and photo backgrounds with the optional Astral interface."
displayTest="IGNORE_ALL_VERSION"
[[dependencies.evatexbackgroundvideo]]
modId="minecraft"
%s
versionRange="[%s]"
ordering="NONE"
side="CLIENT"
[[dependencies.evatexbackgroundvideo]]
modId="%s"
%s
versionRange="%s"
ordering="NONE"
side="CLIENT"
'''%(('[1,)' if loader=='neoforge' else '[61,)'),MOD_VERSION,('type="required"' if loader=='neoforge' else 'mandatory=true'),version,loader,('type="required"' if loader=='neoforge' else 'mandatory=true'),('[21.11.45,)' if loader=='neoforge' else '[61.2.0,)'))
    if loader=='neoforge':
        toml+='\n[[mixins]]\nconfig="videomenu.mixins.json"\n'
        if qa:toml+='\n[[mixins]]\nconfig="evatexqa.mixins.json"\n'
    (meta/('neoforge.mods.toml' if loader=='neoforge' else 'mods.toml')).write_text(toml)
    shutil.copyfile(ROOT/'LICENSE',resource/'LICENSE')
    return project

def build_native(loader,version,prepare_only=False):
    project=generate(loader,version)
    if prepare_only:return
    subprocess.run([str(project/'gradlew'),'-p',str(project),'build','--console','plain','-PmodVersion='+MOD_VERSION,'-PminecraftVersion='+version],check=True)
    artifact=project/'build/libs'/f'evatex-background-{loader}-mc{version}-{MOD_VERSION}.jar'
    with zipfile.ZipFile(artifact) as archive:
        if any(name.startswith('evatexqa/') or name=='evatexqa.mixins.json' for name in archive.namelist()):
            raise RuntimeError('QA helpers must never enter a release jar')
    output=ROOT/'build/dist'/f'evatex-background-{MOD_VERSION}-mc{version}-{loader}.jar'
    # Copy the cached ZIP records: do not recompress bundled engines per loader.
    asset_files=[file for file in sorted((ROOT/'assets').rglob('*')) if file.is_file()]
    digest=hashlib.sha256()
    for file in asset_files:
        digest.update(str(file.relative_to(ROOT)).encode());digest.update(str(file.stat().st_mtime_ns).encode());digest.update(str(file.stat().st_size).encode())
    resource_cache=ROOT/'build'/('resources-'+digest.hexdigest()+'.zip')
    if not resource_cache.exists():
        temporary=resource_cache.with_name(resource_cache.name+f'.{os.getpid()}.tmp')
        with zipfile.ZipFile(temporary,'w',zipfile.ZIP_DEFLATED) as z:
            for file in asset_files:z.write(file,file.relative_to(ROOT),compress_type=zipfile.ZIP_STORED if file.suffix=='.gz' else zipfile.ZIP_DEFLATED)
        temporary.replace(resource_cache)
    output.parent.mkdir(parents=True,exist_ok=True)
    shutil.copyfile(resource_cache,output)
    with zipfile.ZipFile(output,'a',zipfile.ZIP_DEFLATED) as z,zipfile.ZipFile(artifact) as native:
        for item in native.infolist():
            if not item.filename.startswith('assets/'):z.writestr(item,native.read(item.filename))
    with output.open('rb') as stream:checksum=hashlib.file_digest(stream,'sha256').hexdigest()
    output.with_suffix('.jar.sha256').write_text(checksum+'  '+output.name+'\n')
    print('Built native',output,flush=True)

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--loader',choices=('forge','neoforge'),required=True);parser.add_argument('--minecraft',default='1.21.11');parser.add_argument('--prepare-only',action='store_true');args=parser.parse_args()
    build_native(args.loader,args.minecraft,args.prepare_only)
