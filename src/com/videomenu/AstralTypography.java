package com.videomenu;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
/** The home owns its font; it never changes Minecraft's font or needs another client. */
public final class AstralTypography {
    private static final class_2583 STYLE=fontStyle("ui");
    private static final class_2583 MEDIUM_STYLE=fontStyle("ui_medium");
    private static final class_2583 LARGE_STYLE=fontStyle("ui_large");
    private static final class_2583 DISPLAY_STYLE=fontStyle("ui_display");
    private static final class_2583 ICON_STYLE=fontStyle("home_icons");
    private static final class_2583 BRAND_STYLE=fontStyle("brand");
    private static final class_2583 METEOR_STYLE=fontStyle("meteor");
    private static final class_2561[] BRAND_FRAMES=brandFrames();
    private static double brandPhase;
    private static long brandTick;
    private static class_2561[] brandFrames(){
        var frames=new class_2561[64];
        for(int i=0;i<frames.length;i++)frames[i]=class_2561.method_43470(String.valueOf((char)(0xE100+i))).method_10862(BRAND_STYLE);
        return frames;
    }
    public static void brand(net.minecraft.class_332 ctx,int x,int y,int color){
        var client=net.minecraft.class_310.method_1551();
        long now=System.nanoTime();
        if(brandTick!=0 && AstralTheme.animated() && client.method_1569())
            brandPhase=(brandPhase+Math.min((now-brandTick)/1e9,.1)*24)%BRAND_FRAMES.length;
        if(!AstralTheme.animated())brandPhase=0;
        brandTick=now;
        ctx.method_51439(client.field_1772,BRAND_FRAMES[(int)brandPhase],x,y,color,false);
    }
    public static void meteor(net.minecraft.class_332 ctx,int x,int y,int color){ctx.method_51439(net.minecraft.class_310.method_1551().field_1772,class_2561.method_43470("\uE003").method_10862(METEOR_STYLE),x,y,color,false);}
    private static class_2583 fontStyle(String fontName) {
        try {
            var id=net.minecraft.class_2960.method_60655("evatexbackgroundvideo",fontName);
            for(var method:class_2583.class.getMethods())if(method.getName().equals("method_27704")) {
                var kind=method.getParameterTypes()[0];Object font=id;
                if(!kind.isInstance(id)) {
                    font=null;
                    for(var nested:kind.getDeclaredClasses())if(kind.isAssignableFrom(nested)) {
                        try {font=nested.getConstructor(net.minecraft.class_2960.class).newInstance(id);break;}
                        catch(NoSuchMethodException ignored) {}
                    }
                    if(font==null)throw new IllegalStateException("Missing resource font type");
                }
                return (class_2583)method.invoke(class_2583.field_24360,font);
            }
        } catch(ReflectiveOperationException|RuntimeException e){VideoMenuClient.LOGGER.warn("Home font unavailable; using Minecraft font",e);}
        return class_2583.field_24360;
    }
    public static void icon(net.minecraft.class_332 ctx,int type,int x,int y,int color){
        var label=class_2561.method_43470(switch(type){case 1 -> "\uE001";case 2 -> "\uE002";default -> "\uE000";}).method_10862(ICON_STYLE);
        ctx.method_51439(net.minecraft.class_310.method_1551().field_1772,label,x,y,color,false);
    }
    private static class_2583 samplingStyle(float scale){
        double density=net.minecraft.class_310.method_1551().method_22683().method_4495()*scale;
        return density<=2.5 ? STYLE : density<=5.5 ? MEDIUM_STYLE : density<=10 ? LARGE_STYLE : DISPLAY_STYLE;
    }
    public static class_2561 text(String value){return class_2561.method_43470(value).method_10862(samplingStyle(1));}
    public static class_2561 styled(class_2561 value){return value.method_27661().method_10862(samplingStyle(1).method_27702(value.method_10866()));}
    private static final java.util.IdentityHashMap<class_2583,class_2583> ORDERED_STYLES=new java.util.IdentityHashMap<>();
    public static void beginNativeText(){ORDERED_STYLES.clear();}
    public static class_2583 styled(class_2583 value){return ORDERED_STYLES.computeIfAbsent(value,s -> samplingStyle(1).method_27702(s));}
    public static net.minecraft.class_5481 styled(net.minecraft.class_5481 value){
        return sink -> value.accept((index,style,codepoint) -> sink.accept(index,styled(style),codepoint));
    }
    public static String fit(String value,int width){
        String label=value;
        while(label.length()>1 && width(label)>width)label=label.substring(0,label.offsetByCodePoints(label.length(),-1));
        return label;
    }
    public static void centered(net.minecraft.class_332 ctx,String text,int center,int y,int color){draw(ctx,text,center-width(text)/2,y,color);}
    public static int width(String value){return net.minecraft.class_310.method_1551().field_1772.method_27525(text(value));}
    public static void draw(net.minecraft.class_332 ctx,String value,int x,int y,int color){draw(ctx,value,x,y,color,false);}
    public static void drawScaled(net.minecraft.class_332 ctx,String value,int x,int y,int color,float scale){
        var label=class_2561.method_43470(value).method_10862(samplingStyle(scale));
        ctx.method_51439(net.minecraft.class_310.method_1551().field_1772,label,x,y,color,false);
    }
    public static void draw(net.minecraft.class_332 ctx,String value,int x,int y,int color,boolean display){
        var label=class_2561.method_43470(value).method_10862(display ? DISPLAY_STYLE : samplingStyle(1));
        ctx.method_51439(net.minecraft.class_310.method_1551().field_1772,label,x,y,color,false);
    }
}
