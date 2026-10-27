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
import com.videomenu.EvatexPlatform;

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
    private volatile double effectiveFps;
    private boolean stillImage;
    private boolean gif;
    private volatile LocalMediaPipe videoServer;
    private final boolean socketTransport = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows");
    private final java.util.List<Backend> backendCandidates;
    private volatile boolean closed, paused;
    private final Object pauseLock=new Object();
    private volatile Process process;
    private volatile long publishedFrames,decodedFrames;
    private volatile boolean presentationEnabled=true;
    private Thread thread;
    private volatile Thread frameReader;
    private volatile VideoAudio audio;
    private volatile boolean audioEnabled;
    private volatile double audioVolume;
    private volatile boolean hasAudio;
    private String downloadFormat = "nv12";
    private volatile String decoderBackend = "starting";
    private volatile String lastError = "";
    private volatile String failureStatus="error";
    public String failureStatus(){return failureStatus;}

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
        this.effectiveFps = config.fps;
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
        if(stillImage && readStillImage(file))return true;
        int[] size = this.probeSize(file);
        if (size == null) {
            VideoMenuClient.LOGGER.error("[VideoMenu] FFmpeg bulunamadı veya video okunamadı: '{}'. Oynatma motoru ve dosya erişimini günlükten kontrol edin.", this.config.videoPath);
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

    public void setPaused(boolean value) {
        synchronized(pauseLock){if(paused==value)return;paused=value;if(audio!=null)audio.paused(value);pauseLock.notifyAll();}
    }
    private long awaitResume() throws InterruptedException {
        if(!paused)return 0;
        long since=System.nanoTime();
        synchronized(pauseLock){while(paused && !closed)pauseLock.wait();}
        return System.nanoTime()-since;
    }
    public boolean isPaused(){return paused;}
    public void setPresentationEnabled(boolean value){presentationEnabled=value;}
    public boolean isPresentationEnabled(){return presentationEnabled;}
    public boolean hasAudioTrack(){return hasAudio;}
    public long decodedFrames(){return decodedFrames;}
    public double effectiveFps(){return effectiveFps;}

    public void close() {
        this.closed = true;
        synchronized(pauseLock){pauseLock.notifyAll();}
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
    public String decodedSize() { return decodedWidth+"x"+decodedHeight; }
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
            long framesBefore = decodedFrames;
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
                            if(paused){deadline=System.nanoTime()+12_000_000_000L;continue;}
                            if (decodedFrames == framesBefore && System.nanoTime() >= deadline) {
                                lastError = "Decoder did not produce its first frame within 12 seconds";
                                p.destroyForcibly(); return;
                            }
                            VideoAudio sink = audio;
                            if (sink != null && sink.streamEnded() && decodedFrames > framesBefore) {
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
                if (stillImage && decodedFrames > framesBefore) return;
                if (audioEnded.get()) {
                    if (decodedFrames-framesBefore >= config.fps*10L) audioRestarts = 0;
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
                if (decodedFrames == framesBefore) {
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
                    if (decodedFrames-framesBefore >= config.fps*10L) audioRestarts = 0;
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
                if (decodedFrames == framesBefore && ++emptyAttempts >= 2) {
                    VideoMenuClient.LOGGER.error("Cannot decode video {}", config.videoPath, e); return;
                }
                VideoMenuClient.LOGGER.warn("Video decoder interrupted; retrying", e);
                park(2_000_000_000L);
            }
            if (!closed) park(200_000_000L);
        }
    }

    static String videoFilter(Backend backend, int fps, int width, int height, String format) {
        return videoFilter(backend, (double)fps, width, height, format);
    }

    static String videoFilter(Backend backend, double fps, int width, int height, String format) {
        String rate = fps == Math.rint(fps) ? Long.toString((long)fps) : Double.toString(fps);
        String prefix = "fps=" + rate + ",";
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
        String filter = videoFilter(backend, effectiveFps, dstW, dstH, downloadFormat);
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
            LocalMediaPipe server = new LocalMediaPipe(262144);
            videoServer = server;
            cmd.add(server.outputUrl());
        } else cmd.add("pipe:1");
        if (hasAudio) {
            try { synchronized(pauseLock){audio = new VideoAudio(audioEnabled,audioVolume);audio.paused(paused);} }
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
        LocalMediaPipe server = videoServer;
        if (server == null) throw new IOException("Video connection closed");
        return server.accept(() -> !closed && p.isAlive());
    }

    private void closeVideoTransport() {
        LocalMediaPipe server = videoServer; videoServer = null;
        if (server != null) server.close();
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

    private static final class DecodedFrame {
        final int[] pixels;boolean visual;
        DecodedFrame(int[] pixels){this.pixels=pixels;}
    }
    private void readFrames(InputStream in,int w,int h)throws IOException {
        var frames=new java.util.concurrent.ArrayBlockingQueue<DecodedFrame>(4);
        var available=new java.util.concurrent.ArrayBlockingQueue<DecodedFrame>(6);
        for(int i=0;i<(stillImage?2:6);i++)available.add(new DecodedFrame(new int[w*h]));
        var finished=new java.util.concurrent.atomic.AtomicBoolean();
        var failure=new java.util.concurrent.atomic.AtomicReference<Throwable>();
        Thread reader=new Thread(()->{
            byte[] rgba=new byte[w*h*4];
            try {
                while(!closed&&!Thread.currentThread().isInterrupted()){
                    awaitResume();if(closed)break;
                    DecodedFrame frame=available.take();
                    if(!readFully(in,rgba)){available.offer(frame);break;}
                    frame.visual=presentationEnabled;
                    // Opaque EVATEX menus retain the shared audio timeline without
                    // packing Java pixels, publishing frames, or uploading textures.
                    if(frame.visual)pack(rgba,w,h,frame.pixels);
                    frames.put(frame);
                }
            }catch(InterruptedException e){Thread.currentThread().interrupt();}
            catch(Throwable e){if(!closed)failure.set(e);}
            finally{finished.set(true);}
        },"EVATEX-FrameReader");
        reader.setDaemon(true);frameReader=reader;reader.start();
        long interval=Math.round(1_000_000_000.0/Math.max(1.0,effectiveFps)),nextFrameAt=0;
        try {
            while(!closed){
                DecodedFrame frame;
                try {
                    long waited=awaitResume();if(nextFrameAt!=0)nextFrameAt+=waited;
                    if(closed)return;frame=frames.poll(100,TimeUnit.MILLISECONDS);
                    waited=awaitResume();if(nextFrameAt!=0)nextFrameAt+=waited;
                }catch(InterruptedException e){Thread.currentThread().interrupt();if(closed)return;throw new IOException("Video presentation interrupted",e);}
                if(frame==null){if(finished.get()){if(failure.get()!=null)throw new IOException("Video frame reader failed",failure.get());return;}continue;}
                if(closed)return;
                if(decoderBackend.startsWith("starting ")){
                    decoderBackend=decoderBackend.substring(9);
                    VideoMenuClient.LOGGER.info("EVATEX video decoder active: {} ({}x{} at {} FPS; {})",decoderBackend,w,h,effectiveFps,socketTransport?"local TCP":"pipe");
                }
                try {
                    if(presentationEnabled&&frame.visual){listener.onFrame(borrowedFrames?frame.pixels:frame.pixels.clone(),w,h);++publishedFrames;}
                    ++decodedFrames;
                }finally{available.offer(frame);}
                long now=System.nanoTime();if(nextFrameAt==0)nextFrameAt=now;nextFrameAt+=interval;
                if(now<nextFrameAt)park(nextFrameAt-now);else if(now-nextFrameAt>250_000_000L)nextFrameAt=now;
            }
        }finally{reader.interrupt();if(frameReader==reader)frameReader=null;}
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

    /** Standard photos do not need an external executable. Read a bounded image once. */
    private boolean readStillImage(Path file) {
        try(var input=javax.imageio.ImageIO.createImageInputStream(file.toFile())) {
            if(input==null)return false;
            var readers=javax.imageio.ImageIO.getImageReaders(input);
            if(!readers.hasNext())return false;
            var reader=readers.next();
            try {
                reader.setInput(input,true,true);
                sourceWidth=reader.getWidth(0);sourceHeight=reader.getHeight(0);
                if(sourceWidth<=0 || sourceHeight<=0)return false;
                int[] target=computeTarget(sourceWidth,sourceHeight);
                var param=reader.getDefaultReadParam();
                int sample=Math.max(1,Math.min(sourceWidth/target[0],sourceHeight/target[1]));
                param.setSourceSubsampling(sample,sample,0,0);
                var source=reader.read(0,param);
                var image=new java.awt.image.BufferedImage(target[0],target[1],java.awt.image.BufferedImage.TYPE_INT_ARGB);
                var graphics=image.createGraphics();
                try {graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);graphics.drawImage(source,0,0,target[0],target[1],null);}
                finally {graphics.dispose();source.flush();}
                int[] frame=image.getRGB(0,0,target[0],target[1],null,0,target[0]);image.flush();
                for(int i=0;i<frame.length;i++){int argb=frame[i];frame[i]=(argb&0xFF00FF00)|((argb>>>16)&255)|((argb&255)<<16);}
                if(closed)return false;
                decodedWidth=target[0];decodedHeight=target[1];decoderBackend="IMAGE_IO";
                listener.onFrame(frame,target[0],target[1]);++publishedFrames;++decodedFrames;
                VideoMenuClient.LOGGER.info("EVATEX photo decoder active: IMAGE_IO ({}x{})",target[0],target[1]);
                return true;
            }finally{reader.dispose();}
        }catch(IOException|RuntimeException e){VideoMenuClient.LOGGER.warn("Native photo reader unavailable; trying FFmpeg",e);return false;}
    }
    private static String engineFailure(Throwable error) {
        return engineFailure(error,0);
    }
    private static String engineFailure(Throwable error,int depth) {
        if(error==null || depth>8)return "error";
        String message=String.valueOf(error.getMessage()).toLowerCase(Locale.ROOT);
        if(message.contains("error=5") || error instanceof java.nio.file.AccessDeniedException || message.contains("permission denied"))return "engine_blocked";
        for(Throwable suppressed:error.getSuppressed())if(engineFailure(suppressed,depth+1).equals("engine_blocked"))return "engine_blocked";
        if(engineFailure(error.getCause(),depth+1).equals("engine_blocked"))return "engine_blocked";
        return message.contains("cannot run program") || message.contains("error=2") ? "engine_missing" : "error";
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
                // FPS is a ceiling, not a request to synthesize duplicate RGBA frames.
                if (!stillImage && !gif) effectiveFps = FrameRatePolicy.limit(config.fps, FrameRatePolicy.parse(line));
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
            failureStatus=engineFailure(e);
            VideoMenuClient.LOGGER.warn("Cannot probe video {} ({} )", file, failureStatus, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (p != null) p.destroyForcibly();
            process = null;
            if (report != null) try { Files.deleteIfExists(report); } catch (IOException ignored) {}
        }
        return null;
    }

    /** A silent, single-frame poster; callers own timeout and process cleanup. */
    public static Process startPosterProcess(java.nio.file.Path source, java.nio.file.Path output) throws IOException {
        return startFfmpeg(new ProcessBuilder(ffmpegPath(), "-nostdin", "-hide_banner", "-loglevel", "error",
            "-threads", "1", "-ss", "1", "-i", source.toString(), "-an", "-sn", "-frames:v", "1",
            "-vf", "scale=768:432:force_original_aspect_ratio=decrease", "-threads", "1", "-y", output.toString())
            .redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD));
    }

    private static Process startFfmpeg(ProcessBuilder builder) throws IOException {
        try { return builder.start(); }
        catch (IOException e) {
            if (builder.command().get(0).equals("ffmpeg")) throw e;
            VideoMenuClient.LOGGER.warn("Bundled FFmpeg cannot execute here; trying system FFmpeg", e);
            builder.command().set(0,"ffmpeg");
            try {Process fallback=builder.start();systemOnly=true;return fallback;}
            catch(IOException missing){missing.addSuppressed(e);throw missing;}
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
            Path dir = EvatexPlatform.gameDir().resolve(".evatex-ffmpeg").resolve("1.8.0").resolve(platform);
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
            Path dir=EvatexPlatform.gameDir().resolve(".evatex-ffmpeg").resolve("6.1.1").resolve(platform);
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
