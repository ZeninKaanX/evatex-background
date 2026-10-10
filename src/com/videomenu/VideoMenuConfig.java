package com.videomenu;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class VideoMenuConfig {
    public static final String FILE_NAME = "evatexbackgroundvideo.json";
    public String videoPath = "";
    public int fps = 30;
    public double scale = 1.0;
    public boolean enabled = true;
    public boolean fillScreen = true;
    public boolean themeTransitions = true;
    public double darkening = 0.25;
    public boolean showVignette = true;

    public static VideoMenuConfig load() {
        VideoMenuConfig config = new VideoMenuConfig();
        Path file = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        if (!Files.isRegularFile(file, new LinkOption[0])) {
            config.save();
            return config;
        }
        try {
            JsonObject obj = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            config.themeTransitions = getBoolean(obj, "themeTransitions", true);
            config.enabled = getBoolean(obj, "enabled", true);
            config.fillScreen = getBoolean(obj, "fillScreen", true);
            config.videoPath = VideoMenuConfig.getString(obj, "videoPath", config.videoPath);
            config.fps = VideoMenuConfig.getInt(obj, "fps", config.fps);
            config.scale = VideoMenuConfig.getDouble(obj, "scale", config.scale);
            config.darkening = VideoMenuConfig.getDouble(obj, "darkening", config.darkening);
            config.showVignette = VideoMenuConfig.getBoolean(obj, "showVignette", config.showVignette);
        } catch (Exception e) {
            VideoMenuClient.LOGGER.error("[VideoMenu] evatexbackgroundvideo.json okunamadı, varsayılan ayarlar kullanılıyor.", e);
        }
        config.validate();
        return config;
    }

    public VideoMenuConfig copy() {
        VideoMenuConfig copy = new VideoMenuConfig();
        copy.videoPath = videoPath; copy.fps = fps; copy.scale = scale;
        copy.darkening = darkening; copy.showVignette = showVignette;
        copy.themeTransitions = themeTransitions;
        copy.enabled = enabled; copy.fillScreen = fillScreen;
        copy.validate();
        return copy;
    }

    public void validate() {
        if (videoPath == null) videoPath = "";
        fps = Math.max(15, Math.min(120, fps));
        scale = Double.isFinite(scale) ? Math.max(0.25, Math.min(1.0, scale)) : 1.0;
        darkening = Double.isFinite(darkening) ? Math.max(0.0, Math.min(0.8, darkening)) : 0.25;
    }

    public void save() {
        validate();
        try {
            Path dir = FabricLoader.getInstance().getConfigDir();
            Files.createDirectories(dir);
            Path temp = Files.createTempFile(dir, FILE_NAME, ".tmp");
            try {
                Files.writeString(temp, this.toJson(), StandardCharsets.UTF_8);
                try {
                    Files.move(temp, dir.resolve(FILE_NAME), java.nio.file.StandardCopyOption.ATOMIC_MOVE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                    Files.move(temp, dir.resolve(FILE_NAME), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            } finally { Files.deleteIfExists(temp); }
        } catch (Exception e) {
            VideoMenuClient.LOGGER.error("[VideoMenu] evatexbackgroundvideo.json yazılamadı.", e);
        }
    }

    private String toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("_HELP", "Bu dosya EVATEX-Background Video modunun ayarlarıdır (EverVerity tarafından yapılmıştır). Genelde bu dosyayı elle düzenlemenize gerek yoktur; ana menüdeki 'Video Menüsü' butonuyla video ve ayarları oyun içinden değiştirebilirsiniz. videoPath: ana menüde oynatılacak video dosyasının yolu. Videoları oyun klasöründeki 'videolar' klasörüne atarsanız otomatik listelenir. Not: Video, mod ile birlikte gelen FFmpeg ile çözülür; ek kurulum gerekmez.");
        obj.addProperty("themeTransitions", themeTransitions);
        obj.addProperty("enabled", enabled);
        obj.addProperty("fillScreen", fillScreen);
        obj.addProperty("videoPath", this.videoPath);
        obj.addProperty("fps", this.fps);
        obj.addProperty("scale", this.scale);
        obj.addProperty("darkening", this.darkening);
        obj.addProperty("showVignette", this.showVignette);
        return new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement) obj);
    }

    private static String getString(JsonObject obj, String key, String def) {
        return obj.has(key) && obj.get(key).isJsonPrimitive() ? obj.get(key).getAsString() : def;
    }

    private static int getInt(JsonObject obj, String key, int def) {
        try {
            return obj.has(key) ? obj.get(key).getAsInt() : def;
        } catch (Exception e) {
            return def;
        }
    }

    private static double getDouble(JsonObject obj, String key, double def) {
        try {
            return obj.has(key) ? obj.get(key).getAsDouble() : def;
        } catch (Exception e) {
            return def;
        }
    }

    private static boolean getBoolean(JsonObject obj, String key, boolean def) {
        try {
            return obj.has(key) ? obj.get(key).getAsBoolean() : def;
        } catch (Exception e) {
            return def;
        }
    }
}
