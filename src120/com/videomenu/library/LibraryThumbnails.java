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



/** Static 384x216 images only; no decoder or animated hover cards. Lifetime follows the screen. */
public final class LibraryThumbnails implements AutoCloseable {
 private final Map<String,CompletableFuture<byte[]>> pending=new HashMap<>();private final Map<String,class_2960> textures=new HashMap<>();private final Set<String> failed=new HashSet<>();
 public boolean draw(class_332 ctx,LibraryCatalog.Entry e,int x,int y,int w,int h,boolean online){
  class_2960 id=textures.get(e.id());
  if(id==null&&!failed.contains(e.id())){
   var future=pending.get(e.id());
   if(future==null&&(e.builtin()||online||Files.isRegularFile(LibraryCatalog.CACHE.resolve(e.id()+"-thumb.png")))&&pending.size()<64){future=CompletableFuture.supplyAsync(()->load(e),LibraryCatalog.WORKER);pending.put(e.id(),future);}
   if(future!=null&&future.isDone())try{
    byte[] png=future.join();var image=class_1011.method_4309(new ByteArrayInputStream(png));id=new class_2960("evatexbackgroundvideo","library/"+e.id());var tex=new ThumbnailTexture(image);class_310.method_1551().method_1531().method_4616(id,tex);textures.put(e.id(),id);
   }catch(Exception ex){failed.add(e.id());}
  }
  if(id==null)return false;
  double fit=Math.min(w/384.0,h/216.0);int dw=Math.max(1,(int)(384*fit)),dh=Math.max(1,(int)(216*fit));ctx.method_25294(x,y,x+w,y+h,0xFF101416);
  ctx.method_25293(id,x+(w-dw)/2,y+(h-dh)/2,dw,dh,0f,0f,384,216,384,216);return true;
 }
 private static byte[] load(LibraryCatalog.Entry e){
  try{
   Files.createDirectories(LibraryCatalog.CACHE);Path file=LibraryCatalog.CACHE.resolve(e.id()+"-thumb.png");
   if(Files.isRegularFile(file)&&Files.size(file)<2*1024*1024)return Files.readAllBytes(file);
   BufferedImage original;
   if(e.builtin())original=OriginalBackground.render(e,384,216);
   else{
    if(e.thumbnailUrl().isEmpty())throw new IOException("No thumbnail");byte[] bytes;
    try(var in=LibraryCatalog.open(e.thumbnailUrl())){bytes=in.readNBytes(4*1024*1024+1);if(bytes.length>4*1024*1024)throw new IOException("Thumbnail too large");}
    try(var stream=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))){var readers=ImageIO.getImageReaders(stream);if(!readers.hasNext())throw new IOException("Unsupported thumbnail");var reader=readers.next();try{reader.setInput(stream);int width=reader.getWidth(0),height=reader.getHeight(0);if(width<1||height<1||(long)width*height>16_000_000)throw new IOException("Thumbnail dimensions");original=reader.read(0);}finally{reader.dispose();}}
   }
   var scaled=new BufferedImage(384,216,BufferedImage.TYPE_INT_RGB);var g=scaled.createGraphics();try{g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);double scale=Math.min(384.0/original.getWidth(),216.0/original.getHeight());int w=(int)(original.getWidth()*scale),h=(int)(original.getHeight()*scale);g.drawImage(original,(384-w)/2,(216-h)/2,w,h,null);}finally{g.dispose();original.flush();}
   var output=new ByteArrayOutputStream();ImageIO.write(scaled,"png",output);byte[] png=output.toByteArray();Path tmp=Files.createTempFile(LibraryCatalog.CACHE,"thumb-",".part");try{Files.write(tmp,png);Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING);}finally{Files.deleteIfExists(tmp);}return png;
  }catch(Exception ex){throw new CompletionException(ex);}
 }
 @Override public void close(){for(var f:pending.values())if(!f.isDone())f.cancel(true);for(var id:textures.values())class_310.method_1551().method_1531().method_4615(id);textures.clear();pending.clear();}
 private static final class ThumbnailTexture extends class_1043{
  ThumbnailTexture(class_1011 image){super(image);method_4527(true,false);}
 }
}
