package com.videomenu.library;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
/** EVATEX originals are rendered once to a local PNG, without network access or a decoder. */
public final class OriginalBackground {
 private OriginalBackground(){}
 public static BufferedImage render(LibraryCatalog.Entry e,int w,int h){
  var image=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);int[] p=((DataBufferInt)image.getRaster().getDataBuffer()).getData();int variant=Math.floorMod(e.id().hashCode(),13);
  double angle=variant*.19,cs=Math.cos(angle),sn=Math.sin(angle);
  for(int y=0;y<h;y++)for(int x=0;x<w;x++){
   double nx=(x-w*.5)/h,ny=(y-h*.5)/h,xx=nx*cs-ny*sn,yy=nx*sn+ny*cs,r=Math.hypot(xx,yy);double v;
   if(e.category().equals("astral")){
    double ellipse=Math.hypot(xx,yy*(2.6+variant*.1)),ring=Math.exp(-Math.pow((ellipse-.27)*31,2));
    double halo=Math.exp(-Math.pow((r-.24)*8,2))*.24,stream=Math.exp(-Math.pow((yy-.035*Math.sin(xx*12))*38,2))*Math.exp(-xx*xx*1.6);
    v=.035+halo+ring*(.48+.22*Math.sin(Math.atan2(yy,xx)*3+variant))+stream*.16;
    if(r<.155)v*=Math.max(.015,Math.pow(r/.155,12));
   }else{
    double a=Math.sin(xx*(3+variant*.6)+Math.sin(yy*4)*.8),waves=Math.sin(yy*9+a*2),glow=Math.exp(-r*r*1.9);
    v=.06+glow*.15+(waves*.5+.5)*.20+(a*.5+.5)*.12;
   }
   int c=(int)Math.max(0,Math.min(255,v*255));p[y*w+x]=(c<<16)|(c<<8)|c;
  }return image;
 }
}
