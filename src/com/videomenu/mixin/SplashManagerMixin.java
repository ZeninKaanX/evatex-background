package com.videomenu.mixin;
import com.videomenu.SplashMessages;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_4008;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(class_4008.class)
public abstract class SplashManagerMixin {
    @Shadow private List<class_2561> field_17906;
    @Inject(method="method_18175(Ljava/util/List;Lnet/minecraft/class_3300;Lnet/minecraft/class_3695;)V",at=@At("TAIL"))
    private void evatex$appendMessages(CallbackInfo ci) {
        var updated=new ArrayList<>(field_17906);
        for(String message:SplashMessages.all()) {
            if(updated.stream().noneMatch(existing->existing.getString().equals(message)))updated.add(class_2561.method_43470(message));
        }
        field_17906=updated;
    }
}
