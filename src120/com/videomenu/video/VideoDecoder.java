package com.videomenu.video;

import com.videomenu.VideoMenuClient;
import com.videomenu.VideoMenuConfig;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.TimeUnit;
import com.videomenu.VideoFiles;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;

public final class VideoDecoder {
    private static final Pattern DIM_PATTERN = Pattern.compile("(\\d{2,5})x(\\d{2,5})");
    private static volatile Path bundledFfmpeg;
    private final VideoMenuConfig config;
    private final FrameListener listener;
    private final Runnable onFailure;
    private volatile boolean closed;
    private volatile Process process;
    private volatile long publishedFrames;
    private boolean everPublishedFrame;
    private Thread thread;

    public VideoDecoder(VideoMenuConfig config, FrameListener listener) { this(config, listener, () -> {}); }

    public VideoDecoder(VideoMenuConfig config, FrameListener listener, Runnable onFailure) {
        this.onFailure = onFailure;
        this.config = config.copy();
        this.listener = listener;
    }

    public boolean start() {
        if (closed) return false;
        Path file = this.resolveVideoFile();
        if (file == null || !Files.isRegularFile(file, new LinkOption[0])) {
            VideoMenuClient.LOGGER.error("[VideoMenu] Video dosyası bulunamadı: '{}'", this.config.videoPath);
            return false;
        }
        int[] size = this.probeSize(file);
        if (size == null) {
            VideoMenuClient.LOGGER.error("[VideoMenu] FFmpeg bulunamadı veya video okunamadı: '{}'. MP4 (H.264) bir dosya kullanın.", this.config.videoPath);
            return false;
        }
        if (closed) return false;
        this.thread = new Thread(() -> {
            try { this.run(file, size[0], size[1]); }
            finally { if (!closed) onFailure.run(); }
        }, "VideoMenu-Decoder");
        this.thread.setDaemon(true);
        this.thread.start();
        return true;
    }

    public void close() {
        this.closed = true;
        Process p = this.process;
        if (p != null) {
            p.destroyForcibly();
        }
        if (this.thread != null) {
            this.thread.interrupt();
        }
    }

    public long publishedFrames() {
        return this.publishedFrames;
    }

    private void run(Path file, int srcW, int srcH) {
        int[] target = this.computeTarget(srcW, srcH);
        boolean streamLoop = true;
        while (!this.closed) {
            try {
                Process p;
                this.process = p = this.spawnFfmpeg(file, target[0], target[1], streamLoop);
                try {
                    this.readFrames(p.getInputStream(), target[0], target[1]);
                    this.waitForExit(p);
                } finally {
                    p.destroyForcibly();
                    this.process = null;
                }
                if (streamLoop && !this.everPublishedFrame) {
                    streamLoop = false;
                    continue;
                }
                if (!streamLoop && !this.everPublishedFrame) {
                    VideoMenuClient.LOGGER.error("[VideoMenu] Video çözümlenemedi: '{}'. Video kullanılmayacak.", this.config.videoPath);
                    return;
                }
            } catch (Throwable t) {
                if (this.closed) {
                    break;
                }
                if (!this.everPublishedFrame) {
                    VideoMenuClient.LOGGER.error("[VideoMenu] Video çözümlenemedi: '{}'. Video kullanılmayacak.", this.config.videoPath, t);
                    return;
                }
                VideoMenuClient.LOGGER.warn("[VideoMenu] Video ortasında hata oluştu, baştan deneniyor.", t);
                VideoDecoder.park(2000000000L);
            }
            if (this.closed) {
                break;
            }
            VideoDecoder.park(200000000L);
        }
    }

    private Process spawnFfmpeg(Path file, int dstW, int dstH, boolean streamLoop) throws IOException {
        ArrayList<String> cmd = new ArrayList<>();
        cmd.add(VideoDecoder.ffmpegPath());
        cmd.add("-hide_banner");
        cmd.add("-loglevel");
        cmd.add("error");
        cmd.add("-nostdin");
        if (streamLoop) {
            cmd.add("-stream_loop");
            cmd.add("-1");
        }
        cmd.add("-i");
        cmd.add(file.toString());
        cmd.add("-map");
        cmd.add("0:v:0");
        cmd.add("-an");
        cmd.add("-sn");
        cmd.add("-dn");
        cmd.add("-vf");
        cmd.add("fps=" + config.fps + ",scale=" + dstW + ":" + dstH + ":flags=lanczos,setsar=1,format=rgba");
        cmd.add("-f");
        cmd.add("rawvideo");
        cmd.add("-pix_fmt");
        cmd.add("rgba");
        cmd.add("pipe:1");
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        Process p = pb.start();
        if (closed) p.destroyForcibly();
        return p;
    }

    private int[] computeTarget(int srcW, int srcH) {
        double scale = Math.min(Math.max(this.config.scale, 0.05), 1.0);
        scale = Math.min(scale, Math.min(3840.0 / srcW, 2160.0 / srcH));
        int dstW = Math.max(2, (int) Math.round((double) srcW * scale) & 0xFFFFFFFE);
        int dstH = Math.max(2, (int) Math.round((double) srcH * scale) & 0xFFFFFFFE);
        return new int[]{dstW, dstH};
    }

    private void readFrames(InputStream in, int w, int h) throws IOException {
        int frameSize = w * h * 4;
        byte[] rgba = new byte[frameSize];
        int[] out = new int[w * h];
        long interval = 1000000000L / (long) Math.max(1, this.config.fps);
        long nextFrameAt = System.nanoTime();
        while (!this.closed) {
            if (!VideoDecoder.readFully(in, rgba)) {
                return;
            }
            VideoDecoder.pack(rgba, w, h, out);
            if (!closed) this.listener.onFrame(out.clone(), w, h);
            this.everPublishedFrame = true;
            ++this.publishedFrames;
            long now = System.nanoTime();
            if (now < (nextFrameAt += interval)) {
                VideoDecoder.park(nextFrameAt - now);
            } else {
                nextFrameAt = now;
            }
        }
    }

