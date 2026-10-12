package com.videomenu.mixin;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Linear filtering belongs only to EVATEX font atlases, never Minecraft or other mods. */
@Mixin(class_1060.class)
public abstract class AstralFontTextureMixin {
    @Inject(method="method_4616",at=@At("TAIL"))
    private void evatex$font(class_2960 id,class_1044 texture,CallbackInfo ci){
        if(id.toString().startsWith("evatexbackgroundvideo:brand/") || id.toString().startsWith("evatexbackgroundvideo:ui/") || id.toString().startsWith("evatexbackgroundvideo:ui_medium/") || id.toString().startsWith("evatexbackgroundvideo:ui_large/") || id.toString().startsWith("evatexbackgroundvideo:ui_display/") || id.toString().startsWith("evatexbackgroundvideo:home_icons/") || id.toString().startsWith("evatexbackgroundvideo:meteor/"))com.videomenu.FontSmoothing.apply(texture);
    }
}
