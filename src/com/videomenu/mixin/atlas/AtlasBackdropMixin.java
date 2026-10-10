package com.videomenu.mixin.atlas;
import com.videomenu.AtlasCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Pseudo
@Mixin(targets = "me.koraizen.atlasclient.menu.AtlasHorizonShell", remap = false)
public abstract class AtlasBackdropMixin {
    @Inject(method = "backdrop", at = @At("HEAD"), cancellable = true, remap = false)
    private static void evatex$video(@Coerce Object list, float width, float height, float alpha, boolean custom, float[] accent, CallbackInfo ci) {
        if (AtlasCompatibility.drawBackground(list, width, height, alpha)) ci.cancel();
    }
}
