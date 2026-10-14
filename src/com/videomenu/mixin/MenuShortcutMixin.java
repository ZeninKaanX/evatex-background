package com.videomenu.mixin;

import com.videomenu.AstralButton;
import com.videomenu.MenuShortcut;
import com.videomenu.ShortcutHost;
import com.videomenu.VideoSelectScreen;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_364;
import net.minecraft.class_4068;
import net.minecraft.class_437;
import net.minecraft.class_6379;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_437.class)
public abstract class MenuShortcutMixin implements ShortcutHost {
    @Shadow protected abstract <T extends class_364 & class_4068 & class_6379> T method_37063(T widget);
    @Unique private AstralButton evatex$shortcut;
    @Unique private MenuShortcut.State evatex$layout;
    @Unique private boolean evatex$wasCustom;
    @Inject(method="method_25423",at=@At("TAIL"))
    private void evatex$afterMenuInit(CallbackInfo ci) { evatex$refreshShortcut(); }
    @Override public void evatex$refreshShortcut() {
        class_437 screen=(class_437)(Object)this;
        if (!MenuShortcut.titleMenu(screen)) return;
        if (evatex$shortcut==null || !screen.method_25396().contains(evatex$shortcut)) {
            evatex$shortcut=method_37063(new AstralButton(4,4,100,14,class_2561.method_43471("evatex.title"),
                button -> class_310.method_1551().method_1507(new com.videomenu.BackgroundLibraryScreen(screen,true)),false));
            evatex$layout=new MenuShortcut.State();
        }
        if (MenuShortcut.customUi()) {evatex$wasCustom=true;evatex$shortcut.field_22764=false;evatex$shortcut.field_22763=false;return;}
        // Force recovery after a custom UI toggle even when native geometry did not change.
        if (evatex$wasCustom) {evatex$layout=new MenuShortcut.State();evatex$wasCustom=false;}
        evatex$layout.place(screen,evatex$shortcut);
    }
}
