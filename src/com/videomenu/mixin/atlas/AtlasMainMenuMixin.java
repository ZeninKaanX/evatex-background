package com.videomenu.mixin.atlas;
import com.videomenu.AtlasCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Pseudo
@Mixin(targets = "me.koraizen.atlasclient.menu.MainMenuButtons", remap = false)
public abstract class AtlasMainMenuMixin {
    @Shadow(remap = false) private static boolean anotherPanelOpen() { throw new AssertionError(); }
    @Inject(method = "render", at = @At("TAIL"), remap = false)
    private void evatex$shortcut(CallbackInfo ci) { if (!anotherPanelOpen()) AtlasCompatibility.shortcut(); }
}
