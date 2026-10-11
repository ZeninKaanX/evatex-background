package com.videomenu.mixin;

import com.videomenu.VideoSelectScreen;
import com.videomenu.video.VideoBackground;
import net.minecraft.class_310;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_310.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "method_1574", at = @At("HEAD"))
    private void evatex$menuLifecycle(CallbackInfo ci) {
        var screen = ((class_310)(Object)this).field_1755;
        if (screen instanceof com.videomenu.ShortcutHost host) host.evatex$refreshShortcut();
        VideoBackground.INSTANCE.setMenuVisible(VideoBackground.INSTANCE.supportsMenu(screen,
            ((class_310)(Object)this).field_1687 != null));
    }
    @Inject(method = "method_1490", at = @At("HEAD"))
    private void evatex$shutdown(CallbackInfo ci) { VideoBackground.INSTANCE.close(); }
}
