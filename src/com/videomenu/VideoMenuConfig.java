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
import com.videomenu.EvatexPlatform;

public final class VideoMenuConfig {
    public static final String FILE_NAME = "evatexbackgroundvideo.json";
    public String videoPath = "";
    public int fps = 30;
    public double scale = 1.0;
    public boolean enabled = true;
    public boolean libraryOnlinePreviews = true;
    public String libraryCatalogUrl = "";
    public java.util.List<String> libraryFavorites = new java.util.ArrayList<>();
    public boolean audioEnabled = false;
    public boolean hardwareAcceleration = true;
    public double audioVolume = 1.0;
    public int maxDecodeHeight = 1080;
    public boolean fillScreen = true;
    public boolean themeTransitions = true;
    public boolean worldMenuBackground = true;
    public boolean optionsMenuBackground = true;
    public boolean inGameMenuBackground = false;
    public boolean menuMeteors = true;
    public boolean customMainMenu = false;
    public double darkening = 0.25;
    public boolean showVignette = true;

    public static VideoMenuConfig load() {
        VideoMenuConfig config = new VideoMenuConfig();
        Path file = EvatexPlatform.configDir().resolve(FILE_NAME);
        if (!Files.isRegularFile(file, new LinkOption[0])) {
            config.save();
            return config;
        }
        JsonObject obj = new JsonObject();
        try {
            obj = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            config.worldMenuBackground = getBoolean(obj, "worldMenuBackground", true);
            config.optionsMenuBackground = getBoolean(obj, "optionsMenuBackground", true);
            config.inGameMenuBackground = getBoolean(obj, "inGameMenuBackground", false);
            config.customMainMenu = getBoolean(obj, "customMainMenu", false);
            config.menuMeteors = getBoolean(obj, "menuMeteors", true);
            config.themeTransitions = getBoolean(obj, "themeTransitions", true);
            config.audioEnabled = getBoolean(obj, "audioEnabled", false);
            config.hardwareAcceleration = getBoolean(obj, "hardwareAcceleration", true);
            config.audioVolume = getDouble(obj,"audioVolume",1.0);
            config.maxDecodeHeight = getInt(obj, "maxDecodeHeight", 1080);
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
        config.libraryOnlinePreviews = getInt(obj,"libraryPreviewPreferenceVersion",0)<1 || getBoolean(obj, "libraryOnlinePreviews", true);
        var library = obj;
        config.libraryCatalogUrl = getString(library,"libraryCatalogUrl","");
        if(library.has("libraryFavorites") && library.get("libraryFavorites").isJsonArray()) for(var id:library.getAsJsonArray("libraryFavorites")) if(id.isJsonPrimitive() && id.getAsString().matches("[a-z0-9-]{1,120}") && config.libraryFavorites.size()<1000)config.libraryFavorites.add(id.getAsString());
        config.validate();
        return config;
    }

    public VideoMenuConfig copy() {
        VideoMenuConfig copy = new VideoMenuConfig();
        copy.libraryOnlinePreviews=libraryOnlinePreviews;copy.libraryCatalogUrl=libraryCatalogUrl;copy.libraryFavorites=new java.util.ArrayList<>(libraryFavorites);
        copy.videoPath = videoPath; copy.fps = fps; copy.scale = scale;
        copy.darkening = darkening; copy.showVignette = showVignette;
        copy.worldMenuBackground = worldMenuBackground;
        copy.optionsMenuBackground = optionsMenuBackground;
        copy.inGameMenuBackground = inGameMenuBackground;
        copy.menuMeteors = menuMeteors;
        copy.customMainMenu = customMainMenu;
        copy.themeTransitions = themeTransitions;
        copy.audioEnabled = audioEnabled; copy.audioVolume=audioVolume; copy.maxDecodeHeight = maxDecodeHeight;
        copy.hardwareAcceleration = hardwareAcceleration;
        copy.enabled = enabled; copy.fillScreen = fillScreen;
        copy.validate();
        return copy;
    }

    public void validate() {
        audioVolume = Double.isFinite(audioVolume) ? Math.max(0,Math.min(1,audioVolume)) : 1.0;
        if (videoPath == null) videoPath = "";
        if(libraryCatalogUrl==null)libraryCatalogUrl="";
        if(libraryFavorites==null)libraryFavorites=new java.util.ArrayList<>();
        maxDecodeHeight = Math.max(360, Math.min(4320, maxDecodeHeight));
        fps = Math.max(15, Math.min(120, fps));
        scale = Double.isFinite(scale) ? Math.max(0.25, Math.min(1.0, scale)) : 1.0;
        darkening = Double.isFinite(darkening) ? Math.max(0.0, Math.min(0.8, darkening)) : 0.25;
    }

    public void save() {
        validate();
        try {
            Path dir = EvatexPlatform.configDir();
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
        obj.addProperty("_HELP", "EVATEX-Background ayarları. Arka Plan menüsünden video, GIF veya fotoğraf seçebilirsiniz. Dosyaları oyun klasöründeki videos, videolar veya backgrounds klasörüne ekleyin. videoPath seçili arka planın yoludur; eski ayarlar korunur. FFmpeg modla birlikte gelir.");
        obj.addProperty("libraryOnlinePreviews",libraryOnlinePreviews);
        obj.addProperty("libraryPreviewPreferenceVersion",1);
        obj.addProperty("libraryCatalogUrl",libraryCatalogUrl);
        obj.add("libraryFavorites",new com.google.gson.Gson().toJsonTree(libraryFavorites));
        obj.addProperty("worldMenuBackground", worldMenuBackground);
        obj.addProperty("optionsMenuBackground", optionsMenuBackground);
        obj.addProperty("inGameMenuBackground", inGameMenuBackground);
        obj.addProperty("menuMeteors", menuMeteors);
        obj.addProperty("customMainMenu", customMainMenu);
        obj.addProperty("themeTransitions", themeTransitions);
        obj.addProperty("enabled", enabled);
        obj.addProperty("audioEnabled", audioEnabled);
        obj.addProperty("hardwareAcceleration", hardwareAcceleration);
            obj.addProperty("audioVolume", audioVolume);
        obj.addProperty("maxDecodeHeight", maxDecodeHeight);
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
