package com.videomenu;
import net.minecraft.class_1044;
/** Runs only when an owned glyph atlas is registered, including resource reloads. */
public final class FontSmoothing {
    private FontSmoothing() {}
    public static void apply(class_1044 texture){
        try {
            try {class_1044.class.getMethod("method_4527",boolean.class,boolean.class).invoke(texture,true,false);return;}
            catch(NoSuchMethodException modern) {}
            Object cache=com.mojang.blaze3d.systems.RenderSystem.class.getMethod("getSamplerCache").invoke(null);
            var filter=Class.forName("com.mojang.blaze3d.textures.FilterMode");
            Object linear=filter.getField("LINEAR").get(null);
            Object sampler=cache.getClass().getMethod("method_75294",filter).invoke(cache,linear);
            var field=class_1044.class.getDeclaredField("field_63613");field.setAccessible(true);field.set(texture,sampler);
        }catch(ReflectiveOperationException e){VideoMenuClient.LOGGER.warn("EVATEX font filtering unavailable",e);}
    }
}
