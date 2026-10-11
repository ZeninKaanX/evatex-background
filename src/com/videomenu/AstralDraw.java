package com.videomenu;
import net.minecraft.class_332;
/** Small integer geometry; no extra textures, shaders or per-frame collections. */
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
    public static void displayText(net.minecraft.class_332 ctx,String text,int x,int y,float scale,int color) {
        var matrix=ctx.method_51448();matrix.pushMatrix();
        try {
            matrix.translate((float)x,(float)y);matrix.scale(scale,scale);
            ctx.method_25303(net.minecraft.class_310.method_1551().field_1772,text,0,0,color);
        } finally {matrix.popMatrix();}
    }
}
