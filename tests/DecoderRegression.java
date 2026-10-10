import com.videomenu.VideoMenuConfig;
import com.videomenu.VideoFiles;
import com.videomenu.AstralTheme;
import com.videomenu.video.VideoDecoder;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DecoderRegression {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        VideoMenuConfig invalid = new VideoMenuConfig();
        invalid.fps = -100; invalid.scale = Double.NaN; invalid.darkening = Double.POSITIVE_INFINITY; invalid.videoPath = null;
        invalid.validate();
        check(invalid.fps == 15 && invalid.scale == 1 && invalid.darkening == .25 && invalid.videoPath.isEmpty(), "invalid config normalization");
        check(AstralTheme.blend(0x00000000,0xFFFFFFFF,.5)==0x80808080,"palette interpolation");
        invalid.fps = Integer.MAX_VALUE; invalid.scale = 50; invalid.darkening = -9; invalid.validate();
        check(invalid.fps == 120 && invalid.scale == 1 && invalid.darkening == 0, "config upper/lower bounds");
        check(VideoFiles.resolve("invalid\0path") == null, "invalid path rejected");
        Method pack = VideoDecoder.class.getDeclaredMethod("pack", byte[].class, int.class, int.class, int[].class);
        pack.setAccessible(true);
        int[] packed = new int[2];
        pack.invoke(null, new byte[]{(byte)255,0,0,(byte)255,0,0,(byte)255,(byte)255}, 2,1,packed);
        check(packed[0] == 0xFF0000FF && packed[1] == 0xFFFF0000, "RGBA -> native ABGR channel order");
        Method target = VideoDecoder.class.getDeclaredMethod("computeTarget", int.class,int.class); target.setAccessible(true);
        VideoMenuConfig config = new VideoMenuConfig();
        VideoDecoder sizing = new VideoDecoder(config, (frame,w,h) -> {});
        int[] size = (int[])target.invoke(sizing,7680,4320);
        check(size[0] == 3840 && size[1] == 2160,"4K cap");
        size = (int[])target.invoke(sizing,1080,1920);
        check(size[0] == 1080 && size[1] == 1920,"portrait dimensions");
        check(!new VideoDecoder(config,(frame,w,h)->{}).start(),"empty video gracefully rejected");
        for (int fps : new int[]{15,60}) {
            config.videoPath = Path.of(args[0]).toAbsolutePath().toString(); config.fps = fps;
            CountDownLatch enough = new CountDownLatch(1);
            AtomicInteger count = new AtomicInteger();
            int[][] first = new int[1][];
            int[][] snapshot = new int[1][];
            long[] firstAt = new long[1], lastAt = new long[1];
            VideoDecoder decoder = new VideoDecoder(config,(frame,w,h)-> {
                check(w==160 && h==90,"native resolution preserved");
                int n=count.incrementAndGet();
                if (n==1) { first[0]=frame; snapshot[0]=frame.clone(); firstAt[0]=System.nanoTime(); }
                if (n==fps*3+1) { lastAt[0]=System.nanoTime(); enough.countDown(); }
            });
            check(decoder.start(),"decoder starts");
            check(enough.await(10, TimeUnit.SECONDS),"video loops past clip duration");
            decoder.close();
            double elapsed = (lastAt[0]-firstAt[0])/1e9;
            check(elapsed >= 2.75 && elapsed < 4.5,"three video seconds at "+fps+" FPS: "+elapsed);
            check(Arrays.equals(first[0],snapshot[0]),"published frame remains immutable");
            Thread.sleep(200);
            long closedCount=decoder.publishedFrames(); Thread.sleep(200);
            check(closedCount==decoder.publishedFrames(),"close stops publishing");
            System.out.printf("PASS %d FPS: %.3fs, loop, immutable frames, shutdown%n",fps,elapsed);
        }
        System.out.println("PASS config bounds, invalid paths, color ordering, native/4K resolution");
    }
}
