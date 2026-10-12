package com.videomenu.mixin;
import com.videomenu.AstralMenus;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_4264.class)
public abstract class AstralNativeButtonMixin {
    @Inject(method="method_48579",at=@At("HEAD"),cancellable=true)
    private void evatex$button(class_332 ctx,int mx,int my,float delta,CallbackInfo ci){
        var widget=(class_339)(Object)this;
        if(AstralMenus.nativeButton(widget)){AstralMenus.button(ctx,widget,mx,my);ci.cancel();}
    }
}
