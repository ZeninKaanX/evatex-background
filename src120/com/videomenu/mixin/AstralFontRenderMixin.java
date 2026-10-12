package com.videomenu.mixin;
import com.videomenu.*;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
/** Use the same font for native text measurement and glyph preparation in owned menus. */
@Mixin(class_327.class)
public abstract class AstralFontRenderMixin {
    @ModifyVariable(method={"method_27516","method_72731"},at=@At("HEAD"),argsOnly=true,require=0)
    private class_2583 evatex$font(class_2583 style){return AstralMenus.nativeText() ? AstralTypography.styled(style) : style;}
}
