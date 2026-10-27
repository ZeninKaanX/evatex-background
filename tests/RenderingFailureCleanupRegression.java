package com.videomenu.video;

import com.videomenu.VideoMenuConfig;
import java.lang.reflect.*;
import java.nio.file.Path;
import java.util.concurrent.*;

/** A render failure must release a live decoder without waiting for a game tick. */
public final class RenderingFailureCleanupRegression {
    static Field field(Class<?> type,String name)throws Exception {
        Field f=type.getDeclaredField(name);f.setAccessible(true);return f;
    }
    public static void main(String[] args)throws Exception {
        VideoMenuConfig config=new VideoMenuConfig();config.videoPath=Path.of(args[0]).toAbsolutePath().toString();
        config.audioEnabled=false;config.hardwareAcceleration=false;config.fps=30;
        CountDownLatch frame=new CountDownLatch(1);
        VideoDecoder decoder=new VideoDecoder(config,(pixels,w,h)->frame.countDown(),()->{},true,160,90);
        Constructor<VideoBackground> constructor=VideoBackground.class.getDeclaredConstructor();constructor.setAccessible(true);
        VideoBackground background=constructor.newInstance();background.init(config);
        field(VideoBackground.class,"decoder").set(background,decoder);
        try {
            if(!decoder.start()||!frame.await(8,TimeUnit.SECONDS))throw new AssertionError("Live fixture decoder failed to start");
            Process process=(Process)field(VideoDecoder.class,"process").get(decoder);
            if(process==null||!process.isAlive())throw new AssertionError("Expected running FFmpeg before render failure");
            Method fail=VideoBackground.class.getDeclaredMethod("fail",Throwable.class);fail.setAccessible(true);
            fail.invoke(background,new IllegalStateException("Controlled render-failure regression"));
            if(field(VideoBackground.class,"decoder").get(background)!=null)throw new AssertionError("Render failure retained its decoder until a future game tick");
            if(!process.waitFor(3,TimeUnit.SECONDS))throw new AssertionError("Render failure left FFmpeg running");
            if(!"error".equals(background.status()))throw new AssertionError("Failure status was lost");
            System.out.println("PASS render failure immediately detaches and terminates the live FFmpeg decoder without a game tick");
        } finally {decoder.close();background.close();}
    }
}
