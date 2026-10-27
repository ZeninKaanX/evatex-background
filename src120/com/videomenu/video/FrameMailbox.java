package com.videomenu.video;

import java.util.ArrayDeque;

/** Bounded latest-frame handoff. Full pixel copies never hold the publication lock. */
final class FrameMailbox {
    static final class Frame {
        final int[] pixels;
        final int width, height;
        private boolean leased;
        Frame(int width, int height) {this.width=width;this.height=height;pixels=new int[Math.multiplyExact(width,height)];}
    }
    private final ArrayDeque<Frame> free = new ArrayDeque<>();
    private Frame pending;
    private boolean initialized;
    private long skipped;
    boolean offer(int[] pixels, int width, int height) {
        final Frame next;
        synchronized(this) {
            if(!initialized) {
                for(int i=0;i<3;i++)free.add(new Frame(width,height));
                initialized=true;
            }
            next=free.pollFirst();
            if(next==null){skipped++;return false;}
        }
        if(next.width!=width || next.height!=height || next.pixels.length!=pixels.length) {
            synchronized(this){free.addLast(next);}
            throw new IllegalArgumentException("Video frame dimensions changed without restarting decoder");
        }
        System.arraycopy(pixels,0,next.pixels,0,pixels.length);
        synchronized(this) {
            if(pending!=null){free.addLast(pending);skipped++;}
            pending=next;
        }
        return true;
    }
    synchronized Frame acquire() {
        Frame result=pending;pending=null;
        if(result!=null)result.leased=true;
        return result;
    }
    synchronized void release(Frame frame) {
        if(!frame.leased)throw new IllegalStateException("Video frame released twice");
        frame.leased=false;free.addLast(frame);
    }
    synchronized boolean hasPending(){return pending!=null;}
    synchronized long skippedFrames(){return skipped;}
}
