import com.videomenu.VideoMenuConfig;
import com.videomenu.video.VideoDecoder;
import java.nio.file.Path;
/** Opaque UI must pause and resume the existing shared video/PCM timeline. No host audio. */
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
    decoder.setPresentationEnabled(false);decoder.setPaused(true);Thread.sleep(250);
    long shown=decoder.publishedFrames(),decoded=decoder.decodedFrames(),pcm=decoder.audioReceivedBytes();Thread.sleep(1200);
    check(shown==decoder.publishedFrames()&&decoded==decoder.decodedFrames()&&pcm==decoder.audioReceivedBytes(),"Backgrounds must freeze both video and PCM");
    check(processField.get(decoder)==process&&audioField.get(decoder)==audio,"Menu pause restarted media");
    decoder.setPresentationEnabled(true);decoder.setPaused(false);deadline=System.nanoTime()+3_000_000_000L;
    while((decoder.publishedFrames()<shown+5||decoder.audioReceivedBytes()<=pcm)&&System.nanoTime()<deadline)Thread.sleep(20);
    check(decoder.publishedFrames()>=shown+5&&decoder.audioReceivedBytes()>pcm,"AV did not resume after leaving Backgrounds");
    check(processField.get(decoder)==process&&audioField.get(decoder)==audio,"AV resume restarted decoder/audio");
    System.out.println("PASS Backgrounds freezes picture and PCM, same decoder/audio resumes; TCP="+tcp+"; no host sound");
   }finally{decoder.close();}
  }
 }
}
