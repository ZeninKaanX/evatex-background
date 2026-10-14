import java.awt.*;
import java.awt.image.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
/** Bake soft lettering shadows once; runtime still uses one antialiased texture draw. */
public class GenerateWordmark {
 public static void main(String[] args)throws Exception {
  var font=Font.createFont(Font.TRUETYPE_FONT,Path.of(args[0]).toFile()).deriveFont(176f);
  var probe=new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);var graphics=probe.createGraphics();
  var shape=font.createGlyphVector(graphics.getFontRenderContext(),"EVATEX").getOutline();graphics.dispose();
  var bounds=shape.getBounds2D();int pad=12;
  int width=(int)Math.ceil(bounds.getWidth())+pad*2,height=(int)Math.ceil(bounds.getHeight())+pad*2;
  var shadow=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
  graphics=shadow.createGraphics();graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
  graphics.translate(pad-bounds.getX()+1.25,pad-bounds.getY()+2.5);
  graphics.setColor(new Color(20,22,27,145));graphics.fill(shape);graphics.dispose();
  // A separable Gaussian falloff avoids a hard outline on bright wallpapers.
  int radius=5;float[] weights=new float[radius*2+1];double sum=0;
  for(int i=-radius;i<=radius;i++){weights[i+radius]=(float)Math.exp(-i*i/(2*1.8*1.8));sum+=weights[i+radius];}
  for(int i=0;i<weights.length;i++)weights[i]/=(float)sum;
  shadow=new ConvolveOp(new Kernel(weights.length,1,weights),ConvolveOp.EDGE_ZERO_FILL,null).filter(shadow,null);
  shadow=new ConvolveOp(new Kernel(1,weights.length,weights),ConvolveOp.EDGE_ZERO_FILL,null).filter(shadow,null);
  var image=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
  graphics=image.createGraphics();graphics.drawImage(shadow,0,0,null);
  graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
  graphics.translate(pad-bounds.getX(),pad-bounds.getY());
  graphics.setColor(Color.WHITE);graphics.fill(shape);graphics.dispose();
  ImageIO.write(image,"png",Path.of(args[1]).toFile());
  System.out.println(image.getWidth()+"x"+image.getHeight());
 }
}
