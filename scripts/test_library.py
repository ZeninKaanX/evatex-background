#!/usr/bin/env python3
"""Exercise actual catalogue/download code in a temporary game directory, without Minecraft/GPU/audio."""
import os,subprocess,tempfile,json
from pathlib import Path
ROOT=Path(__file__).resolve().parent.parent
jdk=Path(os.environ['JAVA_HOME'])/'bin'
cp=(ROOT/'build/classpath-1.21.11.txt').read_text()
with tempfile.TemporaryDirectory(prefix='evatex-library-test-') as directory:
 work=Path(directory);pkg=work/'com/videomenu';pkg.mkdir(parents=True)
 (pkg/'EvatexPlatform.java').write_text('package com.videomenu; public final class EvatexPlatform {public static java.nio.file.Path gameDir(){return java.nio.file.Path.of(System.getProperty("evatex.test.dir"));}}')
 (pkg/'VideoMenuClient.java').write_text('package com.videomenu; public final class VideoMenuClient {public static final Log LOGGER=new Log(); public static class Log {public void warn(String s,Throwable t){throw new AssertionError(s,t);}}}')
 sources=[*pkg.glob('*.java'),*[ROOT/f'src/com/videomenu/library/{name}.java' for name in ('LibraryCatalog','LibraryDownloads','OriginalBackground')],ROOT/'tests/LibraryRegression.java']
 subprocess.run([str(jdk/'javac'),'-proc:none','--release','21','-cp',cp,'-d',str(work),*map(str,sources)],check=True)
 subprocess.run([str(jdk/'java'),f'-Devatex.test.dir={work}/game','-cp',str(work)+os.pathsep+cp,'com.videomenu.library.LibraryRegression',str(ROOT/'catalog/v1.json')],check=True)
# Exercise the real thumbnail queue without a display, GPU or media decoder.
with tempfile.TemporaryDirectory(prefix='evatex-preview-queue-') as directory:
 runtime=str(ROOT/'build/classes-1.21.11')+os.pathsep+cp
 subprocess.run([str(jdk/'javac'),'-proc:none','--release','21','-cp',runtime,'-d',directory,str(ROOT/'tests/ThumbnailQueueRegression.java')],check=True)
 subprocess.run([str(jdk/'java'),'-cp',directory+os.pathsep+runtime,'ThumbnailQueueRegression'],check=True)
assert (ROOT/'catalog/v1.json').read_bytes()==(ROOT/'assets/evatexbackgroundvideo/catalog/v1.json').read_bytes()
assert set(json.loads((ROOT/'assets/evatexbackgroundvideo/lang/en_us.json').read_text()))==set(json.loads((ROOT/'assets/evatexbackgroundvideo/lang/tr_tr.json').read_text()))
print('PASS embedded/remote seed parity and English/Turkish keys')
