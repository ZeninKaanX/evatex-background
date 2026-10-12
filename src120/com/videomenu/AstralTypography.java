package com.videomenu;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
/** The home owns its font; it never changes Minecraft's font or needs another client. */
public final class AstralTypography {
    private static final class_2583 STYLE=fontStyle();
    private static class_2583 fontStyle() {
        try {
            var id=new net.minecraft.class_2960("evatexbackgroundvideo","ui");
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
    public static class_2561 text(String value){return class_2561.method_43470(value).method_10862(STYLE);}
    public static int width(String value){return net.minecraft.class_310.method_1551().field_1772.method_27525(text(value));}
    public static void draw(net.minecraft.class_332 ctx,String value,int x,int y,int color){ctx.method_51439(net.minecraft.class_310.method_1551().field_1772,text(value),x,y,color,false);}
}