    private static void pack(byte[] rgba, int w, int h, int[] out) {
        int n = w * h;
        int i = 0;
        int p = 0;
        while (i < n) {
            int r = rgba[p] & 0xFF;
            int g = rgba[p + 1] & 0xFF;
            int b = rgba[p + 2] & 0xFF;
            int a = rgba[p + 3] & 0xFF;
            out[i] = a << 24 | b << 16 | g << 8 | r;
            ++i;
            p += 4;
        }
    }

    private static boolean readFully(InputStream in, byte[] buf) throws IOException {
        int off = 0;
        while (off < buf.length) {
            int n = in.read(buf, off, buf.length - off);
            if (n < 0) {
                return false;
            }
            if (n == 0) {
                LockSupport.parkNanos(1000000L);
                continue;
            }
            off += n;
        }
        return true;
    }

    private void waitForExit(Process p) {
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private int[] probeSize(Path file) {
        return this.probeWithFfmpeg(file);
    }

    private int[] probeWithFfmpeg(Path file) {
        Process p = null;
        Path report = null;
        try {
            report = Files.createTempFile("evatex-probe-", ".log");
            p = new ProcessBuilder(VideoDecoder.ffmpegPath(), "-nostdin", "-hide_banner", "-i", file.toString())
                .redirectErrorStream(true).redirectOutput(report.toFile()).start();
            process = p;
            if (closed || !p.waitFor(10, TimeUnit.SECONDS)) return null;
            String out = Files.readString(report, StandardCharsets.UTF_8);
            for (String line : out.split("\\R")) {
                if (!line.contains("Video:")) continue;
                Matcher m = DIM_PATTERN.matcher(line);
                if (m.find()) {
                    int w = Integer.parseInt(m.group(1)), h = Integer.parseInt(m.group(2));
                    Matcher sar = Pattern.compile("SAR (\\d+):(\\d+)").matcher(line);
                    if (sar.find() && Integer.parseInt(sar.group(2)) > 0)
                        w = Math.max(2, (int)Math.round((double)w * Integer.parseInt(sar.group(1)) / Integer.parseInt(sar.group(2))));
                    // FFmpeg autorotates portrait footage before applying the scale filter.
                    if (out.contains("rotation of -90") || out.contains("rotation of 90")) {
                        int swap = w; w = h; h = swap;
                    }
                    return new int[]{w, h};
                }
            }
        } catch (IOException | RuntimeException e) {
            VideoMenuClient.LOGGER.warn("Cannot probe video {}", file, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (p != null) p.destroyForcibly();
            process = null;
            if (report != null) try { Files.deleteIfExists(report); } catch (IOException ignored) {}
        }
        return null;
    }

    private static synchronized String ffmpegPath() {
        Path bundled = VideoDecoder.bundledFfmpeg;
        if (bundled == null) {
            bundled = VideoDecoder.extractBundledFfmpeg();
            VideoDecoder.bundledFfmpeg = bundled;
        }
        return bundled != null ? bundled.toString() : "ffmpeg";
    }

    private static Path extractBundledFfmpeg() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String platform;
        String exeName;
        if (os.contains("mac") || os.contains("darwin")) {
            return null;
        } else if (os.contains("win")) {
            platform = "win64";
            exeName = "ffmpeg.exe";
        } else if (os.contains("mac") || os.contains("darwin")) {
            return null;
        } else {
            if (!System.getProperty("os.arch", "").matches("amd64|x86_64")) return null;
            platform = "linux64";
            exeName = "ffmpeg";
        }
        try (InputStream in = VideoDecoder.class.getClassLoader().getResourceAsStream("assets/videomenu/ffmpeg/" + platform + "/" + exeName)) {
            if (in == null) {
                VideoMenuClient.LOGGER.info("[VideoMenu] Dahili FFmpeg bulunamadı; sistem ffmpeg'i kullanılacak.");
                return null;
            }
            Path dir = FabricLoader.getInstance().getGameDir().resolve(".evatex-ffmpeg").resolve("1.8.0").resolve(platform);
            Files.createDirectories(dir);
            Path out = dir.resolve(exeName);
            if (!Files.isRegularFile(out, new LinkOption[0])) {
                Path tmp = dir.resolve(exeName + ".tmp");
                Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
                if (!os.contains("win")) {
                    tmp.toFile().setExecutable(true, false);
                }
                Files.move(tmp, out, StandardCopyOption.REPLACE_EXISTING);
                VideoMenuClient.LOGGER.info("[VideoMenu] Dahili FFmpeg '{}' klasörüne çıkarıldı.", out);
            }
            return out;
        } catch (Throwable t) {
            VideoMenuClient.LOGGER.warn("[VideoMenu] Dahili FFmpeg çıkarılamadı; sistem ffmpeg'i kullanılacak.", t);
            return null;
        }
    }

    private static void park(long nanos) {
        LockSupport.parkNanos(nanos);
    }

    private Path resolveVideoFile() {
        return VideoDecoder.resolveVideoPath(this.config.videoPath);
    }

    public static Path resolveVideoPath(String path) {
        return VideoFiles.resolve(path);
    }

    @FunctionalInterface
    public static interface FrameListener {
        public void onFrame(int[] var1, int var2, int var3);
    }
}
