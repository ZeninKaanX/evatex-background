package com.videomenu;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;

/** Quiet fallback using native text, no media, custom fonts, sounds or animations. */
public final class EvatexRecoveryScreen extends class_437 {
    private boolean copied;
    public EvatexRecoveryScreen() { super(label("title")); }
    private static class_2561 label(String key) { return class_2561.method_43471("evatex.recovery."+key); }
    @Override protected void method_25426() {
        int panelWidth=Math.min(280,field_22789-32),x=(field_22789-panelWidth)/2;
        int y=Math.min(field_22790-59,field_22790/2+40);
        method_37063(new SafeButton(x,y,panelWidth,label("continue"),button->method_25419()));
        method_37063(new SafeButton(x,y+25,panelWidth,label("copy"),button->{
            try { field_22787.field_1774.method_1455(EvatexRecovery.diagnostics());copied=true; }
            catch(RuntimeException failure) {VideoMenuClient.LOGGER.warn("Cannot copy EVATEX diagnostics",failure);}
        }));
    }
    @Override public void method_25419() { EvatexRecovery.resume(field_22787); }
    @Override public void method_25394(class_332 ctx,int mx,int my,float delta) {
        ctx.method_25294(0,0,field_22789,field_22790,0xFF0D0F12);
        int panelWidth=Math.min(330,field_22789-32),x=(field_22789-panelWidth)/2;
        int top=Math.max(16,field_22790/2-68);
        draw(ctx,class_2561.method_43470("EVATEX"),x,top,0xFFF0F1F3);
        ctx.method_25294(x,top+19,x+28,top+20,0xFF9299A3);
        draw(ctx,label("title"),x,top+32,0xFFF0F1F3);
        draw(ctx,label("body"),x,top+49,0xFFAEB3BB);
        draw(ctx,label("saved"),x,top+65,0xFFAEB3BB);
        if(copied)draw(ctx,label("copied"),x,top+81,0xFFAEB3BB);
        super.method_25394(ctx,mx,my,delta);
    }
    private void draw(class_332 ctx,class_2561 text,int x,int y,int color) {
        String value=text.getString();int maxWidth=Math.min(330,field_22789-32);
        while(!value.isEmpty()&&field_22793.method_1727(value)>maxWidth)value=value.substring(0,value.offsetByCodePoints(value.length(),-1));
        ctx.method_51439(field_22793,class_2561.method_43470(value),x,y,color,false);
    }
    private static final class SafeButton extends class_4185 {
        SafeButton(int x,int y,int width,class_2561 text,class_4241 action) {super(x,y,width,20,text,action,field_40754);}
        @Override protected void method_48579(class_332 ctx,int mx,int my,float delta) {
            int x=method_46426(),y=method_46427(),w=method_25368();
            boolean focus=field_22762||method_25370();
            ctx.method_25294(x,y,x+w,y+20,focus?0xFF383D45:0xFF24282E);
            ctx.method_25294(x,y+19,x+w,y+20,focus?0xFFE1E4E9:0xFF656D78);
            var font=class_310.method_1551().field_1772;
            ctx.method_51439(font,method_25369(),x+(w-font.method_27525(method_25369()))/2,y+6,0xFFE8EAEE,false);
        }
    }
}
