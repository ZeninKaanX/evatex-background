package com.videomenu.library;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.imageio.ImageIO;
import com.videomenu.EvatexPlatform;
/** One cancellable transfer, verified before it becomes visible in the local library. */
public final class LibraryDownloads {
 private static final ExecutorService DOWNLOAD_WORKER=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"EVATEX-download");t.setDaemon(true);return t;});
 public record Progress(long current,long total,String phase){}
 private volatile Progress progress=new Progress(0,0,"idle");private final AtomicBoolean cancel=new AtomicBoolean();private volatile CompletableFuture<Path> active;
 public Progress progress(){return progress;}public boolean busy(){return active!=null&&!active.isDone();}public void cancel(){cancel.set(true);}
 public Path target(LibraryCatalog.Entry e){String extension=e.builtin()?"png":e.fileName().matches("(?i).+\\.(png|jpe?g|bmp|gif|mp4|webm|mkv|mov|m4v|avi)")?e.fileName().substring(e.fileName().lastIndexOf('.')+1).toLowerCase(java.util.Locale.ROOT):e.kind().equals("image")?"png":e.kind().equals("gif")?"gif":"mp4";return EvatexPlatform.gameDir().resolve("backgrounds/evatex-library").resolve(e.id()+"."+extension);}
 public boolean installed(LibraryCatalog.Entry e){try{Path p=target(e);return Files.isRegularFile(p)&&Files.size(p)>0&&(e.builtin()||e.bytes()<=0||Files.size(p)==e.bytes());}catch(java.io.IOException ex){return false;}}
 public CompletableFuture<Path> download(LibraryCatalog.Entry e){
  if(!e.downloadable()||busy())throw new IllegalStateException("Content unavailable or transfer active");cancel.set(false);progress=new Progress(0,e.bytes(),"downloading");
  active=CompletableFuture.supplyAsync(()->{Path tmp=null;try{
   Path destination=target(e);Files.createDirectories(destination.getParent());tmp=Files.createTempFile(destination.getParent(),e.id()+"-",".part");
   if(e.builtin()){
    if(!ImageIO.write(OriginalBackground.render(e,1920,1080),"png",tmp.toFile()))throw new java.io.IOException("PNG encoder unavailable");
    progress=new Progress(Files.size(tmp),Files.size(tmp),"verifying");
   }else{
    var sha=MessageDigest.getInstance(e.pinnedPersonalDownload()?"SHA-1":"SHA-256");if(e.pinnedPersonalDownload())sha.update(("blob "+e.bytes()+"\0").getBytes(java.nio.charset.StandardCharsets.UTF_8));long count=0;
    try(var input=LibraryCatalog.open(e.downloadUrl());var output=Files.newOutputStream(tmp)){byte[] buffer=new byte[65536];int n;
     while((n=input.read(buffer))!=-1){if(cancel.get())throw new CancellationException();count+=n;if(count>e.bytes()||count>512L*1024*1024)throw new java.io.IOException("Unexpected size");sha.update(buffer,0,n);output.write(buffer,0,n);progress=new Progress(count,e.bytes(),"downloading");}
    }
    progress=new Progress(count,e.bytes(),"verifying");if(count!=e.bytes()||!HexFormat.of().formatHex(sha.digest()).equalsIgnoreCase(e.pinnedPersonalDownload()?e.gitBlobSha1():e.sha256()))throw new java.io.IOException("Download verification failed");
   }
   if(cancel.get())throw new CancellationException();try{Files.move(tmp,destination,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException ex){Files.move(tmp,destination,StandardCopyOption.REPLACE_EXISTING);}progress=new Progress(Files.size(destination),Files.size(destination),"ready");return destination;
  }catch(Exception ex){progress=new Progress(0,0,cancel.get()?"cancelled":"error");throw new CompletionException(ex);}finally{if(tmp!=null)try{Files.deleteIfExists(tmp);}catch(java.io.IOException ignored){}}},DOWNLOAD_WORKER);return active;
 }
}
