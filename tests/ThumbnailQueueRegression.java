import com.videomenu.library.LibraryThumbnails;
import java.util.*;
import java.util.concurrent.CompletableFuture;
/** Rapid page changes must free both completed and still-pending off-screen requests. */
public final class ThumbnailQueueRegression {
 public static void main(String[] args)throws Exception{
  var previews=new LibraryThumbnails();
  var field=LibraryThumbnails.class.getDeclaredField("pending");field.setAccessible(true);
  @SuppressWarnings("unchecked") var pending=(Map<String,CompletableFuture<byte[]>>)field.get(previews);
  CompletableFuture<byte[]> visible=null;
  for(int page=0;page<1000;page++){
   var unfinished=new ArrayList<CompletableFuture<byte[]>>();
   if(visible!=null)unfinished.add(visible);
   for(int n=0;n<20;n++){
    var request=n%2==0?CompletableFuture.completedFuture(new byte[10]):new CompletableFuture<byte[]>();
    if(!request.isDone())unfinished.add(request);pending.put("old-"+page+"-"+n,request);
   }
   visible=new CompletableFuture<>();String id="visible-"+page;pending.put(id,visible);
   previews.retainVisible(Set.of(id));
   if(pending.size()!=1||pending.get(id)!=visible||visible.isCancelled())throw new AssertionError("Visible queue stalled or current request cancelled");
   for(var request:unfinished)if(!request.isCancelled())throw new AssertionError("Off-screen pending request survived page change");
  }
  previews.close();if(!visible.isCancelled())throw new AssertionError("Closing screen left a request running");
  System.out.println("PASS 1000 rapid page changes: completed off-screen work removed, pending requests cancelled, current page preserved, close cleanup");
 }
}
