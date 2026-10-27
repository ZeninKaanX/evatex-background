package com.videomenu;

import java.io.*;
import java.util.Objects;

/** A synchronous Windows pipe must never read an empty buffer: that read can
 * hold the kernel handle while the writer needs to publish SET_ACTIVITY.
 * FileInputStream.available() uses PeekNamedPipe in the Windows JDK.
 */
final class WindowsDiscordPipe implements Closeable {
    private final RandomAccessFile pipe;
    private final FileInputStream input;
    private final Object ioLock = new Object();
    private volatile boolean closed;
    WindowsDiscordPipe(String path) throws IOException {
        pipe = new RandomAccessFile(path, "rw");
        input = new FileInputStream(pipe.getFD());
    }
    final InputStream in = new InputStream() {
        @Override public int read() throws IOException {
            byte[] one = new byte[1];
            return read(one, 0, 1) < 0 ? -1 : one[0] & 255;
        }
        @Override public int read(byte[] bytes, int off, int len) throws IOException {
            Objects.checkFromIndexSize(off, len, bytes.length);
            if (len == 0) return 0;
            while (!closed) {
                synchronized (ioLock) {
                    if (closed) break;
                    int available = input.available();
                    if (available > 0) return input.read(bytes, off, Math.min(len, available));
                }
                try { Thread.sleep(20); }
                catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new InterruptedIOException("Discord pipe read interrupted");
                }
            }
            return -1;
        }
    };
    final OutputStream out = new OutputStream() {
        @Override public void write(int value) throws IOException { write(new byte[]{(byte)value}); }
        @Override public void write(byte[] bytes) throws IOException { write(bytes, 0, bytes.length); }
        @Override public void write(byte[] bytes, int off, int len) throws IOException {
            Objects.checkFromIndexSize(off, len, bytes.length);
            synchronized (ioLock) {
                if (closed) throw new IOException("Discord pipe closed");
                pipe.write(bytes, off, len);
            }
        }
    };
    @Override public void close() throws IOException {
        synchronized (ioLock) {
            if (closed) return;
            closed = true;
            pipe.close();
        }
    }
}
