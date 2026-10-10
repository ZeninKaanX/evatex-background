package com.videomenu;
import java.util.function.DoubleConsumer;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_357;
/** Astral styling with native slider dragging, keyboard focus and narration. */
public final class AstralVolumeSlider extends class_357 {
    private final DoubleConsumer changed;
    public AstralVolumeSlider(int x,int y,int width,int height,double value,DoubleConsumer changed) {
        super(x,y,width,height,class_2561.method_43473(),value);this.changed=changed;method_25346();
    }
    @Override protected void method_25346() {method_25355(class_2561.method_43469("evatex.volume",Math.round(field_22753*100)));}
    @Override protected void method_25344() {if(changed!=null)changed.accept(Math.round(field_22753*100)/100.0);}
    @Override public void method_48579(class_332 context,int mx,int my,float delta) {
        int x=method_46426(),y=method_46427(),w=method_25368(),h=method_25364();var colors=AstralTheme.current();
        boolean focus=field_22762||method_25370();
        AstralDraw.rounded(context,x,y,w,h,focus ? colors.accent() : 0xFF454545);
        AstralDraw.rounded(context,x+1,y+1,w-2,h-2,colors.panel());
        int track=x+5,trackWidth=w-10,handle=track+(int)Math.round(trackWidth*field_22753);
        context.method_25294(track,y+h-4,track+trackWidth,y+h-3,0xFF454545);
        context.method_25294(track,y+h-4,handle,y+h-3,colors.accent());
        context.method_25294(Math.max(x+2,handle-2),y+h-6,Math.min(x+w-2,handle+2),y+h-2,colors.text());
        var font=class_310.method_1551().field_1772;
        context.method_25300(font,font.method_27523(method_25369().getString(),Math.max(0,w-14)),x+w/2,y+Math.max(1,(h-12)/2),colors.text());
    }
}
