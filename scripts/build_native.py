#!/usr/bin/env python3
"""Generate native Forge/NeoForge sources from EVATEX's shared version adapters."""
import argparse,json,os,re,shutil,subprocess,zipfile,hashlib
from pathlib import Path
from build import ROOT,CACHE,MOD_VERSION,fetch,metadata
from ports import sources
from native_projects import project as native_project, target as native_target
from named_port import symbols,translate

def method_selectors(cache,version="1.21.11"):
    """Keep precise intermediary overload selectors when translating to Mojang names."""
    mapping=symbols(cache,version)
    classes={}
    for line in (cache/(version+'-mojang-mappings.txt')).read_text().splitlines():
        if line and not line[0].isspace() and ' -> ' in line and line.endswith(':'):
            named,obf=line[:-1].split(' -> ');classes[obf]=named.replace('.','/')
    candidates={}
    for line in (cache/(version+'.tiny')).read_text().splitlines():
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
    loader_version=native_target(loader,version)
    legacy_namespace = version in ('1.20','1.20.1')
    java_version = 25 if version.startswith('26.') else 17 if version in ('1.20','1.20.1','1.20.2','1.20.3','1.20.4') else 21
    manifest=metadata('https://piston-meta.mojang.com/mc/game/version_manifest_v2.json','manifest.json')
    entry=next(item for item in manifest['versions'] if item['id']==version)
    game=metadata(entry['url'],version+'.json')
    if not version.startswith('26.'):
        reference=game['downloads']['client_mappings']
        fetch(reference['url'],CACHE/(version+'-mojang-mappings.txt'),reference['sha1'])
        tiny_jar=fetch(f'https://maven.fabricmc.net/net/fabricmc/intermediary/{version}/intermediary-{version}-v2.jar',CACHE/(version+'-mappings.jar'))
        with zipfile.ZipFile(tiny_jar) as archive:(CACHE/(version+'.tiny')).write_bytes(archive.read('mappings/mappings.tiny'))
    project=native_project(loader,version)
    target=project/'build/generated/sources'
    if target.exists():shutil.rmtree(target)
    target.mkdir(parents=True)
    source,mixin_file=sources(ROOT,version)
    mapping={} if version.startswith("26.") else symbols(CACHE,version)
    selectors={} if version.startswith("26.") else method_selectors(CACHE,version)
    for path in source.rglob('*.java'):
        relative=path.relative_to(source)
        if path.name in ('ModMenuBridge.java','VideoMixinPlugin.java') or 'atlas' in relative.parts:continue
        content=translate(precise_injections(path.read_text(),selectors),mapping)
        content=re.sub(r"\bcomp_\d+\b",lambda m:mapping.get(m[0],m[0]),content)
        if path.name=='VideoMenuClient.java':
            content=content.replace('import net.fabricmc.api.ClientModInitializer;','').replace(' implements ClientModInitializer','').replace('@Override public void onInitializeClient()', 'public void onInitializeClient()')
        # Shared Fabric mixins carry both pre/post-1.21 drawString signatures.
        # The native AP must see only the signature that exists for this target.
        if path.name=='AstralMenuTextMixin.java' and version in ('1.20','1.20.1','1.20.2','1.20.3','1.20.4','1.20.5','1.20.6','1.21','1.21.1','1.21.2','1.21.3','1.21.4'):
            content=re.sub(r'    @Inject\(method="drawString[^"\n]+\)V"[^\n]*\n    private void evatex\$text[^\n]*\{.*?\n    \}\n','',content,flags=re.S)
        if path.name=='MenuShortcutMixin.java' and (legacy_namespace or loader=='forge' and version in ('1.20.2','1.20.3','1.20.4')):
            content=content.replace('@Shadow protected abstract', '@Shadow(remap=false,aliases={"m_142416_"}) protected abstract')
        output=target/relative;output.parent.mkdir(parents=True,exist_ok=True);output.write_text(content)
    package=target/'com/videomenu'
    ns='net.neoforged' if loader=='neoforge' and not legacy_namespace else 'net.minecraftforge'
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
'''%(registry,ns,('NeoForge' if loader=='neoforge' else 'Forge'),('forge' if legacy_namespace else loader)))
    if loader=='neoforge' and not legacy_namespace:
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
    if loader=='neoforge' and java_version==17 and not legacy_namespace:
        entry=entry.replace(',dist=Dist.CLIENT','')
        entry=entry.replace('net.neoforged.neoforge.client.gui.IConfigScreenFactory','net.neoforged.neoforge.client.ConfigScreenHandler.ConfigScreenFactory')
        entry=entry.replace('IConfigScreenFactory.class,(mod,parent)->new VideoSelectScreen(parent,0)', 'ConfigScreenFactory.class,()->new ConfigScreenFactory((minecraft,parent)->new VideoSelectScreen(parent,0))')
        entry=entry.replace('public EvatexNative(IEventBus bus,ModContainer container)', 'public EvatexNative(IEventBus bus)').replace('  bus.addListener(this::setup);','  if(net.neoforged.fml.loading.FMLEnvironment.dist!=Dist.CLIENT)return;\n  ModContainer container=net.neoforged.fml.ModLoadingContext.get().getActiveContainer();\n  bus.addListener(this::setup);\n  ')
    if loader=='neoforge' and version=='1.20.5':
        entry=entry.replace(',dist=Dist.CLIENT','').replace('  bus.addListener(this::setup);','  if(net.neoforged.fml.loading.FMLEnvironment.dist!=Dist.CLIENT)return;\n  bus.addListener(this::setup);')
    if loader=='forge' and not version.startswith('26.') and version!='1.21.11' or legacy_namespace:
        entry=entry.replace('public EvatexNative(FMLJavaModLoadingContext context)', 'public EvatexNative()').replace('  if(FMLEnvironment.dist==Dist.CLIENT)Client.setup(context);','  if(FMLEnvironment.dist==Dist.CLIENT)Client.setup(FMLJavaModLoadingContext.get());')
        entry=entry.replace('context.getContainer()', 'net.minecraftforge.fml.ModLoadingContext.get()')
        entry=entry.replace('ConfigScreenFactory(parent->new VideoSelectScreen(parent,0))','ConfigScreenFactory((minecraft,parent)->new VideoSelectScreen(parent,0))')
        entry=entry.replace('FMLClientSetupEvent.getBus(context.getModBusGroup()).addListener(event->event.enqueueWork(()->new VideoMenuClient().onInitializeClient()));','context.getModEventBus().addListener((FMLClientSetupEvent event)->event.enqueueWork(()->new VideoMenuClient().onInitializeClient()));')
    (package/'EvatexNative.java').write_text(entry)
    resource=project/'build/generated/resources'
    if resource.exists():shutil.rmtree(resource)
    resource.mkdir(parents=True,exist_ok=True)
    for p in (ROOT/'assets').rglob('*'):
        if p.is_file() and 'ffmpeg' not in p.parts:
            dest=resource/'assets'/p.relative_to(ROOT/'assets');dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(p,dest)
    mixins=json.loads(mixin_file.read_text());mixins['client']=[x for x in mixins['client'] if not x.startswith('atlas.')];mixins.pop('plugin',None)
    if legacy_namespace or loader=='forge' and version in ('1.20.2','1.20.3','1.20.4'):mixins['refmap']='videomenu.refmap.json'
    (resource/'videomenu.mixins.json').write_text(json.dumps(mixins,indent=2)+'\n')
    if qa:
        engine=ROOT/'assets/videomenu/ffmpeg/linux64/ffmpeg'
        if engine.is_file():
            dest=resource/'assets/videomenu/ffmpeg/linux64/ffmpeg';dest.parent.mkdir(parents=True,exist_ok=True);os.link(engine,dest)
        for fixture in (ROOT/'tests/native').glob('*.java'):
            dest=target/'evatexqa'/fixture.name;dest.parent.mkdir(exist_ok=True)
            content=fixture.read_text()
            if fixture.name=='NativeSmokeMixin.java' and not version.startswith('26.'):
                content=content.replace('net.minecraft.client.gui.screens.options.OptionsScreen',mapping.get('class_429','net.minecraft.client.gui.screens.options.OptionsScreen'))
            if version in ('1.20','1.20.1','1.20.2','1.20.3','1.20.4','1.20.5','1.20.6','1.21','1.21.1','1.21.2','1.21.3','1.21.4'):
                content=content.replace('.handle()', '.getWindow()')
                content=content.replace('net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget(),image->{','try(var image=net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget())){')
                content=content.replace('finally{image.close();}\n  });','finally{}\n  }')
            if fixture.name=='NativeSmokeMixin.java' and (legacy_namespace or java_version==17):
                content=content.replace('EvatexPlatform.loaderName().equals("NeoForge")',str(loader=='neoforge' and not legacy_namespace).lower())
                content=content.replace('ns+".neoforge.client.gui.IConfigScreenFactory"','ns+".neoforge.client.ConfigScreenHandler"')
                content=content.replace('neo ? "getForMod" : "getScreenFactoryFor"','"getScreenFactoryFor"')
                content=content.replace('if(neo)mc.setScreen((Screen)api.getMethod("createScreen",Class.forName(ns+".fml.ModContainer"),Screen.class).invoke(factory,container,mc.screen));\n     else ', '')
            dest.write_text(content)
        # QA uses its own package because the production mixin package is fixed.
        qa_mixins={'required':True,'package':'evatexqa','compatibilityLevel':'JAVA_'+str(java_version),'client':['NativeSmokeMixin','SecondaryWindowMixin'],'injectors':{'defaultRequire':1}}
        (resource/'evatexqa.mixins.json').write_text(json.dumps(qa_mixins))
        if legacy_namespace or loader=='forge' and version!='1.21.11' and not version.startswith('26.'):
            script=project/'build.gradle'
            addition="\ntasks.named('jar') { manifest { attributes('MixinConfigs':'videomenu.mixins.json,evatexqa.mixins.json') } }\n"
            if legacy_namespace:addition+="mixin { config 'evatexqa.mixins.json' }\n"
            else:addition+="minecraft.runs.client.args '--mixin','evatexqa.mixins.json'\n"
            script.write_text(script.read_text()+addition)
    # Forge/NeoForge include resources natively; no Fabric resource-loader modules.
    pack_formats={'1.20':15,'1.20.1':15,'1.20.2':18,'1.20.3':22,'1.20.4':22,'1.20.5':32,'1.20.6':32,'1.21':34,'1.21.1':34,'1.21.2':42,'1.21.3':42,'1.21.4':46,'1.21.5':55,'1.21.6':63,'1.21.7':64,'1.21.8':64,'1.21.9':69,'1.21.10':69,'1.21.11':75,'26.1':84,'26.1.1':84,'26.1.2':84,'26.2':88,'26.3':97}
    pf=pack_formats[version]
    format_bound=[97,1] if version=='26.3' else pf
    (resource/'pack.mcmeta').write_text(json.dumps({'pack':{'description':'EVATEX client resources','pack_format':pf,'supported_formats':[pf,pf],'min_format':format_bound,'max_format':format_bound}}))
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
'''%(('[1,)' if loader=='neoforge' and not legacy_namespace else '['+loader_version.split('.')[0]+',)'),MOD_VERSION,('type="required"' if loader=='neoforge' and not legacy_namespace else 'mandatory=true'),version,('forge' if legacy_namespace else loader),('type="required"' if loader=='neoforge' and not legacy_namespace else 'mandatory=true'),('['+loader_version+',)'))
    if loader=='neoforge' and not legacy_namespace:
        toml+='\n[[mixins]]\nconfig="videomenu.mixins.json"\n'
        if qa:toml+='\n[[mixins]]\nconfig="evatexqa.mixins.json"\n'
    (meta/('neoforge.mods.toml' if loader=='neoforge' and not legacy_namespace and version not in ('1.20.2','1.20.3','1.20.4','1.20.5') else 'mods.toml')).write_text(toml)
    shutil.copyfile(ROOT/'LICENSE',resource/'LICENSE')
    return project

