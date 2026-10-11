package com.videomenu.video;

import java.lang.reflect.Proxy;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sound.sampled.SourceDataLine;

/** Exercises real PCM transport without emitting sound or depending on a host device. */
public final class AudioRegression {
    private static void check(boolean value, String text) { if (!value) throw new AssertionError(text); }
    private static SourceDataLine line(int capacity, AtomicInteger starts, AtomicInteger samples) {
        boolean[] open = {false}, running = {false}; int[] queued = {0};
        return (SourceDataLine) Proxy.newProxyInstance(AudioRegression.class.getClassLoader(), new Class<?>[]{SourceDataLine.class}, (p,m,a) -> {
            switch (m.getName()) {
                case "open": open[0]=true; return null;
                case "isOpen": return open[0];
                case "isRunning": case "isActive": return running[0];
                case "getBufferSize": return capacity;
                case "available": return Math.max(0,capacity-queued[0]);
                case "start": running[0]=true; starts.incrementAndGet(); queued[0]=0; return null;
                case "stop": running[0]=false; return null;
                case "flush": queued[0]=0; return null;
                case "close": open[0]=false; return null;
                case "write":
                    int n=(int)a[2];
                    if (!running[0]) {
                        queued[0]+=n;
                        check(queued[0]<=capacity,"stopped line overfilled before start: "+capacity);
                    }
                    byte[] pcm=(byte[])a[0];int offset=(int)a[1];
                    samples.set((short)((pcm[offset]&255)|(pcm[offset+1]<<8)));
                    return n;
                default:
                    if (m.getReturnType()==boolean.class) return false;
                    if (m.getReturnType()==int.class) return 0;
                    if (m.getReturnType()==long.class) return 0L;
                    if (m.getReturnType()==float.class) return 0f;
                    return null;
            }
        });
    }
    private static byte[] pcm() {
        byte[] pcm=new byte[4096];
        for (int i=0;i<pcm.length;i+=2) { pcm[i]=0x10;pcm[i+1]=0x27; }
        return pcm;
    }
    private static Socket connect(VideoAudio audio) throws Exception {
        URI uri=URI.create(audio.outputUrl());return new Socket(uri.getHost(),uri.getPort());
    }
    private static void await(java.util.function.BooleanSupplier ready,String text) throws Exception {
        long until=System.nanoTime()+4_000_000_000L;
        while(!ready.getAsBoolean() && System.nanoTime()<until) Thread.sleep(10);
        check(ready.getAsBoolean(),text);
    }
    public static void main(String[] args) throws Exception {
        for (int capacity:new int[]{4,2048,8192,32768}) {
            AtomicInteger starts=new AtomicInteger(),samples=new AtomicInteger();
            try (VideoAudio audio=new VideoAudio(true,.1,f->line(capacity,starts,samples));Socket socket=connect(audio)) {
                for(int i=0;i<12;i++)socket.getOutputStream().write(pcm());
                await(()->audio.writtenBytes()==12*4096,"PCM progresses with buffer "+capacity);
                check(starts.get()==1,"output primed and started once");check(samples.get()==1000,"10% PCM gain");
                audio.enabled(false);Thread.sleep(20);long written=audio.writtenBytes();
                socket.getOutputStream().write(pcm());Thread.sleep(30);check(audio.writtenBytes()==written,"mute stops writes");
                audio.enabled(true);socket.getOutputStream().write(pcm());
                await(()->audio.writtenBytes()>written,"unmute reopens line");
                socket.shutdownOutput();await(audio::streamEnded,"natural PCM EOF reported");
            }
            System.out.println("PASS bounded priming, gain, mute/unmute and stream EOF; buffer="+capacity);
        }
        AtomicInteger attempts=new AtomicInteger(),starts=new AtomicInteger(),samples=new AtomicInteger();
        try (VideoAudio audio=new VideoAudio(true,.1,f->{
            if(attempts.incrementAndGet()==1)throw new javax.sound.sampled.LineUnavailableException("temporary device loss");
            return line(8192,starts,samples);
        });Socket socket=connect(audio)) {
            for(int i=0;i<26;i++) {socket.getOutputStream().write(pcm());Thread.sleep(100);}
            await(()->audio.writtenBytes()>0,"temporary audio device failure recovers without toggle");
            check(attempts.get()==2 && starts.get()==1,"bounded device retry");
        }
        System.out.println("PASS temporary device recovery without restarting video; no host sound emitted");
    }
}
