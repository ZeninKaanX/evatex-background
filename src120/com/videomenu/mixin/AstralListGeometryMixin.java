package com.videomenu.mixin;
import com.videomenu.AstralMenus;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin({class_350.class,class_4267.class,class_528.class})
public abstract class AstralListGeometryMixin {
    @Inject(method="method_25322",at=@At("HEAD"),cancellable=true,require=0)
    private void evatex$row(CallbackInfoReturnable<Integer> ci){if(AstralMenus.ownsList(this))ci.setReturnValue(AstralMenus.rowWidth());}
    @Inject(method="method_25329",at=@At("HEAD"),cancellable=true,require=0)
    private void evatex$scrollbar(CallbackInfoReturnable<Integer> ci){if(AstralMenus.ownsList(this))ci.setReturnValue(AstralMenus.scrollbarX());}
}
