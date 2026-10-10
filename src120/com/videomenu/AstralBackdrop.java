package com.videomenu;
import net.minecraft.class_332;

/** Bounded decorative animation, active only while the video settings screen is visible. */
public final class AstralBackdrop {
    private static final int STARS = 48;
    private AstralBackdrop() {}
    public static void render(class_332 ctx, int width, int height, boolean videoVisible, boolean motion) {
        if (!videoVisible) ctx.method_25296(0,0,width,height,0xFF050505,0xFF181818);
        ctx.method_25294(0,0,width,height,0x78000000);
        double seconds = System.nanoTime()/1e9;
        for (int i=0; i<STARS; i++) {
            int x=(int)(((i*733+137)%997)/997.0*width);
            int y=(int)(((i*419+61)%991)/991.0*height);
            int alpha=motion && i%4==0 ? (int)(65+40*Math.sin(seconds*.65+i)) : 70;
            int size=i%13==0 ? 2 : 1;
            ctx.method_25294(x,y,x+size,y+size,alpha<<24|0xFFFFFF);
        }
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
            for (int segment=0; segment<16; segment++) {
                double tail=segment*3.5;
                int x=(int)(hx-tail), y=(int)(hy-tail);
                if (x+4<0 || y+4<0 || x>width || y>height) continue;
                int alpha=fade*(16-segment)/16;
                ctx.method_25294(x,y,x+4,y+4,alpha<<24|0xFFFFFF);
            }
            int x=(int)hx,y=(int)hy;
            ctx.method_25294(x-2,y-2,x+5,y+5,(fade/7)<<24|0xFFFFFF);
            ctx.method_25294(x,y,x+2,y+2,fade<<24|0xFFFFFF);
        }
    }
}
