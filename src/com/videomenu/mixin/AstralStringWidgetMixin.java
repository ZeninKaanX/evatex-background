package com.videomenu.mixin;
import com.videomenu.*;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
/** New StringWidgets draw through a text collector rather than DrawContext. */
@Mixin(class_7842.class)
public abstract class AstralStringWidgetMixin {
    @ModifyVariable(method="method_75796",at=@At("STORE"),ordinal=0,require=0)
    private class_2561 evatex$label(class_2561 text){return AstralMenus.nativeText() ? AstralTypography.styled(text) : text;}
}
