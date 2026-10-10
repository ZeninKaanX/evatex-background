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
import net.minecraft.class_10799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.lwjgl.system.MemoryUtil;

public final class VideoBackground {
    public static final VideoBackground INSTANCE = new VideoBackground();
    public static final class_2960 TEXTURE_ID = class_2960.method_60655("evatexbackgroundvideo", "video");
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "EVATEX-Startup"); t.setDaemon(true); return t;
    });
    private volatile VideoMenuConfig config;
    private VideoDecoder decoder;
    private boolean menuVisible;
    private long generation;
    private volatile Frame latest;
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
        latest = null;
        releaseTexture();
        status = "idle";
        if (!menuVisible || config == null || !config.enabled || config.videoPath.isBlank()) return;
        status = "loading";
        VideoDecoder next = new VideoDecoder(config, (data, w, h) -> {
            synchronized (this) {
                if (session == generation) { latest = new Frame(data, w, h); status = "playing"; }
            }
        }, () -> { synchronized (this) { if (session == generation) { latest = null; status = "error"; } } });
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
    public boolean prepareFrame() {
        Frame frame = latest;
        VideoMenuConfig settings = config;
        if (frame == null || settings == null) { if (texture != null) releaseTexture(); return false; }
        try {
            if (texture == null || textureW != frame.width || textureH != frame.height) {
                releaseTexture();
                class_1011 image = new class_1011(class_1011.class_1012.field_4997, frame.width, frame.height, false);
                pixels = MemoryUtil.memIntBuffer(((NativeImageAccessor)(Object) image).videomenu$getPointer(), frame.pixels.length);
                texture = new VideoTexture(image);
                textureW = frame.width; textureH = frame.height;
                class_310.method_1551().method_1531().method_4616(TEXTURE_ID, texture);
            }
            if (uploaded != frame) {
                pixels.clear(); pixels.put(frame.pixels); texture.method_4524(); uploaded = frame;
            }
            return true;
        } catch (RuntimeException e) { fail(e); return false; }
    }
    public record AtlasFrame(int textureId, int width, int height, boolean fill, double darkening, boolean vignette) {}
    public AtlasFrame atlasFrame() {
        if (!prepareFrame() || !(texture.method_68004() instanceof net.minecraft.class_10868 gl)) return null;
        return new AtlasFrame(gl.method_68427(), textureW, textureH, config.fillScreen, config.darkening, config.showVignette);
    }
    public boolean render(class_332 context, int width, int height) {
        if (!prepareFrame()) return false;
        Frame frame = uploaded;
        VideoMenuConfig settings = config;
        try {
            double fit = settings.fillScreen
                ? Math.max((double) width / frame.width, (double) height / frame.height)
                : Math.min((double) width / frame.width, (double) height / frame.height);
            int dw = (int) Math.ceil(frame.width * fit), dh = (int) Math.ceil(frame.height * fit);
            context.method_25294(0, 0, width, height, 0xFF080808);
            context.method_25302(class_10799.field_56883, TEXTURE_ID, (width-dw)/2, (height-dh)/2,
                0f, 0f, dw, dh, frame.width, frame.height, frame.width, frame.height);
            context.method_25294(0, 0, width, height, (int)(settings.darkening * 255) << 24);
            if (settings.showVignette) {
                int edge = Math.max(1, Math.min(width, height) / 5);
                context.method_25296(0, 0, width, edge, 0x80000000, 0x00000000);
                context.method_25296(0, height-edge, width, height, 0x00000000, 0x80000000);
            }
            return true;
        } catch (RuntimeException e) {
            fail(e); return false;
        }
    }
    private void fail(RuntimeException e) {
        VideoMenuClient.LOGGER.error("Cannot render video texture", e);
        synchronized (this) {
            generation++; if (decoder != null) decoder.close(); latest = null; status = "error";
        }
        releaseTexture();
    }
}
