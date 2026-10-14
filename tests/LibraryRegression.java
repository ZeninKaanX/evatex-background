package com.videomenu.library;
import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
public final class LibraryRegression {
 static byte[] payload="Verified wallpaper payload".getBytes(java.nio.charset.StandardCharsets.UTF_8);
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 static void rejects(Runnable action){try{action.run();throw new AssertionError("Accepted malformed catalogue/address");}catch(IllegalArgumentException|IllegalStateException expected){}}
 static LibraryCatalog.Entry external(String id,String digest,long size){return new LibraryCatalog.Entry(id,id,"anime","video","https://example.com/source","","Owner","CC0","https://example.com/media",digest,size,1920,1080,false,true);}
 public static void main(String[] args)throws Exception {
  URL.setURLStreamHandlerFactory(protocol->protocol.equals("https")?new URLStreamHandler(){protected URLConnection openConnection(URL u){return new HttpURLConnection(u){public int getResponseCode(){return 200;}public InputStream getInputStream(){return new ByteArrayInputStream(payload);}public void disconnect(){}public boolean usingProxy(){return false;}public void connect(){}};}}:null);
  String catalogue=Files.readString(Path.of(args[0]));var entries=LibraryCatalog.parse(catalogue);
  check(entries.size()==254,"254 curated entries");check(entries.stream().filter(e->e.kind().equals("video")).count()>=4,"Direct MP4 videos are present");check(entries.stream().filter(LibraryCatalog.Entry::builtin).count()==13,"13 offline originals");check(entries.stream().allMatch(LibraryCatalog.Entry::downloadable),"All entries have direct downloads");
  check(entries.stream().filter(e->e.id().startsWith("community-")).allMatch(e->e.pinnedPersonalDownload()&&!e.redistributionAllowed()),"Personal collection downloads do not grant redistribution rights");
  check(entries.stream().filter(e->e.id().startsWith("nasa-")).allMatch(e->e.kind().equals("video")&&e.downloadUrl().endsWith(".mp4")&&e.sha256().matches("[a-f0-9]{64}")&&e.bytes()>0&&e.creator().contains("NASA")),"Credited MP4 files have exact size and SHA-256");
  rejects(()->LibraryCatalog.parse(catalogue.replace("\"schemaVersion\": 1","\"schemaVersion\": 2")));
  rejects(()->LibraryCatalog.parse("{\"schemaVersion\":1,\"entries\":[{\"id\":\"../escape\",\"category\":\"anime\",\"kind\":\"video\"}]}"));
  rejects(()->LibraryCatalog.parse("{\"schemaVersion\":1,\"entries\":[{\"id\":\"same\",\"category\":\"anime\",\"kind\":\"video\"},{\"id\":\"same\",\"category\":\"anime\",\"kind\":\"video\"}]}"));
  for(String bad:List.of("http://example.com/a","https://localhost/a","https://127.0.0.1/a","file:///tmp/a","https://user:password@example.com/a","https://example.com:8443/a"))rejects(()->LibraryCatalog.https(bad));
  var download=new LibraryDownloads();var good=external("verified",LibraryCatalog.digest(payload),payload.length);Path file=download.download(good).get(10,TimeUnit.SECONDS);check(Arrays.equals(payload,Files.readAllBytes(file)),"Verified payload preserved");
  for(var bad:List.of(external("hash-mismatch","0".repeat(64),payload.length),external("size-mismatch",LibraryCatalog.digest(payload),payload.length-1))){try{download.download(bad).get(10,TimeUnit.SECONDS);throw new AssertionError("Invalid download accepted");}catch(ExecutionException expected){}check(!Files.exists(download.target(bad)),"No invalid final file");}
  var blob=java.security.MessageDigest.getInstance("SHA-1");blob.update(("blob "+payload.length+"\0").getBytes(java.nio.charset.StandardCharsets.UTF_8));String blobHash=HexFormat.of().formatHex(blob.digest(payload));
  var personal=new LibraryCatalog.Entry("personal","Personal","anime","image","https://github.com/owner/repo","","Collection","Original artist rights","https://raw.githubusercontent.com/owner/repo/"+"a".repeat(40)+"/wall.jpg","",payload.length,1920,1080,false,false,blobHash,"wall.jpg");
  check(personal.downloadable()&&download.target(personal).toString().endsWith(".jpg"),"Pinned personal source keeps image extension");
  file=download.download(personal).get(10,TimeUnit.SECONDS);check(download.installed(personal)&&Arrays.equals(payload,Files.readAllBytes(file)),"Git blob verified personal download");
  var corrupted=new LibraryCatalog.Entry("corrupt-personal","Personal","anime","image",personal.sourceUrl(),"","","",personal.downloadUrl(),"",payload.length,0,0,false,false,"0".repeat(40),"wall.jpg");
  try{download.download(corrupted).get(10,TimeUnit.SECONDS);throw new AssertionError("Corrupt Git blob accepted");}catch(ExecutionException expected){}check(!download.installed(corrupted),"Rejected blob never installed");
  var original=entries.stream().filter(LibraryCatalog.Entry::builtin).findFirst().orElseThrow();file=download.download(original).get(20,TimeUnit.SECONDS);var image=ImageIO.read(file.toFile());check(image!=null&&image.getWidth()==1920&&image.getHeight()==1080,"Offline original dimensions/PNG");image.flush();
  download.download(original);download.cancel();try{download.download(good);throw new AssertionError("Overlapping transfer accepted");}catch(IllegalStateException expected){}while(download.busy())Thread.sleep(10);check(download.progress().phase().equals("cancelled"),"Cancellation observed");
  try(var files=Files.list(file.getParent())){check(files.noneMatch(p->p.toString().endsWith(".part")),"Partial files removed");}
  System.out.println("PASS library: 254 entries, pinned personal downloads and Git blob verification, schema/duplicate/path/address rejection, checksum/size validation, offline 1080p original, cancellation and partial cleanup");
 }
}
