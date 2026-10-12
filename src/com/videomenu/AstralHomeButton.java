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
    @Override protected void method_75752(class_332 ctx,int mx,int my,float delta) {
        boolean focused=field_22762 || method_25370();
        long now=System.nanoTime();double dt=last==0 ? 0 : Math.min(.1,(now-last)/1e9);last=now;
        hover=AstralTheme.animated() ? hover+((focused ? 1 : 0)-hover)*(1-Math.exp(-dt*18)) : focused ? 1 : 0;
        int x=method_46426(),y=method_46427(),w=method_25368(),h=method_25364();
        int alpha=(int)(36+hover*35);
        ctx.method_25294(x,y,x+w,y+h,alpha<<24|0x101010);
        ctx.method_25294(x,y+h-1,x+w,y+h,((int)(65+hover*150))<<24|0xFFFFFF);
        var font=class_310.method_1551().field_1772;
        int color=field_22763 ? 0xFFF2F2F2 : 0xFF777777;
        String label=method_25369().getString();
        boolean compact=h<=24;
        int icon=-1;
        if(compact) {
            if(label.equals(class_2561.method_43471("menu.options").getString()))icon=1;
            else if(label.equals(class_2561.method_43471("menu.quit").getString()))icon=2;
            else if(label.equals(class_2561.method_43471("evatex.title").getString()))icon=0;
            else if(label.equals(class_2561.method_43471("evatex.home.more").getString()))icon=1;
            if(icon==2)label=class_2561.method_43471("evatex.home.quit").getString();
            if(label.endsWith("..."))label=label.substring(0,label.length()-3);
        }
        float scale=1f;
        int inset=icon<0 ? 8 : 16;
        int available=Math.max(0,w-inset-(compact ? 4 : 22));
        if(compact && AstralTypography.width(label)>available)scale=Math.max(.9f,(float)available/Math.max(1,AstralTypography.width(label)));
        while(AstralTypography.width(label)*scale>available && label.length()>1)label=label.substring(0,label.length()-1);
        int textY=y+(h-(int)(9*scale))/2;
        if(icon>=0)AstralDraw.homeIcon(ctx,icon,x+5,y+(h-9)/2,0xFFCCCCCC);
        AstralDraw.displayText(ctx,label,x+inset,textY,scale,color);
        if(!compact)AstralDraw.displayText(ctx,"→",x+w-14,textY,1f,color);
    }
}
