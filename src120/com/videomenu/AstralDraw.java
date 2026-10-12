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
    public static void homeIcon(net.minecraft.class_332 ctx,int type,int x,int y,int color) {
        if(type==2) { // Power: an open ring and stem.
            ctx.method_25294(x+3,y,x+4,y+4,color);
            ctx.method_25294(x,y+2,x+1,y+6,color);ctx.method_25294(x+6,y+2,x+7,y+6,color);
            ctx.method_25294(x+1,y+6,x+6,y+7,color);
        } else if(type==1) { // Settings gear, with a clear central opening.
            ctx.method_25294(x+2,y,x+5,y+7,color);ctx.method_25294(x,y+2,x+7,y+5,color);
            ctx.method_25294(x+2,y+2,x+5,y+5,0xFF888888);
        } else { // Background folder.
            ctx.method_25294(x,y,x+3,y+1,color);ctx.method_25294(x,y+1,x+7,y+2,color);
            ctx.method_25294(x,y+2,x+1,y+6,color);ctx.method_25294(x+6,y+2,x+7,y+6,color);
            ctx.method_25294(x,y+6,x+7,y+7,color);
        }
    }
    public static void displayText(net.minecraft.class_332 ctx,String text,int x,int y,float scale,int color) {
        var matrix=ctx.method_51448();matrix.method_22903();
        try {
            matrix.method_22904(x,y,0);matrix.method_22905(scale,scale,scale);
            AstralTypography.draw(ctx,text,0,0,color);
        } finally {matrix.method_22909();}
    }
}
