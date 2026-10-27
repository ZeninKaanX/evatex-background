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
        private boolean readable() throws IOException {
            if(ended)return false;
            if(remaining!=0)return true;
            if(delimiter && (in.read()!='\r' || in.read()!='\n'))throw new IOException("Invalid media chunk delimiter");
            remaining=chunkLength(in);
            if(remaining==0){ended=true;return false;}
            delimiter=true;return true;
        }
        @Override public int read() throws IOException {
            if(!readable())return -1;
            int value=in.read();if(value<0)throw new EOFException("Truncated media chunk");
            remaining--;return value;
        }
        @Override public int read(byte[] b,int off,int len) throws IOException {
            java.util.Objects.checkFromIndexSize(off,len,b.length);if(len==0)return 0;
            if(!readable())return -1;
            int count=in.read(b,off,(int)Math.min(len,remaining));if(count<0)throw new EOFException("Truncated media chunk");remaining-=count;return count;
        }
        @Override public void close() throws IOException {in.close();}
    }
    /** Parse bounded ASCII framing without allocating a String/regex per chunk. */
    private static long chunkLength(InputStream in) throws IOException {
        long size=0;int digits=0;boolean extension=false;
        for(int i=0;i<128;i++) {
            int c=in.read();if(c<0)throw new EOFException("Truncated media chunk header");
            if(c=='\r') {
                if(i+1>=128 || in.read()!='\n')throw new IOException("Invalid media framing");
                if(digits==0)throw new IOException("Invalid media chunk length");
                return size;
            }
            if(c=='\n')throw new IOException("Invalid media framing");
            if(extension)continue;
            if(c==';') {
                if(digits==0)throw new IOException("Invalid media chunk length");
                extension=true;continue;
            }
            int digit=c>='0'&&c<='9' ? c-'0' : c>='a'&&c<='f' ? c-'a'+10 : c>='A'&&c<='F' ? c-'A'+10 : -1;
            if(digit<0 || ++digits>8)throw new IOException("Invalid media chunk length");
            size=(size<<4)|digit;
        }
        throw new IOException("Oversized media chunk header");
    }
    @Override public void close() {
        closed=true;try{server.close();}catch(IOException ignored){}
        Socket s=socket;if(s!=null)try{s.close();}catch(IOException ignored){}
    }
}
