"""Create isolated, bounded Gradle projects for historical native loader families."""
import json,shutil
from pathlib import Path
ROOT=Path(__file__).resolve().parent.parent
TARGETS=json.loads((ROOT/'loaders/targets.json').read_text())
def target(loader,version):
    value=TARGETS.get(version,{}).get(loader)
    if not value:raise ValueError(f'{loader} has no published upstream target for Minecraft {version}')
    return value

def project(loader,version):
    lv=target(loader,version)
    if version=='1.21.11':return ROOT/'loaders'/loader
    folder=ROOT/'build/native-projects'/loader/version;folder.mkdir(parents=True,exist_ok=True)
    template=ROOT/'loaders'/loader
    for name in ('gradlew','gradlew.bat','settings.gradle','gradle.properties'):
        shutil.copyfile(template/name,folder/name)
    (folder/'gradlew').chmod(0o755)
    shutil.copytree(template/'gradle',folder/'gradle',dirs_exist_ok=True)
    props=folder/'gradle.properties';props.write_text('org.gradle.jvmargs=-Xmx1536m -XX:ActiveProcessorCount=2\norg.gradle.daemon=false\norg.gradle.parallel=false\norg.gradle.workers.max=1\norg.gradle.caching=true\n')
    java=25 if version.startswith('26.') else 17 if version in ('1.20','1.20.1','1.20.2','1.20.3','1.20.4') else 21
    common=f'''version = providers.gradleProperty('modVersion').get()
group = 'com.videomenu'
base.archivesName = 'evatex-background-{loader}-mc{version}'
java.toolchain.languageVersion = JavaLanguageVersion.of({java})
sourceSets.main.java.srcDirs = [layout.buildDirectory.dir('generated/sources')]
sourceSets.main.resources.srcDirs = [layout.buildDirectory.dir('generated/resources')]
tasks.withType(JavaCompile).configureEach {{ options.encoding='UTF-8' }}
tasks.named('jar') {{ manifest {{ attributes('Implementation-Version': project.version, 'MixinConfigs': 'videomenu.mixins.json') }} }}
'''
    if version in ('1.20','1.20.1'):
        settings=('neoForgeVersion' if loader=='neoforge' else 'forgeVersion')
        gradle=f'''plugins {{ id 'java-library'; id 'net.neoforged.moddev.legacyforge' version '2.0.148' }}
{common}
legacyForge {{
 enable {{ {settings} = '{version}-{lv}'; disableRecompilation = true }}
 mods {{ evatexbackgroundvideo {{ sourceSet sourceSets.main }} }}
 runs {{ client {{ client(); gameDirectory=file('run') }} }}
}}
repositories {{ mavenCentral(); maven {{ url='https://repo.spongepowered.org/repository/maven-public/' }} }}
dependencies {{ annotationProcessor 'org.spongepowered:mixin:0.8.5:processor' }}
mixin {{ add sourceSets.main, 'videomenu.refmap.json'; config 'videomenu.mixins.json' }}
'''
        # MDG legacy shares the modern NeoForge wrapper and supports Gradle 9.
        shutil.copytree(ROOT/'loaders/neoforge/gradle',folder/'gradle',dirs_exist_ok=True)
    elif loader=='neoforge' and version in ('1.20.2','1.20.3','1.20.5'):
        wrapper=folder/'gradle/wrapper/gradle-wrapper.properties';s=wrapper.read_text();import re
        wrapper.write_text(re.sub(r'distributionUrl=.*',r'distributionUrl=https\://services.gradle.org/distributions/gradle-8.14.3-bin.zip',s))
        props.write_text(props.read_text()+'neogradle.subsystems.decompiler.enabled=false\nneogradle.subsystems.decompiler.maxThreads=2\nneogradle.subsystems.decompiler.maxMemory=3g\nneogradle.subsystems.recompiler.maxMemory=1536m\n')
        gradle=f'''plugins {{ id 'java-library'; id 'net.neoforged.gradle.userdev' version '7.1.39' }}
{common}
repositories {{ mavenCentral() }}
dependencies {{ implementation 'net.neoforged:neoforge:{lv}' }}
runs {{ client {{ client(); modSource sourceSets.main }} }}
'''
    elif loader=='neoforge':
        gradle=f'''plugins {{ id 'java-library'; id 'net.neoforged.moddev' version '2.0.148' }}
{common}
neoForge {{ enable {{ version='{lv}'; disableRecompilation=true }}; mods {{ evatexbackgroundvideo {{ sourceSet sourceSets.main }} }}; runs {{ client {{ client(); gameDirectory=file('run') }} }} }}
'''
    elif version.startswith('26.'):
        gradle=(template/'build.gradle').read_text().replace("getOrElse('1.21.11')",f"getOrElse('{version}')").replace("getOrElse('61.2.3')",f"getOrElse('{lv}')").replace('JavaLanguageVersion.of(21)','JavaLanguageVersion.of(25)')
        # Minecraft 26.x ships named bytecode; its official client has no
        # separate mapping artifact. Match Forge's 26.x MDK configuration.
        gradle=gradle.replace("    mappings channel: 'official', version: mcVersion\n",'').replace("version '[7.0.3,8)'","version '[7.0.17,8)'")
    else:
        srg=version in ('1.20.2','1.20.3','1.20.4')
        wrapper=folder/'gradle/wrapper/gradle-wrapper.properties';s=wrapper.read_text();import re
        wrapper.write_text(re.sub(r'distributionUrl=.*',r'distributionUrl=https\://services.gradle.org/distributions/gradle-8.14.3-bin.zip',s))
        mixin_plugin="; id 'org.spongepowered.mixin' version '0.7-SNAPSHOT'" if srg else ''
        processor="; annotationProcessor 'org.spongepowered:mixin:0.8.5:processor'" if srg else ''
        gradle=f'''plugins {{ id 'java'; id 'net.minecraftforge.gradle' version '6.0.54'{mixin_plugin} }}
{common}
minecraft {{ mappings channel:'official',version:'{version}'; {'reobf=false;' if not srg else ''} runs {{ client {{ workingDirectory=file('run'); mods {{ evatexbackgroundvideo {{ source sourceSets.main }} }} }} }} }}
repositories {{ mavenCentral(); maven {{ url='https://repo.spongepowered.org/repository/maven-public/' }} }}
dependencies {{ minecraft 'net.minecraftforge:forge:{version}-{lv}'{processor} }}
'''
        if srg:gradle+="mixin { add sourceSets.main,'videomenu.refmap.json'; config 'videomenu.mixins.json' }\ntasks.named('jar').configure { finalizedBy 'reobfJar' }\n"
        settings=folder/'settings.gradle';s=settings.read_text().replace('gradlePluginPortal();','gradlePluginPortal(); maven { url="https://repo.spongepowered.org/repository/maven-public/" };');settings.write_text(s)
    game=json.loads((ROOT/'build/dependencies'/f'{version}.json').read_text())
    logging=[lib['name'] for lib in game['libraries'] if lib['name'].startswith('org.apache.logging.log4j:')]
    if logging:
        # Legacy loader dependencies request 2.11.+ and query unrelated Maven
        # mirrors. Pin to this game's official logging libraries instead.
        coordinates=','.join(repr(name) for name in logging)
        gradle+=f'\nconfigurations.configureEach {{ resolutionStrategy.force({coordinates}) }}\n'
    (folder/'build.gradle').write_text(gradle)
    return folder
