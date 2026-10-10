package com.videomenu.mixin;

import com.videomenu.VideoSelectScreen;
import com.videomenu.video.VideoBackground;
import net.minecraft.class_2561;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {class_442.class})
public abstract class TitleScreenMixin extends class_437 {
    @org.spongepowered.asm.mixin.Unique private com.videomenu.AstralButton evatex$button;
    protected TitleScreenMixin() {
        super(class_2561.method_43473());
    }

    @Inject(method = {"method_25426"}, at = {@At(value = "TAIL")})
    private void videomenu$addVideoMenuButton(CallbackInfo ci) {
        // PressableTextWidget caches its rendered text: replace it rather than
        // changing its message. Preserve the original credits action and notice.
        for (var child : java.util.List.copyOf(this.method_25396())) {
            if (child instanceof net.minecraft.class_7077 footer && footer.method_25369().getString().equals(class_2561.method_43471("title.credits").getString())) {
                var label=class_2561.method_43470("© Mojang Studios");
                int textWidth=this.field_22793.method_1727(label.getString());
                var compact=new net.minecraft.class_7077(Math.max(4,this.field_22789-textWidth-4),this.field_22790-10,textWidth,10,label,button -> footer.method_25306(null),this.field_22793);
                compact.method_47400(net.minecraft.class_7919.method_47407(footer.method_25369()));
                this.method_37066(footer);
                this.method_37063(compact);
            }
        }
        this.evatex$button = this.method_37063(new com.videomenu.AstralButton(Math.max(4, (this.field_22789 - 100) / 2), this.field_22790 >= 220 ? this.field_22790 - 26 : 6, 100, 14, class_2561.method_43471("evatex.title"), button -> this.field_22787.method_1507(new VideoSelectScreen(this, 0)), false));
    }

    @Inject(method = "method_25394", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_437;method_25394(Lnet/minecraft/class_332;IIF)V"))
    private void evatex$keepShortcutVisible(net.minecraft.class_332 context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (evatex$button != null) evatex$button.field_22764 = !com.videomenu.AtlasCompatibility.isCustomUi();
    }
}
