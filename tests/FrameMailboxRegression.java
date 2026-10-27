package com.videomenu.video;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
public final class FrameMailboxRegression {
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception {
  var box=new FrameMailbox();int[] data=new int[640*360];Arrays.fill(data,1);box.offer(data,640,360);
  var held=box.acquire();check(held!=null,"first frame");
  var error=new AtomicReference<Throwable>();
  Thread producer=new Thread(()->{try{for(int i=2;i<500;i++){Arrays.fill(data,i);box.offer(data,640,360);}}catch(Throwable t){error.set(t);}});
  producer.start();
  while(producer.isAlive()){for(int value:held.pixels)check(value==1,"renderer-owned pixels changed");Thread.yield();}
  producer.join();if(error.get()!=null)throw new AssertionError(error.get());
  var newest=box.acquire();check(newest.pixels[0]==499,"backlog keeps newest frame");
  for(int value:newest.pixels)check(value==499,"partial/torn frame");
  check(box.skippedFrames()>0,"superseded frames accounted");
  box.release(held);box.release(newest);
  check(box.acquire()==null,"no duplicate upload without new data");
  Arrays.fill(data,500);check(box.offer(data,640,360),"pool reusable");var next=box.acquire();check(next.pixels[0]==500,"recycled slot copied safely");box.release(next);
  System.out.println("PASS bounded latest-frame handoff, concurrent immutable lease, newest frame, reusable slots and no duplicate upload");
 }
}
