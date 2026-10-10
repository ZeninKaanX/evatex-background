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
    private static volatile boolean systemOnly;
    private final VideoMenuConfig config;
    private final FrameListener listener;
    private final Runnable onFailure;
    private volatile boolean closed;
    private volatile Process process;
    private volatile long publishedFrames;
    private boolean everPublishedFrame;
    private Thread thread;
    private volatile Thread frameReader;
    private volatile VideoAudio audio;
    private volatile boolean audioEnabled;
    private volatile double audioVolume;
    private boolean hasAudio;
    private volatile String lastError = "";

    public VideoDecoder(VideoMenuConfig config, FrameListener listener) { this(config, listener, () -> {}); }

    public VideoDecoder(VideoMenuConfig config, FrameListener listener, Runnable onFailure) {
        this.onFailure = onFailure;
        this.config = config.copy();
        this.audioEnabled = config.audioEnabled;
        this.audioVolume = config.audioVolume;
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
        VideoAudio sink = audio; if (sink != null) sink.close();
        Thread reader=frameReader;if(reader!=null)reader.interrupt();
        Process p = this.process;
        if (p != null) {
            p.destroyForcibly();
        }
        if (this.thread != null) {
            this.thread.interrupt();
        }
    }

    public void setAudioEnabled(boolean enabled) { audioEnabled = enabled; VideoAudio sink = audio; if (sink != null) sink.enabled(enabled); }
    public void setAudioVolume(double volume) {audioVolume=volume;VideoAudio sink=audio;if(sink!=null)sink.volume(volume);}
    public String audioStatus() { VideoAudio sink = audio; return !hasAudio ? "no_track" : sink == null ? "muted" : sink.status(); }
    public long audioReceivedBytes() { VideoAudio sink = audio; return sink == null ? 0 : sink.receivedBytes(); }
    public long audioWrittenBytes() { VideoAudio sink = audio; return sink == null ? 0 : sink.writtenBytes(); }
    public long audioUnderruns() { VideoAudio sink=audio;return sink==null ? 0 : sink.underruns(); }
    public String lastError() { return lastError; }
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
                    VideoAudio sink = audio; audio = null; if (sink != null) sink.close();
                }
                if (streamLoop && !this.everPublishedFrame) {
                    streamLoop = false;
                    continue;
                }
                if (!streamLoop && !this.everPublishedFrame) {
                    VideoMenuClient.LOGGER.error("Cannot decode video {}: {}", this.config.videoPath, lastError);
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
        cmd.add("-filter_threads"); cmd.add("1");
        if (streamLoop) {
            cmd.add("-stream_loop");
            cmd.add("-1");
        }
        cmd.add("-threads"); cmd.add("2");
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
        cmd.add("-threads"); cmd.add("1");
        cmd.add("-flush_packets"); cmd.add("1");
        cmd.add("-avioflags"); cmd.add("direct");
        cmd.add("pipe:1");
        if (hasAudio) {
            try { audio = new VideoAudio(audioEnabled,audioVolume); }
            catch (IOException e) { VideoMenuClient.LOGGER.warn("Cannot initialize video audio; continuing silently",e); }
            if (audio != null) cmd.addAll(java.util.List.of("-map", "0:a:0", "-vn", "-sn", "-dn", "-ac", "2", "-ar", "48000", "-af", "aresample=async=1:first_pts=0", "-c:a", "pcm_s16le", "-f", "s16le", audio.outputUrl()));
        }
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectError(ProcessBuilder.Redirect.PIPE);
        Process p;
        try { p = startFfmpeg(pb); }
        catch (IOException e) { VideoAudio sink = audio; audio = null; if (sink != null) sink.close(); throw e; }
        Thread errors = new Thread(() -> {
            try (var stream = p.getErrorStream()) {
                byte[] buffer = new byte[2048]; int n;
                while ((n = stream.read(buffer)) > 0) lastError = new String(buffer, 0, n, StandardCharsets.UTF_8);
            } catch (IOException ignored) {}
        }, "EVATEX-FFmpeg-Errors"); errors.setDaemon(true); errors.start();
        if (closed) { p.destroyForcibly(); VideoAudio sink = audio; if (sink != null) sink.close(); }
        return p;
    }

    private int[] computeTarget(int srcW, int srcH) {
        double scale = Math.min(Math.max(this.config.scale, 0.05), 1.0);
        double limitHeight = config.maxDecodeHeight;
        // Bound Java-side frame storage independently of source resolution and worker count.
        double maxPixels = Math.min(7680.0*4320, Math.max(640.0*360, Runtime.getRuntime().maxMemory()/96.0));
        scale = Math.min(scale, Math.min(limitHeight*16.0/9.0/srcW, limitHeight/srcH));
        scale = Math.min(scale, Math.sqrt(maxPixels/((double)srcW*srcH)));
        int dstW = Math.max(2, (int) Math.round((double) srcW * scale) & 0xFFFFFFFE);
        int dstH = Math.max(2, (int) Math.round((double) srcH * scale) & 0xFFFFFFFE);
        return new int[]{dstW, dstH};
    }

    private void readFrames(InputStream in, int w, int h) throws IOException {
        // Keep native pipe reads separate from the presentation clock. In particular,
        // macOS pipe wakeups can otherwise delay several frames after each paced read.
        var frames=new java.util.concurrent.ArrayBlockingQueue<int[]>(4);
        var finished=new java.util.concurrent.atomic.AtomicBoolean();
        var failure=new java.util.concurrent.atomic.AtomicReference<Throwable>();
        Thread reader=new Thread(()->{
            byte[] rgba=new byte[w*h*4];
            try {
                while(!closed && !Thread.currentThread().isInterrupted() && readFully(in,rgba)) {
                    int[] pixels=new int[w*h];pack(rgba,w,h,pixels);frames.put(pixels);
                }
            } catch(InterruptedException e) {Thread.currentThread().interrupt();}
            catch(Throwable e) {if(!closed)failure.set(e);}
            finally {finished.set(true);}
        },"EVATEX-FrameReader");
        reader.setDaemon(true);frameReader=reader;reader.start();
        long interval=1_000_000_000L/Math.max(1,config.fps),nextFrameAt=0;
        try {
            while(!closed) {
                int[] frame;
                try {frame=frames.poll(100,TimeUnit.MILLISECONDS);}
                catch(InterruptedException e) {Thread.currentThread().interrupt();if(closed)return;throw new IOException("Video presentation interrupted",e);}
                if(frame==null) {
                    if(finished.get()) {if(failure.get()!=null)throw new IOException("Video frame reader failed",failure.get());return;}
                    continue;
                }
                if(closed)return;
                listener.onFrame(frame,w,h);everPublishedFrame=true;++publishedFrames;
                long now=System.nanoTime();
                // Preserve the timeline across ordinary OS timer oversleep rather than
                // adding that delay to every subsequent frame. Reset only after a
                // substantial interruption so there is no unbounded catch-up burst.
                if(nextFrameAt==0)nextFrameAt=now;
                nextFrameAt+=interval;
                if(now<nextFrameAt)park(nextFrameAt-now);
                else if(now-nextFrameAt>250_000_000L)nextFrameAt=now;
            }
        } finally {reader.interrupt();if(frameReader==reader)frameReader=null;}
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
            p = startFfmpeg(new ProcessBuilder(VideoDecoder.ffmpegPath(), "-nostdin", "-hide_banner", "-i", file.toString())
                .redirectErrorStream(true).redirectOutput(report.toFile()));
            process = p;
            if (closed || !p.waitFor(10, TimeUnit.SECONDS)) return null;
            String out = Files.readString(report, StandardCharsets.UTF_8);
            hasAudio = out.contains("Audio:");
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

    private static Process startFfmpeg(ProcessBuilder builder) throws IOException {
        try { return builder.start(); }
        catch (IOException e) {
            if (builder.command().get(0).equals("ffmpeg")) throw e;
            systemOnly = true;
            VideoMenuClient.LOGGER.warn("Bundled FFmpeg cannot execute here; trying system FFmpeg", e);
            builder.command().set(0,"ffmpeg"); return builder.start();
        }
    }
    private static synchronized String ffmpegPath() {
        if (systemOnly) return "ffmpeg";
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
        String arch=System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        boolean arm=arch.equals("aarch64") || arch.equals("arm64");
        if (os.contains("mac") || os.contains("darwin")) {
            return extractCompressedEngine(arm ? "darwin-arm64" : "darwin-x64");
        } else if (os.contains("win")) {
            platform = "win64";
            exeName = "ffmpeg.exe";
        } else {
            if (arm) return extractCompressedEngine("linux-arm64");
            if (!arch.matches("amd64|x86_64")) return null;
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
            long expectedSize = VideoDecoder.class.getClassLoader().getResource("assets/videomenu/ffmpeg/"+platform+"/"+exeName).openConnection().getContentLengthLong();
            if (!Files.isRegularFile(out, new LinkOption[0]) || expectedSize>0 && Files.size(out)!=expectedSize) {
                Path tmp = dir.resolve(exeName + ".tmp");
                Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
                if (!os.contains("win")) {
                    tmp.toFile().setExecutable(true, false);
                }
                Files.move(tmp, out, StandardCopyOption.REPLACE_EXISTING);
                VideoMenuClient.LOGGER.info("[VideoMenu] Dahili FFmpeg '{}' klasörüne çıkarıldı.", out);
            }
            if (!os.contains("win") && !Files.isExecutable(out)) out.toFile().setExecutable(true, true);
            if (!os.contains("win") && !Files.isExecutable(out)) return null;
            return out;
        } catch (Throwable t) {
            VideoMenuClient.LOGGER.warn("[VideoMenu] Dahili FFmpeg çıkarılamadı; sistem ffmpeg'i kullanılacak.", t);
            return null;
        }
    }

    private static Path extractCompressedEngine(String platform) {
        String root="assets/videomenu/ffmpeg/";
        Path temporary=null;
        try (InputStream metadata=VideoDecoder.class.getClassLoader().getResourceAsStream(root+"PLATFORMS.json")) {
            if(metadata==null)return null;
            var manifest=com.google.gson.JsonParser.parseString(new String(metadata.readAllBytes(),StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject(platform);
            String expected=manifest.get("binary_sha256").getAsString();
            Path dir=FabricLoader.getInstance().getGameDir().resolve(".evatex-ffmpeg").resolve("6.1.1").resolve(platform);
            Files.createDirectories(dir);Path output=dir.resolve("ffmpeg");
            if(!Files.isRegularFile(output) || !sha256(output).equals(expected)) {
                temporary=Files.createTempFile(dir,"ffmpeg-",".tmp");
                try(InputStream source=VideoDecoder.class.getClassLoader().getResourceAsStream(root+platform+"/ffmpeg.gz")) {
                    if(source==null)return null;
                    try(var gzip=new java.util.zip.GZIPInputStream(source);var destination=Files.newOutputStream(temporary)) {
                        byte[] buffer=new byte[65536];long total=0;int n;
                        while((n=gzip.read(buffer))!=-1) {total+=n;if(total>256L*1024*1024)throw new IOException("FFmpeg engine exceeds size limit");destination.write(buffer,0,n);}
                    }
                }
                if(!sha256(temporary).equals(expected))throw new IOException("FFmpeg engine checksum mismatch");
                Files.move(temporary,output,StandardCopyOption.REPLACE_EXISTING);
            }
            if(!Files.isExecutable(output))output.toFile().setExecutable(true,true);
            return Files.isExecutable(output) ? output : null;
        } catch(Exception e) {VideoMenuClient.LOGGER.warn("Cannot prepare platform FFmpeg; trying system FFmpeg",e);return null;}
        finally {if(temporary!=null)try{Files.deleteIfExists(temporary);}catch(IOException ignored){}}
    }
    private static String sha256(Path path) throws Exception {
        var digest=java.security.MessageDigest.getInstance("SHA-256");
        try(InputStream input=Files.newInputStream(path)) {byte[] buffer=new byte[65536];int n;while((n=input.read(buffer))!=-1)digest.update(buffer,0,n);}
        return java.util.HexFormat.of().formatHex(digest.digest());
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
