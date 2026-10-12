package com.videomenu.mixin;
import com.videomenu.*;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
/** Only native list/text-field drawing inside EVATEX-owned menu presentation. */
@Mixin(class_332.class)
public abstract class AstralMenuTextMixin {
    @Inject(method="method_51433(Lnet/minecraft/class_327;Ljava/lang/String;IIIZ)V",at=@At("HEAD"),cancellable=true,require=0)
    private void evatex$text(class_327 font,String text,int x,int y,int color,boolean shadow,CallbackInfo ci){
        if(AstralMenus.nativeText()){AstralTypography.draw((class_332)(Object)this,text,x,y,color);ci.cancel();}
    }
    @Inject(method="method_51433(Lnet/minecraft/class_327;Ljava/lang/String;IIIZ)I",at=@At("HEAD"),cancellable=true,require=0)
    private void evatex$oldText(class_327 font,String text,int x,int y,int color,boolean shadow,CallbackInfoReturnable<Integer> ci){
        if(AstralMenus.nativeText()){AstralTypography.draw((class_332)(Object)this,text,x,y,color);ci.setReturnValue(x+AstralTypography.width(text));}
    }
    @ModifyVariable(method={"method_51430","method_51439"},at=@At("HEAD"),argsOnly=true,ordinal=0,require=0)
    private boolean evatex$shadow(boolean shadow){return AstralMenus.nativeText() ? false : shadow;}
    @ModifyVariable(method="method_51430",at=@At("HEAD"),argsOnly=true,require=0)
    private class_5481 evatex$ordered(class_5481 text){return AstralMenus.nativeText() ? AstralTypography.styled(text) : text;}
    @ModifyVariable(method="method_51439",at=@At("HEAD"),argsOnly=true,require=0)
    private class_2561 evatex$component(class_2561 text){return AstralMenus.nativeText() ? AstralTypography.styled(text) : text;}
}
