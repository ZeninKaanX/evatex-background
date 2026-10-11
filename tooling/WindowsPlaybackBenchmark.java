import com.videomenu.VideoMenuConfig;
import com.videomenu.video.VideoDecoder;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

/** Measures the real decoder, transport, packing and two copies; excludes OpenGL. */
public final class WindowsPlaybackBenchmark {
    public static void main(String[] args) throws Exception {
        boolean hardware = Boolean.parseBoolean(args[1]);
        int height = Integer.parseInt(args[2]);
        VideoMenuConfig config = new VideoMenuConfig();
        config.videoPath = Path.of(args[0]).toAbsolutePath().toString();
        config.fps = 30; config.maxDecodeHeight = height;
        config.hardwareAcceleration = hardware;
        config.audioEnabled = false; config.audioVolume = .1;
        AtomicLong frames = new AtomicLong(), last = new AtomicLong(), maxGap = new AtomicLong();
        AtomicLong measuring = new AtomicLong();
        int[][] staging = new int[1][];
        IntBuffer[] nativePixels = new IntBuffer[1];
        VideoDecoder decoder = new VideoDecoder(config, (pixels,w,h) -> {
            if (staging[0] == null) {
                staging[0] = new int[pixels.length];
                nativePixels[0] = ByteBuffer.allocateDirect(pixels.length * 4)
                    .order(ByteOrder.nativeOrder()).asIntBuffer();
            }
            System.arraycopy(pixels,0,staging[0],0,pixels.length);
            nativePixels[0].clear(); nativePixels[0].put(staging[0]);
            if (measuring.get() != 0) {
                long now = System.nanoTime(), previous = last.getAndSet(now);
                if (previous != 0) maxGap.accumulateAndGet(now-previous,Math::max);
                frames.incrementAndGet();
            }
        }, () -> {}, true);
        try {
            if (!decoder.start()) throw new AssertionError("Decoder failed to start");
            long deadline = System.nanoTime()+35_000_000_000L;
            while (decoder.publishedFrames()<60 && System.nanoTime()<deadline) Thread.sleep(100);
            if (decoder.publishedFrames()<60) throw new AssertionError("No sustained playback: "+decoder.lastError());
            var bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            var child = ProcessHandle.current().descendants().filter(p -> p.info().command()
                .orElse("").toLowerCase().contains("ffmpeg")).findFirst().orElseThrow();
            long childBefore = child.info().totalCpuDuration().orElseThrow().toNanos();
            long javaBefore = bean.getProcessCpuTime(), start = System.nanoTime();
            measuring.set(1); Thread.sleep(12000); measuring.set(0);
            double seconds = (System.nanoTime()-start)/1e9;
            double javaCores = (bean.getProcessCpuTime()-javaBefore)/1e9/seconds;
            double ffmpegCores = (child.info().totalCpuDuration().orElseThrow().toNanos()-childBefore)/1e9/seconds;
            double fps = frames.get()/seconds;
            String report = String.format(java.util.Locale.ROOT,
                "{\"hardware_requested\":%s,\"height\":%d,\"backend\":\"%s\",\"fps\":%.2f,\"java_cpu_cores\":%.3f,\"ffmpeg_cpu_cores\":%.3f,\"max_gap_ms\":%.1f,\"logical_cpus\":%d,\"includes_opengl\":false}",
                hardware,height,decoder.decoderBackend(),fps,javaCores,ffmpegCores,maxGap.get()/1e6,
                Runtime.getRuntime().availableProcessors());
            System.out.println("BENCHMARK_JSON "+report);
            System.out.println("::notice title=Windows playback measurement::"+report);
            if (fps<27) throw new AssertionError("30 FPS decoder cannot sustain playback: "+fps);
        } finally { decoder.close(); }
    }
}
