package com.videomenu.video;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
public final class LocalMediaPipeRegression {
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    static void request(Socket socket,String path,String body)throws Exception {
        socket.getOutputStream().write(("POST "+path+" HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\n"+body).getBytes(StandardCharsets.US_ASCII));
    }
    static InputStream framed(String body)throws Exception {
        var type=Class.forName("com.videomenu.video.LocalMediaPipe$Chunked");
        var constructor=type.getDeclaredConstructor(InputStream.class);constructor.setAccessible(true);
        return (InputStream)constructor.newInstance(new ByteArrayInputStream(body.getBytes(StandardCharsets.US_ASCII)));
    }
    public static void main(String[] args)throws Exception {
        try(LocalMediaPipe pipe=new LocalMediaPipe(8192);LocalMediaPipe other=new LocalMediaPipe(8192)) {
            URI uri=URI.create(pipe.outputUrl());check(uri.getHost().equals("127.0.0.1"),"IPv4 loopback only");
            check(!uri.getPath().equals(URI.create(other.outputUrl()).getPath()),"per-session secret differs");
            ExecutorService worker=Executors.newSingleThreadExecutor();
            try {
                Future<InputStream> accepted=worker.submit(()->pipe.accept(()->true));
                try(Socket bad=new Socket(uri.getHost(),uri.getPort())) {
                    bad.setSoTimeout(2000);request(bad,"/wrong","4\r\nfake\r\n0\r\n\r\n");
                    check(bad.getInputStream().read()==-1,"unauthorized connection rejected");
                }
                try(Socket partial=new Socket(uri.getHost(),uri.getPort())) {
                    partial.setSoTimeout(2500);partial.getOutputStream().write("POST /".getBytes(StandardCharsets.US_ASCII));
                    check(partial.getInputStream().read()==-1,"incomplete handshake expires");
                }
                try(Socket valid=new Socket(uri.getHost(),uri.getPort())) {
                    request(valid,uri.getPath(),"3\r\nabc\r\n2\r\nde\r\n0\r\n\r\n");
                    InputStream input=accepted.get(3,TimeUnit.SECONDS);
                    check(new String(input.readAllBytes(),StandardCharsets.US_ASCII).equals("abcde"),"chunk framing stripped without altering payload");
                    try(Socket extra=new Socket(uri.getHost(),uri.getPort())){throw new AssertionError("listener remained open");}
                    catch(ConnectException expected){}
                }
            } finally {worker.shutdownNow();}
        }
        LocalMediaPipe interrupted=new LocalMediaPipe(8192);URI uri=URI.create(interrupted.outputUrl());
        ExecutorService worker=Executors.newSingleThreadExecutor();
        try {
            Future<?> accepted=worker.submit(()->{try{interrupted.accept(()->true);throw new AssertionError("accepted after close");}catch(IOException expected){}});
            try(Socket pending=new Socket(uri.getHost(),uri.getPort())){Thread.sleep(100);interrupted.close();accepted.get(2,TimeUnit.SECONDS);}
        }finally{interrupted.close();worker.shutdownNow();}
        try(InputStream chunks=framed("00000003;source=ffmpeg\r\nabc\r\n2\r\nde\r\n0\r\n\r\n")) {
            check(chunks.read()=='a',"single-byte read preserves its first byte");
            check(new String(chunks.readAllBytes(),StandardCharsets.US_ASCII).equals("bcde"),"zero-padded size and extension preserve data");
            check(chunks.read(new byte[1],0,0)==0,"empty read remains zero at EOF");
        }
        for(String invalid:new String[]{"\r\n","-1\r\n","100000000\r\n","g\r\n","1\na","1\r\n","1\r\naX\r\n0\r\n","1;"+"x".repeat(128)+"\r\na"}) {
            try(InputStream chunks=framed(invalid)) {
                chunks.readAllBytes();throw new AssertionError("Malformed framing accepted");
            }catch(IOException expected){}
        }
        System.out.println("PASS authenticated loopback, wrong/partial clients, exact chunk payload, one connection and shutdown");
    }
}
