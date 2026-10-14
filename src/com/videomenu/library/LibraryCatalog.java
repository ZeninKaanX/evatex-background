package com.videomenu.library;

import com.google.gson.*;
import com.videomenu.EvatexPlatform;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

/** A small catalogue, never bundled media; network work stays off Minecraft's render thread. */
public final class LibraryCatalog {
 public record Entry(String id,String title,String category,String kind,String sourceUrl,String thumbnailUrl,
                     String creator,String license,String downloadUrl,String sha256,long bytes,int width,int height,
                     boolean builtin,boolean redistributionAllowed) {
  public boolean downloadable(){return builtin || redistributionAllowed && !downloadUrl.isBlank() && sha256.matches("[a-fA-F0-9]{64}") && bytes>0 && bytes<=512L*1024*1024;}
 }
 public static final String DEFAULT_URL="https://raw.githubusercontent.com/ZeninKaanX/evatex-background-video-1.8.2/main/catalog/v1.json";
 public static final ExecutorService WORKER=Executors.newFixedThreadPool(2,r->{Thread t=new Thread(r,"EVATEX-library");t.setDaemon(true);return t;});
 public static final Path CACHE=EvatexPlatform.gameDir().resolve(".evatex-library");
 private static final Set<String> CATEGORIES=Set.of("anime","astral","nature","games","minimal");
 private LibraryCatalog(){}
 public static List<Entry> bundled(){
  try(var in=LibraryCatalog.class.getResourceAsStream("/assets/evatexbackgroundvideo/catalog/v1.json")){
   if(in==null)throw new IOException("Bundled catalogue missing");return parse(new String(in.readNBytes(1024*1024),StandardCharsets.UTF_8));
  }catch(Exception e){com.videomenu.VideoMenuClient.LOGGER.warn("Cannot load background catalogue",e);return List.of();}
 }
 public static List<Entry> parse(String text){
  var root=JsonParser.parseString(text).getAsJsonObject();if(root.get("schemaVersion").getAsInt()!=1)throw new IllegalArgumentException("Unsupported catalogue schema");
  var out=new ArrayList<Entry>();var ids=new HashSet<String>();var array=root.getAsJsonArray("entries");if(array.size()>1000)throw new IllegalArgumentException("Catalogue too large");
  for(var item:array){var o=item.getAsJsonObject();String id=s(o,"id"),category=s(o,"category"),kind=s(o,"kind");
   if(!id.matches("[a-z0-9-]{1,120}")||!ids.add(id)||!CATEGORIES.contains(category)||!Set.of("video","image","gif").contains(kind))throw new IllegalArgumentException("Invalid catalogue entry");
   String source=s(o,"sourceUrl"),thumb=s(o,"thumbnailUrl"),download=s(o,"downloadUrl");for(String url:List.of(source,thumb,download))if(!url.isEmpty())https(url);
   out.add(new Entry(id,s(o,"title"),category,kind,source,thumb,s(o,"creator"),s(o,"license"),download,s(o,"sha256"),o.has("bytes")?o.get("bytes").getAsLong():0,integer(o,"width"),integer(o,"height"),bool(o,"builtin"),bool(o,"redistributionAllowed")));
  }return List.copyOf(out);
 }
 private static String s(JsonObject o,String key){String value=o.has(key)?o.get(key).getAsString():"";if(value.length()>2048)throw new IllegalArgumentException("Catalogue text too long");return value;}
 private static boolean bool(JsonObject o,String k){return o.has(k)&&o.get(k).getAsBoolean();}
 private static int integer(JsonObject o,String k){return o.has(k)?Math.max(0,Math.min(16384,o.get(k).getAsInt())):0;}
 public static URI https(String value){
  URI u=URI.create(value);String host=u.getHost();
  if(!"https".equalsIgnoreCase(u.getScheme())||host==null||u.getUserInfo()!=null||u.getPort()!=-1&&u.getPort()!=443||host.equalsIgnoreCase("localhost")||host.matches("[0-9.:]+"))throw new IllegalArgumentException("Expected a public HTTPS address");return u;
 }
 public static InputStream open(String value)throws IOException{
  URI uri=https(value);
  for(int n=0;n<5;n++){
   for(var address:InetAddress.getAllByName(uri.getHost()))if(address.isAnyLocalAddress()||address.isLoopbackAddress()||address.isLinkLocalAddress()||address.isSiteLocalAddress()||address.isMulticastAddress()||(address.getAddress().length==16&&(address.getAddress()[0]&0xfe)==0xfc))throw new IOException("Private network address refused");
   var c=(HttpURLConnection)uri.toURL().openConnection();c.setConnectTimeout(10000);c.setReadTimeout(15000);c.setInstanceFollowRedirects(false);c.setRequestProperty("User-Agent","EVATEX-Background/library");int code=c.getResponseCode();
   if(code>=300&&code<400){String loc=c.getHeaderField("Location");c.disconnect();if(loc==null)throw new IOException("Redirect missing");uri=https(uri.resolve(loc).toString());continue;}
   if(code!=200){c.disconnect();throw new IOException("HTTP "+code);}
   return new FilterInputStream(c.getInputStream()){@Override public void close()throws IOException{try{super.close();}finally{c.disconnect();}}};
  }throw new IOException("Too many redirects");
 }
 public static String digest(byte[] bytes)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}
 public static CompletableFuture<List<Entry>> refresh(String address){return CompletableFuture.supplyAsync(()->{
  try(var input=open(address)){byte[] bytes=input.readNBytes(1024*1024+1);if(bytes.length>1024*1024)throw new IOException("Catalogue too large");var list=parse(new String(bytes,StandardCharsets.UTF_8));Files.createDirectories(CACHE);Path tmp=Files.createTempFile(CACHE,"catalog-",".part");try{Files.write(tmp,bytes);Files.move(tmp,CACHE.resolve("catalog.json"),StandardCopyOption.REPLACE_EXISTING);}finally{Files.deleteIfExists(tmp);}return list;}catch(Exception e){throw new CompletionException(e);}
 },WORKER);}
 public static List<Entry> initial(){try{Path file=CACHE.resolve("catalog.json");if(Files.isRegularFile(file)&&Files.size(file)<=1024*1024)return parse(Files.readString(file));}catch(Exception ignored){}return bundled();}
}
