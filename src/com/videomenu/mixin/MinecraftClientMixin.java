package com.videomenu.mixin;

import com.videomenu.VideoSelectScreen;
import com.videomenu.video.VideoBackground;
import net.minecraft.class_310;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_310.class)
public abstract class MinecraftClientMixin {
    @org.spongepowered.asm.mixin.Unique private final com.videomenu.MenuOwnership evatex$ownership=new com.videomenu.MenuOwnership();
    @Inject(method = "method_1574", at = @At("HEAD"))
    private void evatex$menuLifecycle(CallbackInfo ci) {
        var client=(class_310)(Object)this;
        com.videomenu.EvatexRecovery.tick(client);
        if(com.videomenu.EvatexRecovery.suspended()) return;
        try {evatex$updateMenus(client);}
        catch(RuntimeException|LinkageError failure){com.videomenu.EvatexRecovery.report("menu lifecycle",failure);}
    }
    @org.spongepowered.asm.mixin.Unique private void evatex$updateMenus(class_310 client) {
        var screen = client.field_1755;
        boolean custom=VideoBackground.INSTANCE.customMainMenu();
        var kind=screen instanceof com.videomenu.EvatexTitleScreen ? com.videomenu.MenuOwnership.Kind.EVATEX
            : screen!=null && screen.getClass()==class_442.class ? com.videomenu.MenuOwnership.Kind.VANILLA
            : com.videomenu.MenuShortcut.titleMenu(screen) ? com.videomenu.MenuOwnership.Kind.FOREIGN
            : com.videomenu.MenuOwnership.Kind.NONE;
        if(evatex$ownership.shouldOpen(custom,client.field_1687!=null,kind)) {
            client.method_1507(new com.videomenu.EvatexTitleScreen());screen=client.field_1755;
        } else if(!custom && screen instanceof com.videomenu.EvatexTitleScreen && client.field_1687==null) {
            client.method_1507(new class_442());screen=client.field_1755;
        }
        if (screen instanceof com.videomenu.EvatexTitleScreen home) home.captureLateButtons();
        if (screen instanceof com.videomenu.ShortcutHost host) host.evatex$refreshShortcut();
        VideoBackground.INSTANCE.setPresentationFor(screen);
        VideoBackground.INSTANCE.setForeground(client.method_1569());
        VideoBackground.INSTANCE.setMenuVisible(VideoBackground.INSTANCE.supportsMenu(screen,
            ((class_310)(Object)this).field_1687 != null));
    }
    @Inject(method = "method_1490", at = @At("HEAD"))
    private void evatex$shutdown(CallbackInfo ci) { com.videomenu.EvatexDiscord.close(); VideoBackground.INSTANCE.close(); }
}
