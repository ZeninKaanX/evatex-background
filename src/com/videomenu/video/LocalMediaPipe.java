package com.videomenu.video;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.function.BooleanSupplier;

/** One authenticated local FFmpeg upload. Not a boundary against same-user process inspection. */
final class LocalMediaPipe implements AutoCloseable {
    private final ServerSocket server = new ServerSocket();
    private final String path;
    private volatile Socket socket;
    private volatile boolean closed;
    LocalMediaPipe(int bufferSize) throws IOException {
        byte[] token=new byte[32];new SecureRandom().nextBytes(token);
        path="/"+HexFormat.of().formatHex(token);
        server.setReceiveBufferSize(bufferSize);
        server.bind(new InetSocketAddress("127.0.0.1",0),1);
        server.setSoTimeout(500);
    }
    String outputUrl() {return "http://127.0.0.1:"+server.getLocalPort()+path;}
    InputStream accept(BooleanSupplier alive) throws IOException {
        long deadline=System.nanoTime()+12_000_000_000L;
        while(!closed && alive.getAsBoolean() && System.nanoTime()<deadline) {
            Socket candidate;
            try {candidate=server.accept();} catch(SocketTimeoutException e){continue;}
            socket=candidate;
            try {
                candidate.setSoTimeout(1000);
                InputStream in=new BufferedInputStream(candidate.getInputStream(),262144);
                String request=line(in,4096);int total=request.length();
                boolean chunked=false;
                String header;
                while(!(header=line(in,4096)).isEmpty()) {
                    total+=header.length();if(total>8192)throw new IOException("Oversized media handshake");
                    if(header.equalsIgnoreCase("Transfer-Encoding: chunked"))chunked=true;
                }
                if(!request.equals("POST "+path+" HTTP/1.1") || !chunked)throw new IOException("Unauthorized media connection");
                if(closed)throw new IOException("Media connection closed");
                candidate.getOutputStream().write("HTTP/1.1 200 OK\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
                candidate.setSoTimeout(0);server.close();
                return new Chunked(in);
            } catch(IOException e) {
                candidate.close();socket=null;
                if(closed)throw e;
            }
        }
        throw new IOException("Decoder stopped before authenticated media connection");
    }
    private static String line(InputStream in,int limit) throws IOException {
        StringBuilder b=new StringBuilder();
        for(int i=0;i<limit;i++) {
            int c=in.read();if(c<0)throw new EOFException("Truncated media handshake");
            if(c=='\n') {if(b.length()==0 || b.charAt(b.length()-1)!='\r')throw new IOException("Invalid media framing");return b.substring(0,b.length()-1);}
            b.append((char)c);
        }
        throw new IOException("Oversized media line");
    }
    private static final class Chunked extends InputStream {
        private final InputStream in;private long remaining;private boolean ended,delimiter;
        Chunked(InputStream in){this.in=in;}
        @Override public int read() throws IOException {byte[] b=new byte[1];return read(b,0,1)<0 ? -1 : b[0]&255;}
        @Override public int read(byte[] b,int off,int len) throws IOException {
            java.util.Objects.checkFromIndexSize(off,len,b.length);if(len==0)return 0;if(ended)return -1;
            if(remaining==0) {
                if(delimiter && !line(in,2).isEmpty())throw new IOException("Invalid media chunk delimiter");
                String size=line(in,128);int extension=size.indexOf(';');if(extension>=0)size=size.substring(0,extension);
                if(!size.matches("[0-9a-fA-F]{1,8}"))throw new IOException("Invalid media chunk length");
                remaining=Long.parseLong(size,16);
                if(remaining==0){ended=true;return -1;}delimiter=true;
            }
            int count=in.read(b,off,(int)Math.min(len,remaining));if(count<0)throw new EOFException("Truncated media chunk");remaining-=count;return count;
        }
        @Override public void close() throws IOException {in.close();}
    }
    @Override public void close() {
        closed=true;try{server.close();}catch(IOException ignored){}
        Socket s=socket;if(s!=null)try{s.close();}catch(IOException ignored){}
    }
}
