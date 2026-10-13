package com.videomenu.mixin;

import com.videomenu.video.VideoBackground;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_350;
import net.minecraft.class_4267;
import net.minecraft.class_500;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Legacy lists draw opaque dirt over the screen background, including the header/footer. */
@Mixin(class_350.class)
public abstract class ServerListBackgroundMixin {
    @Shadow private boolean field_26846;
    @Shadow private boolean field_26847;
    @Unique private boolean evatex$restore;
    @Unique private boolean evatex$background;
    @Unique private boolean evatex$edges;

    @Inject(method="method_25394", at=@At("HEAD"))
    private void evatex$transparentList(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        var client=class_310.method_1551();
        var screen=client.field_1755;
        evatex$restore=((Object)this instanceof class_4267 && screen instanceof class_500
            || VideoBackground.INSTANCE.decoratesMenu(screen))
            && VideoBackground.INSTANCE.supportsMenu(screen,client.field_1687!=null) && VideoBackground.INSTANCE.isActive();
        if (evatex$restore) {
            evatex$background=field_26846; evatex$edges=field_26847;
            field_26846=false; field_26847=false;
        }
    }
    @Inject(method="method_25394", at=@At("RETURN"))
    private void evatex$restoreList(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (evatex$restore) { field_26846=evatex$background; field_26847=evatex$edges; evatex$restore=false; }
    }
}
