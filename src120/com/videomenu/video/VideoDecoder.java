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
    private static final Pattern DIM_PATTERN = Pattern.compile("\\b([1-9]\\d{0,4})x([1-9]\\d{0,4})\\b");
    private static volatile Path bundledFfmpeg;
    private static volatile boolean systemOnly;
    private final VideoMenuConfig config;
    private final FrameListener listener;
    private final Runnable onFailure;
    private final boolean borrowedFrames;
    private final int viewportWidth, viewportHeight;
    private volatile int sourceWidth, sourceHeight, decodedWidth, decodedHeight;
    private boolean stillImage;
    private boolean gif;
    private volatile java.net.ServerSocket videoServer;
    private volatile java.net.Socket videoSocket;
    private final boolean socketTransport = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows");
    private final java.util.List<Backend> backendCandidates;
    private volatile boolean closed;
    private volatile Process process;
    private volatile long publishedFrames;
    private Thread thread;
    private volatile Thread frameReader;
    private volatile VideoAudio audio;
    private volatile boolean audioEnabled;
    private volatile double audioVolume;
    private boolean hasAudio;
    private String downloadFormat = "nv12";
    private volatile String decoderBackend = "starting";
    private volatile String lastError = "";

    public VideoDecoder(VideoMenuConfig config, FrameListener listener) { this(config, listener, () -> {}); }

    public VideoDecoder(VideoMenuConfig config, FrameListener listener, Runnable onFailure) {
        this(config, listener, onFailure, false);
    }

    /** With borrowedFrames, pixels are valid only during the synchronous callback. */
    public VideoDecoder(VideoMenuConfig config, FrameListener listener, Runnable onFailure, boolean borrowedFrames) {
        this(config, listener, onFailure, borrowedFrames, 0, 0);
    }

    public VideoDecoder(VideoMenuConfig config, FrameListener listener, Runnable onFailure, boolean borrowedFrames,
                        int viewportWidth, int viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.borrowedFrames = borrowedFrames;
        this.onFailure = onFailure;
        this.config = config.copy();
        this.audioEnabled = config.audioEnabled;
        this.audioVolume = config.audioVolume;
        this.listener = listener;
        this.backendCandidates = backends(System.getProperty("os.name", ""), config.hardwareAcceleration);
    }

    public boolean start() {
        if (closed) return false;
        Path file = this.resolveVideoFile();
        if (file == null || !Files.isRegularFile(file, new LinkOption[0])) {
            VideoMenuClient.LOGGER.error("[VideoMenu] Video dosyası bulunamadı: '{}'", this.config.videoPath);
            return false;
        }
        stillImage = VideoFiles.isStillImage(file);
        gif = VideoFiles.isGif(file);
        int[] size = this.probeSize(file);
        if (size == null) {
            VideoMenuClient.LOGGER.error("[VideoMenu] FFmpeg bulunamadı veya video okunamadı: '{}'. MP4 (H.264) bir dosya kullanın.", this.config.videoPath);
            return false;
        }
        if (closed) return false;
        this.thread = new Thread(() -> {
            try { this.run(file, size[0], size[1]); }
            finally { if (!closed && !(stillImage && publishedFrames > 0)) onFailure.run(); }
        }, "VideoMenu-Decoder");
        this.thread.setDaemon(true);
        this.thread.start();
        return true;
    }

    public void close() {
        this.closed = true;
        closeVideoTransport();
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
    public String decoderBackend() { return decoderBackend; }
    public long publishedFrames() {
        return this.publishedFrames;
    }

    // Hardware frames are explicitly required: a successful first frame verifies
    // this backend instead of silently reporting software decoding as GPU playback.
    enum Backend { D3D11_SCALE, D3D11, SOFTWARE }

    static java.util.List<Backend> backends(String os, boolean hardware) {
        return hardware && os.toLowerCase(Locale.ROOT).startsWith("windows")
            ? java.util.List.of(Backend.D3D11_SCALE, Backend.D3D11, Backend.SOFTWARE)
            : java.util.List.of(Backend.SOFTWARE);
    }

    private void run(Path file, int srcW, int srcH) {
        sourceWidth = srcW; sourceHeight = srcH;
        int[] target = this.computeTarget(srcW, srcH);
        decodedWidth = target[0]; decodedHeight = target[1];
        var candidates = stillImage || gif ? java.util.List.of(Backend.SOFTWARE) : backendCandidates;
        int attempt = 0;
        boolean streamLoop = !stillImage;
        int emptyAttempts = 0, audioRestarts = 0;
        while (!closed) {
            Backend backend = candidates.get(attempt);
            long framesBefore = publishedFrames;
            var audioEnded = new java.util.concurrent.atomic.AtomicBoolean();
            try {
                Process p;
                process = p = spawnFfmpeg(file, target[0], target[1], streamLoop, backend);
                // Monitor startup and a prematurely ended audio output independently
                // of rendering; video may continue even after the PCM connection ends.
                Thread startup = new Thread(() -> {
                    long deadline = System.nanoTime() + 12_000_000_000L;
                    try {
                        while (!closed && process == p && p.isAlive()) {
                            Thread.sleep(500);
                            if (publishedFrames == framesBefore && System.nanoTime() >= deadline) {
                                lastError = "Decoder did not produce its first frame within 12 seconds";
                                p.destroyForcibly(); return;
                            }
                            VideoAudio sink = audio;
                            if (sink != null && sink.streamEnded() && publishedFrames > framesBefore) {
                                audioEnded.set(true);
                                lastError = "PCM stream ended while video playback continued";
                                p.destroyForcibly(); return;
                            }
                        }
                    } catch (InterruptedException ignored) {}
                }, "EVATEX-Decoder-Startup");
                startup.setDaemon(true); startup.start();
                try {
                    try (InputStream input = videoInput(p)) { readFrames(input, target[0], target[1]); }
                    waitForExit(p);
                } finally {
                    startup.interrupt();
                    closeVideoTransport();
                    p.destroyForcibly();
                    process = null;
                    VideoAudio sink = audio; audio = null; if (sink != null) sink.close();
                }
                if (closed) return;
                if (stillImage && publishedFrames > framesBefore) return;
                if (audioEnded.get()) {
                    if (publishedFrames-framesBefore >= config.fps*10L) audioRestarts = 0;
                    if (++audioRestarts > 2) {
                        VideoMenuClient.LOGGER.error("Audio output repeatedly ended during video playback: {}", lastError);
                        return;
                    }
                    VideoMenuClient.LOGGER.warn("Restarting shared video/audio decoder after PCM output ended");
                    continue;
                }
                if (backend != Backend.SOFTWARE) {
                    // -stream_loop should never end. Retry without a failed GPU path,
                    // including failures after playback has already started.
                    VideoMenuClient.LOGGER.warn("EVATEX {} stopped; trying next decoder: {}", backend, lastError);
                    ++attempt;
                    continue;
                }
                if (publishedFrames == framesBefore) {
                    if (streamLoop) { streamLoop = false; continue; }
                    if (++emptyAttempts >= 2) {
                        VideoMenuClient.LOGGER.error("Cannot decode video {}: {}", config.videoPath, lastError);
                        return;
                    }
                } else emptyAttempts = 0;
            } catch (Exception e) {
                closeVideoTransport();
                Process failed = process; if (failed != null) failed.destroyForcibly(); process = null;
                if (closed) return;
                VideoAudio sink = audio; audio = null; if (sink != null) sink.close();
                if (audioEnded.get()) {
                    if (publishedFrames-framesBefore >= config.fps*10L) audioRestarts = 0;
                    if (++audioRestarts > 2) {
                        VideoMenuClient.LOGGER.error("Audio output repeatedly ended during video playback", e);
                        return;
                    }
                    VideoMenuClient.LOGGER.warn("Restarting shared video/audio decoder after PCM transport failure");
                    continue;
                }
                if (backend != Backend.SOFTWARE) {
                    VideoMenuClient.LOGGER.warn("EVATEX {} unavailable; trying next decoder", backend, e);
                    ++attempt; continue;
                }
                if (publishedFrames == framesBefore && ++emptyAttempts >= 2) {
                    VideoMenuClient.LOGGER.error("Cannot decode video {}", config.videoPath, e); return;
                }
                VideoMenuClient.LOGGER.warn("Video decoder interrupted; retrying", e);
                park(2_000_000_000L);
            }
            if (!closed) park(200_000_000L);
        }
    }

    static String videoFilter(Backend backend, int fps, int width, int height, String format) {
        String prefix = "fps=" + fps + ",";
        if (backend == Backend.D3D11_SCALE) {
            // Drop surplus frames and shrink on the GPU before transferring pixels.
            return prefix + "scale_d3d11=width=" + width + ":height=" + height + ":format=nv12,"
                + "hwdownload,format=nv12,setsar=1,format=rgba";
        }
        if (backend == Backend.D3D11) prefix += "hwdownload,format=" + format + ",";
        // Use the lighter CPU resampler on Windows, including hybrid GPU decode.
        String resampler = backend != Backend.SOFTWARE || System.getProperty("os.name", "")
            .toLowerCase(Locale.ROOT).startsWith("windows") ? "bicubic" : "lanczos";
        return prefix + "scale=" + width + ":" + height + ":flags=" + resampler + ",setsar=1,format=rgba";
    }

    private Process spawnFfmpeg(Path file, int dstW, int dstH, boolean streamLoop, Backend backend) throws IOException {
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
        if (backend != Backend.SOFTWARE) {
            cmd.addAll(java.util.List.of("-hwaccel", "d3d11va", "-hwaccel_output_format", "d3d11"));
        }
        cmd.add("-threads"); cmd.add("2");
        if (gif) cmd.addAll(java.util.List.of("-ignore_loop", "1"));
        if (stillImage) cmd.addAll(java.util.List.of("-f", "image2", "-pattern_type", "none"));
        cmd.add("-i");
        cmd.add(file.toString());
        cmd.add("-map");
        cmd.add("0:v:0");
        cmd.add("-an");
        cmd.add("-sn");
        cmd.add("-dn");
        cmd.add("-vf");
        String filter = videoFilter(backend, config.fps, dstW, dstH, downloadFormat);
        if (stillImage) filter = filter.substring(filter.indexOf(',') + 1)
            .replace(":flags=bicubic,", ":flags=lanczos,");
        cmd.add(filter);
        if (stillImage) cmd.addAll(java.util.List.of("-frames:v", "1"));
        cmd.add("-f");
        cmd.add("rawvideo");
        cmd.add("-pix_fmt");
        cmd.add("rgba");
        cmd.add("-threads"); cmd.add("1");
        // Windows anonymous process pipes have small buffers. A bounded local TCP
        // connection avoids repeatedly stalling both FFmpeg and Java on those pipes.
        if (socketTransport) {
            java.net.ServerSocket server = new java.net.ServerSocket();
            videoServer = server;
            server.setReceiveBufferSize(262144);
            server.bind(new java.net.InetSocketAddress("127.0.0.1", 0), 1);
            server.setSoTimeout(500);
            cmd.add("tcp://127.0.0.1:" + server.getLocalPort());
        } else cmd.add("pipe:1");
        if (hasAudio) {
            try { audio = new VideoAudio(audioEnabled,audioVolume); }
            catch (IOException e) { VideoMenuClient.LOGGER.warn("Cannot initialize video audio; continuing silently",e); }
            if (audio != null) cmd.addAll(java.util.List.of("-map", "0:a:0", "-vn", "-sn", "-dn", "-ac", "2", "-ar", "48000", "-af", "aresample=async=1:first_pts=0", "-c:a", "pcm_s16le", "-f", "s16le", audio.outputUrl()));
        }
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectError(ProcessBuilder.Redirect.PIPE);
        if (socketTransport) pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        Process p;
        try { p = startFfmpeg(pb); }
        catch (IOException e) { VideoAudio sink = audio; audio = null; if (sink != null) sink.close(); throw e; }
        decoderBackend = "starting " + backend;
        lastError = "";
        Thread errors = new Thread(() -> {
            try (var stream = p.getErrorStream()) {
                byte[] buffer = new byte[2048]; int n;
                while ((n = stream.read(buffer)) > 0) {
                    String combined = lastError + new String(buffer, 0, n, StandardCharsets.UTF_8);
                    lastError = combined.substring(Math.max(0, combined.length()-8192));
                }
            } catch (IOException ignored) {}
        }, "EVATEX-FFmpeg-Errors"); errors.setDaemon(true); errors.start();
        if (closed) { p.destroyForcibly(); VideoAudio sink = audio; if (sink != null) sink.close(); }
        return p;
    }

    private InputStream videoInput(Process p) throws IOException {
        if (!socketTransport) return p.getInputStream();
        java.net.ServerSocket server = videoServer;
        if (server == null) throw new IOException("Video connection closed");
        while (!closed) {
            try {
                java.net.Socket socket = server.accept();
                videoSocket = socket;
                if (closed) { socket.close(); throw new IOException("Video closed"); }
                return new java.io.BufferedInputStream(socket.getInputStream(), 262144);
            } catch (java.net.SocketTimeoutException ignored) { if (!p.isAlive()) break; }
        }
        throw new IOException("Decoder stopped before opening the video connection");
    }

    private void closeVideoTransport() {
        java.net.Socket socket = videoSocket; videoSocket = null;
        java.net.ServerSocket server = videoServer; videoServer = null;
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        try { if (server != null) server.close(); } catch (IOException ignored) {}
    }

    public boolean viewportChanged(int width, int height, boolean fill) {
        if (!socketTransport || stillImage || decodedWidth == 0 || width <= 0 || height <= 0) return false;
        int[] target = computeTarget(sourceWidth, sourceHeight, width, height, fill);
        return target[0] != decodedWidth || target[1] != decodedHeight;
    }

    private int[] computeTarget(int srcW, int srcH) {
        return computeTarget(srcW, srcH, viewportWidth, viewportHeight, config.fillScreen);
    }
    private int[] computeTarget(int srcW, int srcH, int viewportWidth, int viewportHeight, boolean fill) {
        double scale = Math.min(Math.max(this.config.scale, 0.05), 1.0);
        double limitHeight = config.maxDecodeHeight;
        // Bound Java-side frame storage independently of source resolution and worker count.
        double maxPixels = Math.min(7680.0*4320, Math.max(640.0*360, Runtime.getRuntime().maxMemory()/96.0));
        scale = Math.min(scale, Math.min(limitHeight*16.0/9.0/srcW, limitHeight/srcH));
        scale = Math.min(scale, Math.sqrt(maxPixels/((double)srcW*srcH)));
        // Avoid converting, copying and uploading pixels a Windows menu cannot display.
        // Use the fill/fit ratio so portrait and cropped wallpapers retain visible detail.
        if (socketTransport && !stillImage && viewportWidth > 0 && viewportHeight > 0) {
            double horizontal = (double)viewportWidth / srcW, vertical = (double)viewportHeight / srcH;
            scale = Math.min(scale, fill ? Math.max(horizontal, vertical) : Math.min(horizontal, vertical));
        }
        int dstW = Math.max(2, (int) Math.round((double) srcW * scale) & 0xFFFFFFFE);
        int dstH = Math.max(2, (int) Math.round((double) srcH * scale) & 0xFFFFFFFE);
        return new int[]{dstW, dstH};
    }

    private void readFrames(InputStream in, int w, int h) throws IOException {
        // Keep native pipe reads separate from the presentation clock. In particular,
        // macOS pipe wakeups can otherwise delay several frames after each paced read.
        var frames=new java.util.concurrent.ArrayBlockingQueue<int[]>(4);
        var available=new java.util.concurrent.ArrayBlockingQueue<int[]>(6);
        for (int i=0;i<(stillImage ? 2 : 6);i++) available.add(new int[w*h]);
        var finished=new java.util.concurrent.atomic.AtomicBoolean();
        var failure=new java.util.concurrent.atomic.AtomicReference<Throwable>();
        Thread reader=new Thread(()->{
            byte[] rgba=new byte[w*h*4];
            try {
                while(!closed && !Thread.currentThread().isInterrupted()) {
                    int[] pixels = available.take();
                    if (!readFully(in, rgba)) { available.offer(pixels); break; }
                    pack(rgba,w,h,pixels);frames.put(pixels);
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
                if (decoderBackend.startsWith("starting ")) {
                    decoderBackend = decoderBackend.substring(9);
                    VideoMenuClient.LOGGER.info("EVATEX video decoder active: {} ({}x{} at {} FPS; {})", decoderBackend, w, h, config.fps, socketTransport ? "local TCP" : "pipe");
                }
                try { listener.onFrame(borrowedFrames ? frame : frame.clone(),w,h);++publishedFrames; }
                finally { available.offer(frame); }
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
        // NativeImage expects little-endian ABGR ints. Bulk conversion avoids
        // four channel operations per pixel in Java for every decoded frame.
        java.nio.ByteBuffer.wrap(rgba).order(java.nio.ByteOrder.LITTLE_ENDIAN)
            .asIntBuffer().get(out, 0, w*h);
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
            ArrayList<String> probe = new ArrayList<>(java.util.List.of(VideoDecoder.ffmpegPath(), "-nostdin", "-hide_banner"));
            if (stillImage) probe.addAll(java.util.List.of("-f", "image2", "-pattern_type", "none"));
            probe.addAll(java.util.List.of("-i", file.toString()));
            p = startFfmpeg(new ProcessBuilder(probe).redirectErrorStream(true).redirectOutput(report.toFile()));
            process = p;
            if (closed || !p.waitFor(10, TimeUnit.SECONDS)) return null;
            String out = Files.readString(report, StandardCharsets.UTF_8);
            hasAudio = out.contains("Audio:");
            for (String line : out.split("\\R")) {
                if (!line.contains("Video:")) continue;
                downloadFormat = line.matches(".*(?:yuv420p10|p010|yuv420p12).*" ) ? "p010le" : "nv12";
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
