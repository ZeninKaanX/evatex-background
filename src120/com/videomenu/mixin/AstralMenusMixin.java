package com.videomenu.mixin;
import com.videomenu.AstralMenus;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_437.class)
public abstract class AstralMenusMixin {
    @Inject(method="method_25423",at=@At("HEAD"))
    private void evatex$restoreBeforeInit(CallbackInfo ci){AstralMenus.beforeInit();}
    @Inject(method="method_25423",at=@At("TAIL"))
    private void evatex$layout(CallbackInfo ci){AstralMenus.prepare((class_437)(Object)this);}
    @Inject(method="method_47413",at=@At("HEAD"))
    private void evatex$frameStart(CallbackInfo ci){AstralMenus.beginFrame((class_437)(Object)this);}
    @Inject(method="method_47413",at=@At("RETURN"))
    private void evatex$frameEnd(CallbackInfo ci){AstralMenus.endFrame();}
    @Inject(method="method_25394",at=@At("HEAD"),cancellable=true)
    private void evatex$render(class_332 ctx,int mx,int my,float delta,CallbackInfo ci){
        if(AstralMenus.render((class_437)(Object)this,ctx,mx,my,delta,false))ci.cancel();
    }
}
