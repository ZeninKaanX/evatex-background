package com.videomenu;

/** Optional Mod Menu entrypoint, loaded only by Mod Menu itself. */
public final class ModMenuBridge implements com.terraformersmc.modmenu.api.ModMenuApi {
    @Override public com.terraformersmc.modmenu.api.ConfigScreenFactory<VideoSelectScreen> getModConfigScreenFactory() {
        return parent -> new VideoSelectScreen(parent,0);
    }
}
