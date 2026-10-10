package com.videomenu.video;

import com.videomenu.VideoMenuClient;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/** PCM from the same FFmpeg input timeline as the video; no second video decoder. */
public final class VideoAudio implements AutoCloseable {
    private final ServerSocket server;
    private volatile Socket socket;
    private volatile SourceDataLine line;
    private volatile boolean closed, enabled;
    private volatile String status = "muted";
    private volatile long receivedBytes, writtenBytes, underruns;
    private boolean playbackStarted;
    private int primedBytes;
    private volatile boolean deviceFailed;
    private volatile double volume;
    private final Thread thread;
    public VideoAudio(boolean enabled,double volume) throws java.io.IOException {
        this.volume=volume;
        this.enabled = enabled;
        server = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
        server.setReceiveBufferSize(8192);
        server.setSoTimeout(15000);
        thread = new Thread(this::read, "EVATEX-Audio"); thread.setDaemon(true); thread.start();
    }
    public String outputUrl() { return "tcp://127.0.0.1:"+server.getLocalPort()+"?tcp_nodelay=1&send_buffer_size=8192"; }
    public String status() { return status; }
    public long receivedBytes() { return receivedBytes; }
    public long writtenBytes() { return writtenBytes; }
    public long underruns() { return underruns; }
    public void volume(double value) {volume=Double.isFinite(value) ? Math.max(0,Math.min(1,value)) : 1;}
    private static void applyVolume(byte[] pcm,int count,double gain) {
        if(gain==1)return;
        if(gain==0){java.util.Arrays.fill(pcm,0,count,(byte)0);return;}
        for(int i=0;i+1<count;i+=2) {
            int sample=(pcm[i]&255)|(pcm[i+1]<<8);
            int scaled=(int)Math.round(sample*gain);
            pcm[i]=(byte)scaled;pcm[i+1]=(byte)(scaled>>8);
        }
    }
    public void enabled(boolean value) {
        if (enabled==value)return;
        enabled = value;
        if (value) deviceFailed = false;
        if (!value) { status = "muted"; closeLine(); }
    }
    private void read() {
        try {
            socket = server.accept(); socket.setReceiveBufferSize(8192); server.close();
            byte[] pcm = new byte[4096];
            try (InputStream input = socket.getInputStream()) {
                int count;
                while (!closed && (count = input.readNBytes(pcm,0,pcm.length)) > 0) {
                    receivedBytes += count;
                    if (!enabled || count == 0) continue;
                    applyVolume(pcm,count,volume);
                    SourceDataLine sink = line;
                    if (sink == null && !deviceFailed) {
                        try {
                            AudioFormat format = new AudioFormat(48000,16,2,true,false);
                            sink = defaultOutput(format); sink.open(format,32768);
                            playbackStarted=false;primedBytes=0;
                            if (closed || !enabled) { sink.close(); continue; }
                            synchronized (this) {
                                if (closed || !enabled) { sink.close(); continue; }
                                line = sink;
                            }
                        } catch (Exception e) {
                            if (sink != null) { try { sink.close(); } catch (RuntimeException ignored) {} sink=null; }
                            deviceFailed = true; status = "unavailable";
                            VideoMenuClient.LOGGER.warn("Video audio device unavailable; video will continue", e);
                        }
                    }
                    if (sink != null && enabled && !closed) {
                        if (playbackStarted && sink.available() >= sink.getBufferSize()) underruns++;
                        int offset = 0;
                        while (offset < count && enabled && !closed && sink.isOpen()) {
                            int bytes;
                            try { bytes = sink.write(pcm, offset, count-offset); }
                            catch (RuntimeException e) {
                                if (enabled && !closed) { deviceFailed = true; status = "unavailable"; VideoMenuClient.LOGGER.warn("Video audio device stopped; video will continue", e); }
                                closeLine(); break;
                            }
                            if (bytes <= 0) break;
                            offset += bytes; writtenBytes += bytes;
                            if (!playbackStarted) {
                                primedBytes += bytes;
                                if (primedBytes >= 16384 && enabled && !closed && sink.isOpen()) { sink.start(); playbackStarted=true; }
                            }
                        }
                        if (enabled && sink.isOpen()) status = "playing";
                    }
                }
            }
        } catch (Exception e) {
            if (!closed) { status = "unavailable"; VideoMenuClient.LOGGER.warn("Video audio stream ended", e); }
        } finally { close(); }
    }
    private static SourceDataLine defaultOutput(AudioFormat format) throws javax.sound.sampled.LineUnavailableException {
        // JavaSound's automatic selection can skip ALSA's single-line default device
        // and open a physical card, bypassing PipeWire/PulseAudio and the user's output.
        if (System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("linux")) {
            for(var mixer : AudioSystem.getMixerInfo()) {
                if(mixer.getName().contains("[default]"))return AudioSystem.getSourceDataLine(format,mixer);
            }
        }
        return AudioSystem.getSourceDataLine(format);
    }
    private void closeLine() {
        SourceDataLine sink; synchronized (this) { sink = line; line = null; }
        if (sink != null) { sink.stop(); sink.flush(); sink.close(); }
    }
    @Override public void close() {
        closed = true; closeLine();
        try { server.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
        thread.interrupt();
    }
}
