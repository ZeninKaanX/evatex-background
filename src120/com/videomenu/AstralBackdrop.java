package com.videomenu;
import net.minecraft.class_332;

/** Bounded decorative animation, shared by the video settings and optional vanilla menu backdrops. */
public final class AstralBackdrop {
    private AstralBackdrop() {}
    public static void render(class_332 ctx, int width, int height, boolean videoVisible, boolean motion) {
        paint(ctx,width,height,videoVisible,motion,0x00000000);
    }
    public static void renderMenu(class_332 ctx, int width, int height, boolean motion) {
        paint(ctx,width,height,true,motion,0x30000000);
    }
    private static void paint(class_332 ctx, int width, int height, boolean videoVisible, boolean motion, int overlay) {
        if (!videoVisible) ctx.method_25296(0,0,width,height,0xFF050505,0xFF181818);
        ctx.method_25294(0,0,width,height,overlay);
        double seconds = System.nanoTime()/1e9;
        if (!motion) return;
        int count = Math.min(width,height)<280 ? 2 : 3;
        for (int i=0; i<count; i++) {
            double phase=(seconds+i*4.1)%13.0;
            if (phase>2.0) continue;
            double progress=phase/2.0;
            double startX=width*(.06+i*.24), startY=-height*(.08+i*.1);
            double distance=height*1.15;
            double hx=startX+distance*progress, hy=startY+distance*progress;
            int fade=(int)(170*Math.sin(progress*Math.PI));
            AstralTypography.meteor(ctx,(int)hx-41,(int)hy-41,fade<<24|0xFFFFFF);
        }
    }
}
