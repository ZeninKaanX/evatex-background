package com.videomenu.mixin;

import com.videomenu.video.VideoBackground;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_350;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep list entries and scrolling; remove opaque panels only over an active menu wallpaper. */
@Mixin(class_350.class)
public abstract class MenuListBackgroundMixin {
    @Inject(method={"method_57713", "method_57715"}, at=@At("HEAD"), cancellable=true)
    private void evatex$transparentMenu(class_332 context, CallbackInfo ci) {
        var client=class_310.method_1551();
        var screen=client.field_1755;
        if (com.videomenu.AstralMenus.ownsList(this) || VideoBackground.INSTANCE.supportsMenu(screen, client.field_1687!=null) && VideoBackground.INSTANCE.isActive()
            && VideoBackground.INSTANCE.decoratesMenu(screen)) ci.cancel();
    }
}
