import com.videomenu.VideoMenuConfig;
import com.videomenu.video.VideoDecoder;
import java.nio.file.Path;
/** Opaque UI must suspend visuals without interrupting the shared PCM clock. No host audio. */
public final class PresentationRegression {
 private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception{
  for(boolean tcp:new boolean[]{false,true}){
   var cfg=new VideoMenuConfig();cfg.videoPath=Path.of(args[0]).toAbsolutePath().toString();cfg.audioEnabled=false;cfg.audioVolume=.1;cfg.fps=30;
   var decoder=new VideoDecoder(cfg,(frame,w,h)->{},()->{},true);
   var transport=VideoDecoder.class.getDeclaredField("socketTransport");transport.setAccessible(true);transport.setBoolean(decoder,tcp);
   var processField=VideoDecoder.class.getDeclaredField("process");processField.setAccessible(true);
   var audioField=VideoDecoder.class.getDeclaredField("audio");audioField.setAccessible(true);
   try{
    check(decoder.start(),"AV fixture starts");long deadline=System.nanoTime()+10_000_000_000L;
    while((decoder.publishedFrames()<5||decoder.audioReceivedBytes()==0)&&System.nanoTime()<deadline)Thread.sleep(20);
    check(decoder.hasAudioTrack()&&decoder.publishedFrames()>=5&&decoder.audioReceivedBytes()>0,"AV fixture delivers pixels and muted PCM");
    Object process=processField.get(decoder),audio=audioField.get(decoder);
    decoder.setPresentationEnabled(false);Thread.sleep(150);
    long shown=decoder.publishedFrames(),decoded=decoder.decodedFrames(),pcm=decoder.audioReceivedBytes();Thread.sleep(1200);
    check(shown==decoder.publishedFrames(),"Opaque UI published video frames");
    check(decoder.decodedFrames()>decoded+10&&decoder.audioReceivedBytes()>pcm+48000,"PCM timeline stopped behind opaque UI");
    check(processField.get(decoder)==process&&audioField.get(decoder)==audio,"UI transition restarted decoder/audio");
    decoder.setPresentationEnabled(true);deadline=System.nanoTime()+3_000_000_000L;
    while(decoder.publishedFrames()<shown+5&&System.nanoTime()<deadline)Thread.sleep(20);
    check(decoder.publishedFrames()>=shown+5&&processField.get(decoder)==process&&audioField.get(decoder)==audio,"Visuals failed to resume on the existing audio timeline");
    decoder.setPresentationEnabled(false);decoder.setPaused(true);Thread.sleep(200);long paused=decoder.decodedFrames(),silent=decoder.audioReceivedBytes();Thread.sleep(300);
    check(paused==decoder.decodedFrames()&&silent==decoder.audioReceivedBytes(),"Unfocused opaque UI failed to pause all media");
    System.out.println("PASS opaque visuals suspended, PCM continuous, same decoder/audio on resume, focus pause; TCP="+tcp+"; no host sound");
   }finally{decoder.close();}
  }
 }
}
