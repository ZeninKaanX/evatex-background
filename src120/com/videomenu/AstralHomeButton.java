package com.videomenu;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4185;

/** Large translucent tiles for the optional Astral home, with native input and narration. */
public final class AstralHomeButton extends class_4185 {
    private double hover;
    private long last;
    public AstralHomeButton(int x,int y,int width,int height,class_2561 text,class_4241 action) {
        super(x,y,width,height,text,action,field_40754);
    }
    @Override protected void method_48579(class_332 ctx,int mx,int my,float delta) {
        boolean focused=field_22762 || method_25370();
        long now=System.nanoTime();double dt=last==0 ? 0 : Math.min(.1,(now-last)/1e9);last=now;
        hover=AstralTheme.animated() ? hover+((focused ? 1 : 0)-hover)*(1-Math.exp(-dt*18)) : focused ? 1 : 0;
        int x=method_46426(),y=method_46427(),w=method_25368(),h=method_25364();
        int alpha=(int)(100+hover*70);
        ctx.method_25294(x,y,x+w,y+h,alpha<<24|0x101010);
        ctx.method_25294(x,y+h-1,x+w,y+h,((int)(65+hover*150))<<24|0xFFFFFF);
        var font=class_310.method_1551().field_1772;
        int color=field_22763 ? 0xFFF2F2F2 : 0xFF777777;
        ctx.method_25303(font,font.method_27523(method_25369().getString(),Math.max(0,w-32)),x+12,y+(h-font.field_2000)/2,color);
        ctx.method_25303(font,"›",x+w-16,y+(h-font.field_2000)/2,color);
    }
}
