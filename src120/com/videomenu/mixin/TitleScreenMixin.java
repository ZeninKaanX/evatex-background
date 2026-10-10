package com.videomenu.mixin;

import com.videomenu.VideoSelectScreen;
import com.videomenu.video.VideoBackground;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {class_442.class})
public abstract class TitleScreenMixin extends class_437 {
    protected TitleScreenMixin() {
        super(class_2561.method_43473());
    }

    @Inject(method = {"method_25426"}, at = {@At(value = "TAIL")})
    private void videomenu$addVideoMenuButton(CallbackInfo ci) {
        // Keep the original credits action and full localized notice on hover.
        for (var child : this.method_25396()) {
            if (child instanceof net.minecraft.class_7077 footer && footer.method_25369().getString().equals(class_2561.method_43471("title.credits").getString())) {
                footer.method_47400(net.minecraft.class_7919.method_47407(footer.method_25369()));
                footer.method_25355(class_2561.method_43470("© Mojang Studios"));
                int textWidth=this.field_22793.method_1727(footer.method_25369().getString());
                footer.method_25358(textWidth);
                footer.method_46421(Math.max(4,this.field_22789-textWidth-4));
                footer.method_46419(this.field_22790-10);
            }
        }
        this.method_37063(new com.videomenu.AstralButton(Math.max(4, this.field_22789 - 104), Math.max(6, this.field_22790 - 26), 100, 14, class_2561.method_43471("evatex.title"), button -> this.field_22787.method_1507(new VideoSelectScreen(this, 0)), false));
    }

    @Inject(method = "method_25394", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_332;method_51422(FFFF)V", ordinal = 1, shift = At.Shift.AFTER))
    private void evatex$drawVideoBeforeWidgets(class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        VideoBackground.INSTANCE.render(context, field_22789, field_22790);
    }
}
