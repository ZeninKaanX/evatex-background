import java.awt.*;
import java.awt.image.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
/** Bake the contour once; runtime still uses one antialiased texture draw. */
public class GenerateWordmark {
 public static void main(String[] args)throws Exception {
  var font=Font.createFont(Font.TRUETYPE_FONT,Path.of(args[0]).toFile()).deriveFont(176f);
  var probe=new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);var graphics=probe.createGraphics();
  var shape=font.createGlyphVector(graphics.getFontRenderContext(),"EVATEX").getOutline();graphics.dispose();
  var bounds=shape.getBounds2D();int pad=12;
  var image=new BufferedImage((int)Math.ceil(bounds.getWidth())+pad*2,(int)Math.ceil(bounds.getHeight())+pad*2,BufferedImage.TYPE_INT_ARGB);
  graphics=image.createGraphics();graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
  graphics.translate(pad-bounds.getX(),pad-bounds.getY());
  graphics.setColor(new Color(16,16,20,210));graphics.setStroke(new BasicStroke(3.2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));graphics.draw(shape);
  graphics.setColor(Color.WHITE);graphics.fill(shape);graphics.dispose();
  ImageIO.write(image,"png",Path.of(args[1]).toFile());
  System.out.println(image.getWidth()+"x"+image.getHeight());
 }
}
