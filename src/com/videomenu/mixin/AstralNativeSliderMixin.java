package com.videomenu.mixin;
import com.videomenu.AstralMenus;
import net.minecraft.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_357.class)
public abstract class AstralNativeSliderMixin {
    @Shadow protected double field_22753;
    @Inject(method="method_48579",at=@At("HEAD"),cancellable=true)
    private void evatex$slider(class_332 ctx,int mx,int my,float delta,CallbackInfo ci){
        var widget=(class_339)(Object)this;
        if(AstralMenus.nativeButton(widget)){
            AstralMenus.button(ctx,widget,mx,my);
            int x=widget.method_46426(),y=widget.method_46427(),w=widget.method_25368(),h=widget.method_25364();
            int handle=x+4+(int)Math.round((w-8)*field_22753);
            ctx.method_25294(x+4,y+h-4,x+w-4,y+h-3,0xFF555555);
            ctx.method_25294(x+4,y+h-4,handle,y+h-3,0xFFCCCCCC);
            ctx.method_25294(Math.max(x+2,handle-1),y+h-5,Math.min(x+w-2,handle+1),y+h-2,0xFFF0F0F0);
            ci.cancel();
        }
    }
}
