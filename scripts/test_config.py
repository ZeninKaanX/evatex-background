"""Exercise both production config implementations in an isolated directory."""
import os,subprocess,tempfile
from pathlib import Path
ROOT=Path(__file__).resolve().parent.parent
jdk=Path(os.environ['JAVA_HOME'])/'bin'
cp=(ROOT/'build/classpath-1.21.11.txt').read_text()
for tree in ('src','src120'):
 with tempfile.TemporaryDirectory(prefix='evatex-config-') as directory:
  work=Path(directory);pkg=work/'com/videomenu';pkg.mkdir(parents=True)
  (pkg/'EvatexPlatform.java').write_text('package com.videomenu; public final class EvatexPlatform {public static java.nio.file.Path configDir(){return java.nio.file.Path.of(System.getProperty("evatex.test.dir"));}}')
  (pkg/'VideoMenuClient.java').write_text('package com.videomenu; public final class VideoMenuClient {public static final Log LOGGER=new Log(); public static class Log {public void error(String s,Throwable t){}}}')
  files=[*pkg.glob('*.java'),ROOT/tree/'com/videomenu/VideoMenuConfig.java',ROOT/'tests/ConfigRegression.java']
  subprocess.run([str(jdk/'javac'),'-proc:none','--release','17','-cp',cp,'-d',str(work),*map(str,files)],check=True)
  subprocess.run([str(jdk/'java'),f'-Devatex.test.dir={work}/config','-cp',str(work)+os.pathsep+cp,'ConfigRegression'],check=True)
