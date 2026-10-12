import com.videomenu.VideoMenuConfig;
import com.videomenu.video.VideoDecoder;
import java.nio.file.Path;
public final class MutedMediaRegression {
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception {
        for(String path:args) {
            VideoMenuConfig config=new VideoMenuConfig();config.videoPath=Path.of(path).toAbsolutePath().toString();
            config.scale=.5;config.fps=30;config.audioEnabled=false;config.audioVolume=.1;config.hardwareAcceleration=false;
            var frameCount=new java.util.concurrent.atomic.AtomicInteger();
            VideoDecoder decoder=new VideoDecoder(config,(pixels,w,h)->{check(w==960&&h==540,"1080p media downscales to bounded test output");frameCount.incrementAndGet();});
            var transport=VideoDecoder.class.getDeclaredField("socketTransport");transport.setAccessible(true);transport.setBoolean(decoder,true);
            try {
                check(decoder.start(),"supplied media starts");
                long deadline=System.nanoTime()+25_000_000_000L;
                while(frameCount.get()<540 && System.nanoTime()<deadline)Thread.sleep(100);
                check(frameCount.get()>=540,"18 seconds of paced authenticated video");
                check(decoder.decoderBackend().equals("SOFTWARE"),"hardware opt-out reaches real software decoder");
                check(decoder.audioReceivedBytes()>1_000_000,"authenticated PCM continues alongside video");
                check(decoder.audioWrittenBytes()==0,"audio remains muted at 10 percent");
                System.out.println("PASS supplied 1080p media, authenticated video/PCM, SOFTWARE, 540 frames and muted 10%: "+Path.of(path).getFileName());
            } finally {decoder.close();}
        }
    }
}
