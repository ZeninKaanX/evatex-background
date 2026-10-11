import com.videomenu.VideoMenuConfig;
import com.videomenu.video.VideoDecoder;
import java.lang.reflect.Field;
import java.nio.file.Path;
public class RealVideoRegression {
    static void check(boolean ok,String text) { if(!ok)throw new AssertionError(text); }
    public static void main(String[] args)throws Exception {
        VideoMenuConfig cfg=new VideoMenuConfig();cfg.videoPath=Path.of(args[0]).toAbsolutePath().toString();cfg.scale=.25;cfg.fps=30;cfg.audioVolume=.1;
        VideoDecoder decoder=new VideoDecoder(cfg,(frame,w,h)->check(w==480&&h==270,"1080p source scaled for lightweight audio test"));
        try {
            check(decoder.start(),"provided video starts");
            long warmupAt=System.nanoTime(),warmupDeadline=warmupAt+20_000_000_000L;
            while(decoder.publishedFrames()<60 && System.nanoTime()<warmupDeadline)Thread.sleep(100);
            System.out.println("Warmup seconds: "+(System.nanoTime()-warmupAt)/1e9+"; frames="+decoder.publishedFrames());
            check(decoder.publishedFrames()>50,"video advances while muted: "+decoder.lastError());
            check(decoder.audioReceivedBytes()>100000,"PCM received while muted");
            check(decoder.audioWrittenBytes()==0,"mute does not play sound");
            Field field=VideoDecoder.class.getDeclaredField("process");field.setAccessible(true);Process process=(Process)field.get(decoder);
            long frames=decoder.publishedFrames();decoder.setAudioEnabled(true);Thread.sleep(args.length>1 ? 55000 : 3000);
            System.out.println("Audio underruns during continuous playback: "+decoder.audioUnderruns());
            check(decoder.audioUnderruns()==0,"continuous sound has no buffer underruns");
            check(decoder.audioWrittenBytes()>100000,"audio reaches output device: "+decoder.audioStatus());
            if(args.length>1)check(decoder.audioWrittenBytes()>9_000_000,"PCM continues across multiple full clip loops");
            System.out.println("PCM written after enabled loop test: "+decoder.audioWrittenBytes());
            check(decoder.publishedFrames()>frames+50,"video advances with sound");
            check(field.get(decoder)==process,"audio enabled without video restart");
            decoder.setAudioVolume(.05);Thread.sleep(500);check(field.get(decoder)==process,"volume change keeps decoder");
            long beforeZero=decoder.publishedFrames();decoder.setAudioVolume(0);Thread.sleep(500);check(decoder.publishedFrames()>beforeZero,"zero volume keeps video");
            decoder.setAudioVolume(.1);Thread.sleep(500);check(field.get(decoder)==process,"restoring volume keeps decoder");
            decoder.setAudioEnabled(false);Thread.sleep(300);long written=decoder.audioWrittenBytes();Thread.sleep(1500);
            check(decoder.audioWrittenBytes()==written,"muting stops output");
            check(process.isAlive(),"video survives mute");
            Thread.sleep(10000);check(decoder.publishedFrames()>450,"provided 16.6 second clip loops");
            check(field.get(decoder)==process,"loop keeps same decoder process");
            System.out.println("PASS provided 1080p video, PCM sound, live unmute/mute/volume, loop, no decoder restart");
            // Inject the same end-of-stream signal raised by an unexpectedly closed PCM socket.
            Field audioField=VideoDecoder.class.getDeclaredField("audio");audioField.setAccessible(true);
            Object sink=audioField.get(decoder);Field ended=sink.getClass().getDeclaredField("streamEnded");ended.setAccessible(true);ended.setBoolean(sink,true);
            long beforeRecovery=decoder.publishedFrames(),deadline=System.nanoTime()+10_000_000_000L;
            while(System.nanoTime()<deadline && (field.get(decoder)==process || decoder.publishedFrames()<beforeRecovery+30))Thread.sleep(100);
            check(field.get(decoder)!=null && field.get(decoder)!=process && decoder.publishedFrames()>=beforeRecovery+30,"premature PCM EOF restarts the shared timeline");
            check(decoder.audioReceivedBytes()>100000,"PCM transport recovers with video");
            check(decoder.audioWrittenBytes()==0,"recovery preserves muted state");
            System.out.println("PASS premature PCM EOF recovery with mute/volume state preserved");
        } finally { decoder.close(); }
        Thread.sleep(500);long count=decoder.publishedFrames();Thread.sleep(300);check(count==decoder.publishedFrames(),"shutdown stops frames");
        System.out.println("PASS audio/video shutdown");
    }
}
