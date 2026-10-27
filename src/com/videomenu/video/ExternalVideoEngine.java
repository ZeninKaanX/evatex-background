package com.videomenu.video;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.LongConsumer;
import java.util.zip.*;

/** Opt-in, pinned external FFmpeg for distributions that cannot include executables. */
public final class ExternalVideoEngine {
    private static final String ROOT="assets/evatexbackgroundvideo/";
    private static final long LIMIT=300L*1024*1024;
    private static final Set<String> HOSTS=Set.of("www.gyan.dev","github.com","release-assets.githubusercontent.com","objects.githubusercontent.com");
    private static final ExecutorService WORKER=Executors.newSingleThreadExecutor(r->{var t=new Thread(r,"EVATEX-Engine-Setup");t.setDaemon(true);return t;});
    private static final Set<Path> VERIFIED=ConcurrentHashMap.newKeySet();
    private static CompletableFuture<Path> active;
    public static volatile String state="idle";
    public static volatile long downloaded;
    private ExternalVideoEngine() {}
    public static boolean externalDistribution(){return ExternalVideoEngine.class.getClassLoader().getResource(ROOT+"external-engine.properties")!=null;}
    public static String platform(String os,String arch){
        String name=os.toLowerCase(Locale.ROOT),cpu=arch.toLowerCase(Locale.ROOT);
        boolean arm=cpu.equals("aarch64")||cpu.equals("arm64"),x64=cpu.equals("amd64")||cpu.equals("x86_64");
        if(name.startsWith("windows"))return x64?"win64":null;
        if(name.contains("mac")||name.contains("darwin"))return arm?"darwin-arm64":x64?"darwin-x64":null;
        if(name.contains("linux"))return arm?"linux-arm64":x64?"linux64":null;
        return null;
    }
    private static String platform(){return platform(System.getProperty("os.name",""),System.getProperty("os.arch",""));}
    private static JsonObject entry() throws IOException {
        String platform=platform();if(platform==null)throw new IOException("Unsupported video engine platform");
        try(InputStream in=ExternalVideoEngine.class.getClassLoader().getResourceAsStream(ROOT+"engine-manifest.json")){
            if(in==null)throw new IOException("Missing video engine manifest");
            JsonObject object=JsonParser.parseString(new String(in.readAllBytes(),StandardCharsets.UTF_8)).getAsJsonObject();
            if(!object.has(platform))throw new IOException("Unsupported video engine platform");return object.getAsJsonObject(platform);
        }
    }
    public static Path folder(Path game){return game.resolve(".evatex-ffmpeg").resolve("bin");}
    private static String name(){return System.getProperty("os.name","").toLowerCase(Locale.ROOT).startsWith("windows")?"ffmpeg.exe":"ffmpeg";}
    private static Path cached(Path game,JsonObject entry){return game.resolve(".evatex-ffmpeg/engines").resolve(platform()).resolve(entry.get("binary_sha256").getAsString()).resolve(name());}
    private static Path local(Path game){
        Path manual=folder(game).resolve(name());if(usable(manual))return manual;
        String path=System.getenv("PATH");if(path!=null)for(String dir:path.split(java.util.regex.Pattern.quote(File.pathSeparator))){
            if(dir.isBlank())continue;
            try{Path candidate=Path.of(dir).resolve(name());if(usable(candidate))return candidate;}catch(InvalidPathException ignored){}
        }
        return null;
    }
    private static boolean usable(Path file){return Files.isRegularFile(file)&&Files.isExecutable(file);}
    /** Presence check only; heavy integrity checks run on decoder/setup workers. */
    public static boolean needsSetup(Path game){
        if(!externalDistribution())return false;
        if(local(game)!=null)return false;
        try{return !Files.isRegularFile(cached(game,entry()));}catch(IOException|RuntimeException e){return true;}
    }
    public static Path resolve(Path game) throws IOException {
        JsonObject e;
        try{e=entry();}catch(IOException unsupported){Path installed=local(game);if(installed!=null)return installed;throw unsupported;}
        Path file=cached(game,e);
        if(usable(file)){
            if(VERIFIED.contains(file)||hash(file).equals(e.get("binary_sha256").getAsString())){VERIFIED.add(file);return file;}
            state="invalid";
        }
        Path installed=local(game);if(installed!=null)return installed;
        // Previously bundled engines may be reused only when their pinned hash matches.
        if(e.has("legacy_sha256")){
            Path legacy=game.resolve(".evatex-ffmpeg/1.8.0").resolve(platform()).resolve(name());
            if(usable(legacy)&&(VERIFIED.contains(legacy)||hash(legacy).equals(e.get("legacy_sha256").getAsString()))){VERIFIED.add(legacy);return legacy;}
        }
        throw new IOException("Video engine not installed. Open EVATEX Settings > Video engine.");
    }
    /** Called exclusively by the explicit Download button, never at mod startup. */
    public static synchronized CompletableFuture<Path> install(Path game){
        if(active!=null&&!active.isDone())return active;
        state="downloading";downloaded=0;
        active=CompletableFuture.supplyAsync(()->{
            try{
                JsonObject e=entry();Path result=cached(game,e);Files.createDirectories(result.getParent());
                if(usable(result)&&hash(result).equals(e.get("binary_sha256").getAsString())){VERIFIED.add(result);state="ready";return result;}
                install(game,e,uri->open(uri),n->downloaded=n);VERIFIED.add(result);state="ready";return result;
            }catch(Exception failure){state="failed";throw new CompletionException(failure);}
        },WORKER);return active;
    }
    @FunctionalInterface interface Source {InputStream open(URI uri)throws IOException;}
    static void install(Path game,JsonObject e,Source source,LongConsumer progress)throws IOException {
        URI uri=URI.create(e.get("url").getAsString());validate(uri);
        String expected=e.get("archive_sha256").getAsString(),binary=e.get("binary_sha256").getAsString();
        if(!expected.matches("[0-9a-f]{64}")||!binary.matches("[0-9a-f]{64}"))throw new IOException("Invalid engine hash");
        Path result=cached(game,e),dir=result.getParent();Files.createDirectories(dir);
        Path archive=Files.createTempFile(dir,"download-",".part"),extracted=null;
        try{
            try(InputStream in=source.open(uri);OutputStream out=Files.newOutputStream(archive)){copy(in,out,LIMIT,progress);}
            if(!hash(archive).equals(expected))throw new IOException("Video engine download checksum mismatch");
            extracted=Files.createTempFile(dir,"engine-",".part");
            if(e.get("format").getAsString().equals("gzip")){
                try(var in=new GZIPInputStream(Files.newInputStream(archive));var out=Files.newOutputStream(extracted)){copy(in,out,256L*1024*1024,n->{});}
            }else if(e.get("format").getAsString().equals("zip")){
                try(var zip=new ZipFile(archive.toFile())){
                    var entry=zip.getEntry(e.get("entry").getAsString());if(entry==null||entry.isDirectory())throw new IOException("Missing engine ZIP entry");
                    try(var in=zip.getInputStream(entry);var out=Files.newOutputStream(extracted)){copy(in,out,256L*1024*1024,n->{});}
                }
            }else throw new IOException("Unsupported engine archive");
            if(!hash(extracted).equals(binary))throw new IOException("Video engine binary checksum mismatch");
            if(!extracted.toFile().setExecutable(true,true)&&!Files.isExecutable(extracted))throw new IOException("Engine execution permission unavailable");
            try{Files.move(extracted,result,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
            catch(AtomicMoveNotSupportedException ex){Files.move(extracted,result,StandardCopyOption.REPLACE_EXISTING);}
        }finally{Files.deleteIfExists(archive);if(extracted!=null)Files.deleteIfExists(extracted);}
    }
    private static void copy(InputStream in,OutputStream out,long limit,LongConsumer progress)throws IOException {
        byte[] buffer=new byte[65536];int n;long total=0,until=System.nanoTime()+300_000_000_000L;
        while((n=in.read(buffer))!=-1){total+=n;if(total>limit||System.nanoTime()>until)throw new IOException("Engine download exceeds time/size limit");out.write(buffer,0,n);progress.accept(total);}
    }
    static void validate(URI uri)throws IOException {
        if(!"https".equals(uri.getScheme())||uri.getHost()==null||!HOSTS.contains(uri.getHost().toLowerCase(Locale.ROOT))||uri.getUserInfo()!=null||uri.getPort()!=-1&&uri.getPort()!=443)throw new IOException("Untrusted video engine URL");
    }
    private static InputStream open(URI uri)throws IOException {
        for(int i=0;i<6;i++){
            validate(uri);var c=(HttpURLConnection)uri.toURL().openConnection();c.setInstanceFollowRedirects(false);c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setRequestProperty("User-Agent","EVATEX-Video-Engine/1.8.26");
            int status=c.getResponseCode();
            if(Set.of(301,302,303,307,308).contains(status)){String next=c.getHeaderField("Location");c.disconnect();if(next==null)throw new IOException("Invalid engine redirect");uri=uri.resolve(next);continue;}
            if(status!=200){c.disconnect();throw new IOException("Video engine HTTP "+status);}
            if(c.getContentLengthLong()>LIMIT){c.disconnect();throw new IOException("Oversized video engine download");}
            return new FilterInputStream(c.getInputStream()){@Override public void close()throws IOException{try{super.close();}finally{c.disconnect();}}};
        }throw new IOException("Too many video engine redirects");
    }
    static String hash(Path file)throws IOException {
        try{var d=MessageDigest.getInstance("SHA-256");try(var in=Files.newInputStream(file)){byte[] b=new byte[65536];int n;while((n=in.read(b))!=-1)d.update(b,0,n);}return HexFormat.of().formatHex(d.digest());}
        catch(java.security.NoSuchAlgorithmException e){throw new AssertionError(e);}
    }
}
