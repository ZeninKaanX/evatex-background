package com.videomenu.video;

import com.videomenu.VideoMenuClient;
import com.videomenu.VideoMenuConfig;
import com.videomenu.mixin.NativeImageAccessor;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.lwjgl.system.MemoryUtil;

public final class VideoBackground {
    public static final VideoBackground INSTANCE = new VideoBackground();
    public static final class_2960 TEXTURE_ID = new class_2960("evatexbackgroundvideo", "video");
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "EVATEX-Startup"); t.setDaemon(true); return t;
    });
    private volatile VideoMenuConfig config;
    private VideoDecoder decoder;
    private boolean menuVisible;
    private long generation;
    private volatile Frame latest;
    private int[] staging;
    private volatile String status = "idle";
    private Frame uploaded;
    private class_1043 texture;
    private IntBuffer pixels;
    private int textureW, textureH;

    private record Frame(int[] pixels, int width, int height) {}
    private VideoBackground() {}
    public synchronized void init(VideoMenuConfig value) { config = value.copy(); }
    public String status() { return status; }
    public synchronized String audioStatus() { return decoder == null ? "muted" : decoder.audioStatus(); }
    public boolean isActive() { return latest != null; }
    /** One policy for decoder lifetime, screen backdrop and opaque list suppression. */
    public boolean supportsMenu(net.minecraft.class_437 screen, boolean worldLoaded) {
        if (worldLoaded || screen == null) return false;
        return screen instanceof net.minecraft.class_442 || screen instanceof net.minecraft.class_500
            || screen instanceof com.videomenu.VideoSelectScreen || decoratesMenu(screen);
    }
    public boolean decoratesMenu(net.minecraft.class_437 screen) {
        VideoMenuConfig settings = config;
        return settings != null && settings.enabled &&
            (settings.worldMenuBackground && (screen instanceof net.minecraft.class_526 || screen instanceof net.minecraft.class_525 || screen instanceof net.minecraft.class_524)
             || settings.optionsMenuBackground && (screen instanceof net.minecraft.class_429 || screen instanceof net.minecraft.class_4667));
    }
    public boolean renderMenu(class_332 context, int width, int height, net.minecraft.class_437 screen) {
        boolean decorated = decoratesMenu(screen);
        if (!(screen instanceof net.minecraft.class_500) && !decorated) return false;
        if (!render(context,width,height)) return false;
        VideoMenuConfig settings = config;
        if (decorated) com.videomenu.AstralBackdrop.renderMenu(context,width,height,settings.menuMeteors && settings.themeTransitions);
        return true;
    }
    public synchronized void setMenuVisible(boolean visible) {
        if (menuVisible == visible) return;
        menuVisible = visible;
        restart();
    }
    public synchronized void reload(VideoMenuConfig value) {
        VideoMenuConfig next = value.copy();
        boolean restart = config == null || !Objects.equals(config.videoPath, next.videoPath)
            || config.hardwareAcceleration != next.hardwareAcceleration || config.maxDecodeHeight != next.maxDecodeHeight || config.scale != next.scale || config.fps != next.fps || config.enabled != next.enabled;
        config = next;
        if (restart) restart();
        else if (decoder != null) {decoder.setAudioEnabled(next.audioEnabled);decoder.setAudioVolume(next.audioVolume);}
    }
    private void restart() {
        long session = ++generation;
        if (decoder != null) { decoder.close(); decoder = null; }
        latest = null; staging = null;
        releaseTexture();
        status = "idle";
        if (!menuVisible || config == null || !config.enabled || config.videoPath.isBlank()) return;
        status = "loading";
        VideoDecoder next = new VideoDecoder(config, (data, w, h) -> {
            synchronized (this) {
                if (session == generation) {
                    if (staging == null || staging.length != data.length) staging = new int[data.length];
                    System.arraycopy(data, 0, staging, 0, data.length);
                    latest = new Frame(staging, w, h); status = "playing";
                }
            }
        }, () -> { synchronized (this) { if (session == generation) { latest = null; staging = null; status = "error"; } } }, true);
        decoder = next;
        worker.execute(() -> {
            boolean started = next.start();
            synchronized (this) { if (session == generation && !started) status = "error"; }
        });
    }
    public synchronized void close() {
        menuVisible = false;
        restart();
        worker.shutdownNow();
    }
    private void releaseTexture() {
        if (texture != null) {
            class_310.method_1551().method_1531().method_4615(TEXTURE_ID);
            texture = null;
        }
        pixels = null; uploaded = null;
    }
    public synchronized boolean render(class_332 context, int width, int height) {
        Frame frame = latest;
        VideoMenuConfig settings = config;
        if (frame == null || settings == null) { if (texture != null) releaseTexture(); return false; }
        try {
            if (texture == null || textureW != frame.width || textureH != frame.height) {
                releaseTexture();
                class_1011 image = new class_1011(class_1011.class_1012.field_4997, frame.width, frame.height, false);
                pixels = MemoryUtil.memIntBuffer(((NativeImageAccessor)(Object) image).videomenu$getPointer(), frame.pixels.length);
                texture = new class_1043(image);
                texture.method_4527(true, false);
                textureW = frame.width; textureH = frame.height;
                class_310.method_1551().method_1531().method_4616(TEXTURE_ID, texture);
            }
            if (uploaded != frame) {
                pixels.clear(); pixels.put(frame.pixels); texture.method_4524(); uploaded = frame;
            }
            double fit = settings.fillScreen
                ? Math.max((double) width / frame.width, (double) height / frame.height)
                : Math.min((double) width / frame.width, (double) height / frame.height);
            int dw = (int) Math.ceil(frame.width * fit), dh = (int) Math.ceil(frame.height * fit);
            context.method_25294(0, 0, width, height, 0xFF080808);
            context.method_25293(TEXTURE_ID, (width-dw)/2, (height-dh)/2, dw, dh,
                0f, 0f, frame.width, frame.height, frame.width, frame.height);
            context.method_25294(0, 0, width, height, (int)(settings.darkening * 255) << 24);
            if (settings.showVignette) {
                int edge = Math.max(1, Math.min(width, height) / 5);
                context.method_25296(0, 0, width, edge, 0x80000000, 0x00000000);
                context.method_25296(0, height-edge, width, height, 0x00000000, 0x80000000);
            }
            return true;
        } catch (RuntimeException e) {
            VideoMenuClient.LOGGER.error("Cannot render video texture", e);
            synchronized (this) {
                generation++; if (decoder != null) decoder.close(); latest = null; status = "error";
            }
            releaseTexture();
            return false;
        }
    }
}
