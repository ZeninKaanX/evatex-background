import com.videomenu.MenuPlaybackPolicy;
import com.videomenu.VideoMenuConfig;
public final class MenuPlaybackRegression {
 private static void check(boolean v,String message){if(!v)throw new AssertionError(message);}
 public static void main(String[] args){
  var cfg=new VideoMenuConfig();
  check(!cfg.inGameMenuBackground,"world menu video is opt in");
  cfg.inGameMenuBackground=true;check(cfg.copy().inGameMenuBackground,"copy preserves opt in");
  for(boolean enabled:new boolean[]{false,true}) {
   check(!MenuPlaybackPolicy.allows(true,enabled,false,false,false),"gameplay/chat/inventory cannot start decoding");
   check(!MenuPlaybackPolicy.allows(true,enabled,true,false,false),"foreign title while world exists cannot start decoding");
   check(MenuPlaybackPolicy.allows(false,enabled,true,false,false),"title background unaffected by in-game option");
   check(MenuPlaybackPolicy.allows(false,enabled,false,true,false),"existing options background unaffected");
  }
  check(!MenuPlaybackPolicy.allows(true,false,false,true,false),"in-game options off");
  check(MenuPlaybackPolicy.allows(true,true,false,true,false),"in-game options on");
  check(!MenuPlaybackPolicy.allows(true,false,false,false,true),"preview off in world");
  check(MenuPlaybackPolicy.allows(true,true,false,false,true),"preview enabled in world");
  for(int i=0;i<1000;i++){
   check(MenuPlaybackPolicy.allows(true,true,false,true,false),"options entering");
   check(!MenuPlaybackPolicy.allows(true,true,false,false,false),"return to gameplay stops decoding");
  }
  System.out.println("PASS optional in-game menus, defaults, copied config, gameplay exclusion and 1000 menu exits");
 }
}
