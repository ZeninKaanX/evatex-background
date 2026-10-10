package com.videomenu;

import com.videomenu.video.VideoBackground;
import java.io.IOException;
import java.nio.file.Files;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VideoMenuClient implements ClientModInitializer {
    public static final String MOD_ID = "evatexbackgroundvideo";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override public void onInitializeClient() {
        try { Files.createDirectories(VideoFiles.directory()); }
        catch (IOException e) { LOGGER.warn("Cannot create videos folder", e); }
        VideoMenuConfig config = VideoMenuConfig.load();
        AstralTheme.animations(config.themeTransitions);
        VideoBackground.INSTANCE.init(config);
    }
}
