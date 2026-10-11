package com.videomenu.mixin;

import com.videomenu.video.VideoBackground;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_526;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Older world lists paint their own background without calling Screen.renderBackground. */
@Mixin({class_526.class, net.minecraft.class_5235.class})
public abstract class WorldSelectionBackgroundMixin {
    @Inject(method="method_25394", at=@At("HEAD"))
    private void evatex$worldWallpaper(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        var screen=(net.minecraft.class_437)(Object)this;
        if (class_310.method_1551().field_1687==null)
            VideoBackground.INSTANCE.renderMenu(context,screen.field_22789,screen.field_22790,screen);
    }
}
