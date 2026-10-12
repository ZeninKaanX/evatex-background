package com.videomenu.mixin;
import com.videomenu.video.VideoBackground;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_500;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = class_437.class, priority = 10000)
public abstract class ServerBackgroundMixin {
    @Inject(method = "method_25420", at = @At("HEAD"), cancellable = true)
    private void evatex$serverVideo(class_332 context, CallbackInfo ci) {
        class_437 screen = (class_437)(Object)this;
        if (class_310.method_1551().field_1687 == null
            && VideoBackground.INSTANCE.renderMenu(context, screen.field_22789, screen.field_22790, screen)) {
            com.videomenu.AstralMenus.chrome(context,screen);ci.cancel();
        }
    }
    @Inject(method = "method_25434", at = @At("HEAD"), cancellable = true)
    private void evatex$packVideo(class_332 context, CallbackInfo ci) {
        class_437 screen = (class_437)(Object)this;
        if (screen instanceof net.minecraft.class_5375 && class_310.method_1551().field_1687 == null
            && VideoBackground.INSTANCE.renderMenu(context, screen.field_22789, screen.field_22790, screen)) {
            com.videomenu.AstralMenus.chrome(context,screen);ci.cancel();
        }
    }
}
