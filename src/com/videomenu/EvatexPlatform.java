package com.videomenu;

import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Loader services used by the shared UI and playback engine. */
public final class EvatexPlatform {
    private EvatexPlatform() {}
    public static Path gameDir() { return FabricLoader.getInstance().getGameDir(); }
    public static Path configDir() { return FabricLoader.getInstance().getConfigDir(); }
    public static String modVersion(String id) {
        return FabricLoader.getInstance().getModContainer(id)
            .map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("unavailable");
    }
    public static int modCount() { return FabricLoader.getInstance().getAllMods().size(); }
    public static String loaderName() { return "Fabric Loader"; }
    public static String loaderVersion() { return modVersion("fabricloader"); }
}
