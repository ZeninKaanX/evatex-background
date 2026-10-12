package com.videomenu.mixin;
import com.videomenu.AstralMenus;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Older versions override render; newer versions inherit Screen's renderer. */
@Mixin({class_500.class,class_526.class,class_429.class})
public abstract class AstralLegacyMenusMixin {
    @Inject(method="method_25394",at=@At("HEAD"),cancellable=true,require=0)
    private void evatex$render(class_332 ctx,int mx,int my,float delta,CallbackInfo ci){
        if(AstralMenus.render((class_437)(Object)this,ctx,mx,my,delta,true))ci.cancel();
    }
}
