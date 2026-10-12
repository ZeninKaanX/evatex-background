package evatextest;
import com.videomenu.AstralDraw;
import net.minecraft.*;
/** Test-only fixed-background comparison; never distributed. */
public final class FontProbeScreen extends class_437 {
    public FontProbeScreen(){super(class_2561.method_43470("Font sampling probe"));}
    @Override public void method_25394(class_332 ctx,int mx,int my,float delta){
        ctx.method_25294(0,0,field_22789,field_22790,0xFF202020);
        int y=24;
        for(int density:new int[]{1,2,3,4,8,16}){
            try{
                var style=com.videomenu.AstralTypography.class.getDeclaredMethod("fontStyle",String.class);style.setAccessible(true);
                var label=class_2561.method_43470("Density "+density+"  Options / Backgrounds / Görünüm / Çözünürlük 1080p").method_10862((class_2583)style.invoke(null,"ui/probe"+density));
                ctx.method_51439(field_22787.field_1772,label,24,y,0xFFFFFFFF,false);
            }catch(Exception e){throw new RuntimeException(e);}
            y+=36;
        }
        AstralDraw.displayText(ctx,"EVATEX",24,y+8,4.5f,0xFFFFFFFF);
    }
}
