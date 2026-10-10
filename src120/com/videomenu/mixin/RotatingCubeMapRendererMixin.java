package com.videomenu.mixin;
import com.videomenu.video.VideoBackground;
import net.minecraft.class_766;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_766.class)
public abstract class RotatingCubeMapRendererMixin {
    @Inject(method = "method_3317(FF)V", at = @At("HEAD"), cancellable = true)
    private void evatex$skipPanorama(float delta, float rotation, CallbackInfo ci) {
        if (VideoBackground.INSTANCE.isActive()) ci.cancel();
    }
}
