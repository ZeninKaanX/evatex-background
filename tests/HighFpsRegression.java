import com.videomenu.VideoMenuConfig;
import com.videomenu.video.VideoDecoder;
import com.videomenu.video.FrameRatePolicy;
import java.nio.file.Path;

/** Real FFmpeg test: high ceiling must not duplicate low-FPS frames or change playback time. */
public final class HighFpsRegression {
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception {
        check(FrameRatePolicy.parse("Stream #0: Video: h264, 1920x1080, 29.97 fps, 30 tbr")==29.97,"decimal stream FPS");
        check(Math.abs(FrameRatePolicy.parse("Video: h264, 30000/1001 fps")-30000.0/1001)<1e-9,"rational FPS");
        check(Double.isNaN(FrameRatePolicy.parse("Video: h264, 60 tbr")),"do not mistake codec time base for FPS");
        check(FrameRatePolicy.limit(120,30)==30,"120 ceiling does not quadruple 30-FPS work");
        check(FrameRatePolicy.limit(120,60)==60,"120 ceiling does not double 60-FPS work");
        check(FrameRatePolicy.limit(120,120)==120,"native 120-FPS remains available");
        check(FrameRatePolicy.limit(60,120)==60,"lower user ceiling respected");
        check(FrameRatePolicy.limit(120,Double.NaN)==120,"unknown FPS retains configured ceiling");
        for(int i=0;i<args.length;i+=2) {
            double source=Double.parseDouble(args[i+1]);
            var cfg=new VideoMenuConfig();cfg.videoPath=Path.of(args[i]).toAbsolutePath().toString();cfg.fps=120;cfg.audioEnabled=false;
            var d=new VideoDecoder(cfg,(pixels,w,h)->{},()->{},true);
            try {
                check(d.start(),"fixture starts");
                long deadline=System.nanoTime()+10_000_000_000L;
                while(d.publishedFrames()<3&&System.nanoTime()<deadline)Thread.sleep(10);
                check(d.publishedFrames()>=3,"first frames arrive: "+d.lastError());
                check(Math.abs(d.effectiveFps()-source)<.02,"source ceiling detected: "+d.effectiveFps());
                long n=d.publishedFrames(),start=System.nanoTime();Thread.sleep(1600);
                double seconds=(System.nanoTime()-start)/1e9;long frames=d.publishedFrames()-n;
                check(Math.abs(frames-source*seconds)<Math.max(5,source*.2),"wall-clock playback speed: "+frames+" over "+seconds);
                d.setPaused(true);Thread.sleep(150);long paused=d.publishedFrames();Thread.sleep(250);check(d.publishedFrames()==paused,"pause at high ceiling");
                d.setPaused(false);Thread.sleep(300);check(d.publishedFrames()>paused,"same player resumes");
                System.out.println("PASS ceiling=120, source="+source+", effective="+d.effectiveFps()+", sampled="+frames+" frames in "+seconds+"s; pause/resume");
            } finally {d.close();}
        }
    }
}
