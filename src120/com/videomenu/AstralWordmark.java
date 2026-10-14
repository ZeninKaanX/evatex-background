package com.videomenu;
import net.minecraft.class_2960;import net.minecraft.class_1011;import net.minecraft.class_1043;import net.minecraft.class_310;import net.minecraft.class_332;

/** One bounded antialiased wordmark texture, sampled independently of the glyph atlas. */
public final class AstralWordmark {
 private static final class_2960 ID=new class_2960("evatexbackgroundvideo","wordmark");
 private static int W,H;private static boolean loaded,failed;
 private static final class Texture extends class_1043{
 Texture(class_1011 image){super(image);method_4527(true,false);}
 }
 public static boolean draw(class_332 ctx,int x,int y,int targetHeight){
  if(failed)return false;
  try{
   if(!loaded){try(var in=AstralWordmark.class.getResourceAsStream("/assets/evatexbackgroundvideo/textures/wordmark.png")){
    if(in==null)throw new java.io.IOException("Missing wordmark");var image=class_1011.method_4309(in);W=image.method_4307();H=image.method_4323();class_310.method_1551().method_1531().method_4616(ID,new Texture(image));loaded=true;
   }}
   int h=Math.max(1,targetHeight),w=Math.max(1,Math.round((float)W*h/H));ctx.method_25293(ID,x,y,w,h,0f,0f,W,H,W,H);return true;
  }catch(Exception e){failed=true;VideoMenuClient.LOGGER.warn("EVATEX wordmark unavailable",e);return false;}
 }
}
