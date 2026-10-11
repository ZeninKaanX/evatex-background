package com.videomenu.mixin.atlas;
import com.videomenu.EvatexTitleScreen;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Temporarily let our standalone screen render; never changes Atlas's saved UI mode. */
@Pseudo
@Mixin(targets="me.koraizen.atlasclient.gui.UIModeManager",remap=false)
public abstract class AtlasHomeModeMixin {
    @Inject(method="isVanillaMode",at=@At("HEAD"),cancellable=true,remap=false)
    private static void evatex$standaloneHome(CallbackInfoReturnable<Boolean> cir) {
        if(class_310.method_1551().field_1755 instanceof EvatexTitleScreen) cir.setReturnValue(true);
    }
}
