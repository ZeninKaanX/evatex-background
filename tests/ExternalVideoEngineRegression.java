package com.videomenu.video;
import com.google.gson.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public final class ExternalVideoEngineRegression {
 static String hash(byte[] b)throws Exception{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(b));}
 static JsonObject manifest(byte[] archive,byte[] binary,String format)throws Exception {
  JsonObject e=new JsonObject();e.addProperty("url","https://www.gyan.dev/ffmpeg/builds/test.zip");e.addProperty("archive_sha256",hash(archive));e.addProperty("binary_sha256",hash(binary));e.addProperty("format",format);e.addProperty("entry","package/bin/ffmpeg.exe");return e;
 }
 static byte[] zip(byte[] binary)throws Exception {
  var b=new ByteArrayOutputStream();try(var z=new ZipOutputStream(b)){
   z.putNextEntry(new ZipEntry("../../escape.exe"));z.write(new byte[]{1});z.closeEntry();
   z.putNextEntry(new ZipEntry("package/bin/ffmpeg.exe"));z.write(binary);z.closeEntry();
  }return b.toByteArray();
 }
 static long files(Path dir,String suffix)throws Exception{try(var files=Files.walk(dir)){return files.filter(x->x.getFileName().toString().endsWith(suffix)).count();}}
 static void fails(Path dir,JsonObject e,ExternalVideoEngine.Source source)throws Exception{
  try{ExternalVideoEngine.install(dir,e,source,n->{});throw new AssertionError("Invalid download accepted");}catch(IOException expected){}
  if(files(dir,".part")!=0)throw new AssertionError("Partial download not removed");
 }
 public static void main(String[] args)throws Exception{
  if(!"win64".equals(ExternalVideoEngine.platform("Windows 11","amd64"))||!"darwin-arm64".equals(ExternalVideoEngine.platform("Darwin","aarch64"))||ExternalVideoEngine.platform("Windows 11","aarch64")!=null)throw new AssertionError("Wrong platform");
  for(String u:List.of("http://www.gyan.dev/a","https://127.0.0.1/a","https://www.gyan.dev.evil.test/a","https://github.com:444/a","https://user:pass@github.com/a")){
   try{ExternalVideoEngine.validate(java.net.URI.create(u));throw new AssertionError("Untrusted URL allowed");}catch(IOException expected){}
  }
  Path root=Files.createTempDirectory("evatex-engine-test-");
  try{
   byte[] binary="test-engine-payload".getBytes(java.nio.charset.StandardCharsets.UTF_8),archive=zip(binary);JsonObject e=manifest(archive,binary,"zip");
   ExternalVideoEngine.install(root,e,u->new ByteArrayInputStream(archive),n->{});
   Path result;try(var paths=Files.walk(root)){result=paths.filter(Files::isRegularFile).findFirst().orElseThrow();}
   if(!Arrays.equals(Files.readAllBytes(result),binary)||Files.exists(root.resolve("escape.exe")))throw new AssertionError("Wrong or unsafe ZIP extraction");
   JsonObject bad=e.deepCopy();bad.addProperty("archive_sha256","0".repeat(64));fails(root,bad,u->new ByteArrayInputStream(archive));
   bad=e.deepCopy();bad.addProperty("binary_sha256","0".repeat(64));fails(root,bad,u->new ByteArrayInputStream(archive));
   fails(root,e,u->new InputStream(){int reads;public int read()throws IOException{throw new IOException("network interrupted");}});
   byte[] existing=Files.readAllBytes(result);if(!Arrays.equals(existing,binary))throw new AssertionError("Failed retry damaged installed engine");
   var b=new ByteArrayOutputStream();try(var gzip=new GZIPOutputStream(b)){gzip.write(binary);}byte[] gz=b.toByteArray();
   ExternalVideoEngine.install(root,manifest(gz,binary,"gzip"),u->new ByteArrayInputStream(gz),n->{});
   if(files(root,".part")!=0)throw new AssertionError("Temporary files remain");
   System.out.println("PASS pinned ZIP/gzip engine, exact extraction, hostile URLs, checksum failures, interruption cleanup and previous engine preservation; no executable launched");
  }finally{try(var paths=Files.walk(root)){for(Path p:paths.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(p);}}
 }
}