def build_native(loader,version,prepare_only=False,compile_only=False):
    project=generate(loader,version)
    if prepare_only:return
    env=os.environ.copy()
    if (loader=='forge' and version not in ('1.20','1.20.1','1.21.11') and not version.startswith('26.')) or (loader=='neoforge' and version in ('1.20.2','1.20.3','1.20.5')):
        # Gradle 8's embedded Groovy cannot run on Java 25. The game toolchain
        # remains version-specific; only the Gradle launcher uses Java 21.
        candidates=[Path(env.get('EVATEX_GRADLE_JAVA21','/nonexistent'))]
        candidates.extend((Path.home()/'.gradle/jdks').glob('*21*'))
        candidates.extend(Path('/usr/lib/jvm').glob('*21*'))
        jdk=next((p for p in candidates if (p/'bin/java').exists()),None)
        if jdk is not None:env['JAVA_HOME']=str(jdk)
    subprocess.run([str(project/'gradlew'),'-p',str(project),'build','--console','plain','-PmodVersion='+MOD_VERSION,'-PminecraftVersion='+version],check=True,env=env)
    artifact=project/'build/libs'/f'evatex-background-{loader}-mc{version}-{MOD_VERSION}.jar'
    with zipfile.ZipFile(artifact) as archive:
        if any(name.startswith('evatexqa/') or name=='evatexqa.mixins.json' for name in archive.namelist()):
            raise RuntimeError('QA helpers must never enter a release jar')
    if compile_only:
        print('Compiled development artifact (media engines are not bundled):',artifact,flush=True)
        return
    package_native(loader,version,artifact)

def package_native(loader,version,artifact):
    """Bundle a separately compiled native artifact without rerunning Gradle."""
    with zipfile.ZipFile(artifact) as archive:
        names=archive.namelist()
        if 'fabric.mod.json' in names or any(name.startswith('evatexqa/') or name=='evatexqa.mixins.json' for name in names):
            raise RuntimeError('Invalid native release input')
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
    parser=argparse.ArgumentParser();parser.add_argument('--loader',choices=('forge','neoforge'),required=True);parser.add_argument('--minecraft',default='1.21.11');parser.add_argument('--prepare-only',action='store_true');parser.add_argument('--compile-only',action='store_true',help='CI development JAR only; does not create a distributable bundled release');parser.add_argument('--package-existing',type=Path,help='Bundle a verified native development artifact without local compilation');args=parser.parse_args()
    if args.package_existing:package_native(args.loader,args.minecraft,args.package_existing)
    else:build_native(args.loader,args.minecraft,args.prepare_only,args.compile_only)
