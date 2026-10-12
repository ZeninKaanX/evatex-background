package com.videomenu;
import net.minecraft.class_332;
/** Simple panel geometry and cached font icons; no custom shaders or per-frame geometry. */
public final class AstralDraw {
    private AstralDraw() {}
    public static void rounded(class_332 ctx, int x, int y, int width, int height, int color) {
        if (width<6 || height<6) { ctx.method_25294(x,y,x+width,y+height,color); return; }
        ctx.method_25294(x+3,y,x+width-3,y+1,color);
        ctx.method_25294(x+1,y+1,x+width-1,y+3,color);
        ctx.method_25294(x,y+3,x+width,y+height-3,color);
        ctx.method_25294(x+1,y+height-3,x+width-1,y+height-1,color);
        ctx.method_25294(x+3,y+height-1,x+width-3,y+height,color);
    }
    public static void homeIcon(net.minecraft.class_332 ctx,int type,int x,int y,int color) {
        AstralTypography.icon(ctx,type,x,y,color);
    }
    public static void displayText(net.minecraft.class_332 ctx,String text,int x,int y,float scale,int color) {
        var matrix=ctx.method_51448();matrix.pushMatrix();
        try {
            matrix.translate((float)x,(float)y);matrix.scale(scale,scale);
            // Match raster density to physical lettering size; avoid undersampling dense small glyphs.
            AstralTypography.drawScaled(ctx,text,0,0,color,scale);
        } finally {matrix.popMatrix();}
    }
}
