package com.videomenu;

/** Menus opt in to decoding; ordinary gameplay and unrelated overlays never do. */
public final class MenuPlaybackPolicy {
    private MenuPlaybackPolicy() {}
    public static boolean allows(boolean worldLoaded, boolean inGameEnabled, boolean mainMenu,
                                 boolean decoratedMenu, boolean settingsPreview) {
        if (worldLoaded) return inGameEnabled && (decoratedMenu || settingsPreview);
        return mainMenu || decoratedMenu || settingsPreview;
    }
}
