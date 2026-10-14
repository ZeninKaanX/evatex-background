package com.videomenu.library;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import net.minecraft.class_2960;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_310;
import net.minecraft.class_332;

import com.mojang.blaze3d.systems.RenderSystem;

/** Static 768x432 images only; no decoder or animated hover cards. Lifetime follows the screen. */
public final class LibraryThumbnails implements AutoCloseable {
 private final Map<String,CompletableFuture<byte[]>> pending=new HashMap<>();
 private final Map<String,class_2960> textures=new LinkedHashMap<>(64,.75f,true);private final Set<String> failed=new HashSet<>();
 private final Map<String,Path> local=new ConcurrentHashMap<>();private volatile boolean closed;private volatile Process poster;
 private final ExecutorService posters=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"EVATEX-local-posters");t.setDaemon(true);return t;});
 public void localFiles(Map<LibraryCatalog.Entry,Path> files){files.forEach((entry,path)->local.put(entry.id(),path));}
 public String state(LibraryCatalog.Entry entry){return failed.contains(entry.id())?"preview_error":"loading";}
 public void retry(){failed.clear();}
 /** Keep only the visible page and detail card queued, even after rapid paging. */
 public void retainVisible(Set<String> visible){
  var iterator=pending.entrySet().iterator();
  while(iterator.hasNext()){var item=iterator.next();if(!visible.contains(item.getKey())){item.getValue().cancel(true);iterator.remove();}}
 }


 public boolean draw(class_332 ctx,LibraryCatalog.Entry e,int x,int y,int w,int h,boolean online){
  class_2960 id=textures.get(e.id());
  if(id==null&&!failed.contains(e.id())){
   var future=pending.get(e.id());
   if(future==null&&(local.containsKey(e.id())||e.builtin()||online||Files.isRegularFile(LibraryCatalog.CACHE.resolve(e.id()+"-thumb-768.png")))&&pending.size()<20){future=CompletableFuture.supplyAsync(()->load(e),local.containsKey(e.id())&&e.kind().equals("video")?posters:LibraryCatalog.WORKER);pending.put(e.id(),future);}
   if(future!=null&&future.isDone())try{
    byte[] png=future.join();var image=class_1011.method_4309(new ByteArrayInputStream(png));id=new class_2960("evatexbackgroundvideo","library/"+e.id());var tex=new ThumbnailTexture(image);class_310.method_1551().method_1531().method_4616(id,tex);textures.put(e.id(),id);
   }catch(Exception ex){failed.add(e.id());}finally{pending.remove(e.id());}
   while(textures.size()>48){var oldest=textures.keySet().iterator().next();class_310.method_1551().method_1531().method_4615(textures.remove(oldest));}
  }
  if(id==null)return false;
  double fit=Math.min(w/768.0,h/432.0);int dw=Math.max(1,(int)(768*fit)),dh=Math.max(1,(int)(432*fit));ctx.method_25294(x,y,x+w,y+h,0xFF101416);
  ctx.method_25293(id,x+(w-dw)/2,y+(h-dh)/2,dw,dh,0f,0f,768,432,768,432);return true;
 }
 private byte[] load(LibraryCatalog.Entry e){
  try{
   Files.createDirectories(LibraryCatalog.CACHE);Path file=LibraryCatalog.CACHE.resolve(e.id()+"-thumb-768.png");
   if(Files.isRegularFile(file)&&Files.size(file)<2*1024*1024)return Files.readAllBytes(file);
   BufferedImage original;
   if(closed)throw new IOException("Closed");
   Path source=local.get(e.id());
   if(source!=null&&e.kind().equals("video")){
    Path temporary=Files.createTempFile(LibraryCatalog.CACHE,"poster-",".png");
    try{Process process=com.videomenu.video.VideoDecoder.startPosterProcess(source,temporary);poster=process;
     long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(8);
     while(process.isAlive()&&!closed&&System.nanoTime()<deadline)Thread.sleep(100);
     if(process.isAlive()){process.destroyForcibly();throw new IOException("Poster timed out");}
     if(process.exitValue()!=0)throw new IOException("Poster unavailable");original=ImageIO.read(temporary.toFile());
    }finally{if(poster!=null&&poster.isAlive())poster.destroyForcibly();poster=null;Files.deleteIfExists(temporary);}
   }else if(source!=null)original=readImage(Files.newInputStream(source));
   else if(e.builtin())original=OriginalBackground.render(e,768,432);
   else{
    if(e.thumbnailUrl().isEmpty())throw new IOException("No thumbnail");byte[] bytes;
    try(var in=LibraryCatalog.open(e.thumbnailUrl())){bytes=in.readNBytes(8*1024*1024+1);if(bytes.length>8*1024*1024)throw new IOException("Thumbnail too large");}
    original=readImage(new ByteArrayInputStream(bytes));
   }
   var scaled=new BufferedImage(768,432,BufferedImage.TYPE_INT_RGB);var g=scaled.createGraphics();try{g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);double scale=Math.min(768.0/original.getWidth(),432.0/original.getHeight());int w=(int)(original.getWidth()*scale),h=(int)(original.getHeight()*scale);g.drawImage(original,(768-w)/2,(432-h)/2,w,h,null);}finally{g.dispose();original.flush();}
   var output=new ByteArrayOutputStream();ImageIO.write(scaled,"png",output);byte[] png=output.toByteArray();Path tmp=Files.createTempFile(LibraryCatalog.CACHE,"thumb-",".part");try{Files.write(tmp,png);Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING);}finally{Files.deleteIfExists(tmp);}return png;
  }catch(Exception ex){throw new CompletionException(ex);}
 }
 private static BufferedImage readImage(InputStream input)throws IOException{
  try(input;var stream=ImageIO.createImageInputStream(input)){
   var readers=ImageIO.getImageReaders(stream);if(!readers.hasNext())throw new IOException("Unsupported image");
   var reader=readers.next();try{reader.setInput(stream);int w=reader.getWidth(0),h=reader.getHeight(0);
    if(w<1||h<1||(long)w*h>100_000_000)throw new IOException("Image dimensions");
    var parameters=reader.getDefaultReadParam();int sample=Math.max(1,Math.min(w/768,h/432));parameters.setSourceSubsampling(sample,sample,0,0);return reader.read(0,parameters);
   }finally{reader.dispose();}
  }
 }
 @Override public void close(){closed=true;if(poster!=null)poster.destroyForcibly();posters.shutdownNow();for(var f:pending.values())if(!f.isDone())f.cancel(true);for(var id:textures.values())class_310.method_1551().method_1531().method_4615(id);textures.clear();pending.clear();}
 private static final class ThumbnailTexture extends class_1043{
  ThumbnailTexture(class_1011 image){super(image);method_4527(true,false);}
 }
}
