package com.videomenu;

import com.google.gson.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.*;

/** Actual local transport, two ownership handoffs and the public link payload. */
public final class DiscordPresenceRegression {
    record Packet(int op,String body){}
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    static void fill(SocketChannel c,ByteBuffer b)throws Exception{while(b.hasRemaining())if(c.read(b)<0)throw new java.io.EOFException();}
    static Packet read(SocketChannel c)throws Exception{
        ByteBuffer h=ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);fill(c,h);h.flip();int op=h.getInt(),n=h.getInt();check(n>=0&&n<=65536,"Invalid packet size");
        ByteBuffer b=ByteBuffer.allocate(n);fill(c,b);return new Packet(op,new String(b.array(),StandardCharsets.UTF_8));
    }
    static void send(SocketChannel c,int op,String value)throws Exception{
        byte[] bytes=value.getBytes(StandardCharsets.UTF_8);ByteBuffer b=ByteBuffer.allocate(8+bytes.length).order(ByteOrder.LITTLE_ENDIAN).putInt(op).putInt(bytes.length).put(bytes);b.flip();while(b.hasRemaining())c.write(b);
    }
    public static void main(String[] args)throws Exception{
        check(EvatexDiscordPresence.isWindows("Windows 11")&&!EvatexDiscordPresence.isWindows("Darwin"),"Windows/macOS transport selection");
        var roots=EvatexDiscordPresence.socketRoots(java.util.Map.of("XDG_RUNTIME_DIR","/run/user/1000","TMPDIR","","TMP","/tmp"));
        check(roots.contains("/run/user/1000/.flatpak/com.discordapp.Discord/xdg-run"),"Private Flatpak socket discovery");
        check(roots.contains("/run/user/1000/app/com.discordapp.Discord"),"Exported Flatpak socket discovery");
        check(roots.contains("/run/user/1000/.flatpak/dev.vencord.Vesktop/xdg-run"),"Vesktop socket discovery");
        check(!roots.contains(""),"Empty environment path must not search working directory");
        check(EvatexDiscordPresence.socketRoots(java.util.Map.of()).equals(java.util.Set.of("/tmp")),"No runtime fallback");
        boolean discover=java.util.Arrays.asList(args).contains("--flatpak-discovery");
        Path folder=discover?Path.of(System.getenv("XDG_RUNTIME_DIR"),".flatpak/com.discordapp.Discord/xdg-run"):Files.createTempDirectory("evatex-rpc-");
        Files.createDirectories(folder);
        Path socket=folder.resolve(discover?"discord-ipc-0":"ipc");String id="123456789012345678";
        var first=new CompletableFuture<Void>();var yielded=new CompletableFuture<Void>();var resumed=new CompletableFuture<Void>();var off=new CompletableFuture<Void>();
        try(ServerSocketChannel server=ServerSocketChannel.open(StandardProtocolFamily.UNIX)){
            server.bind(UnixDomainSocketAddress.of(socket));
            Thread peer=new Thread(()->{
                try{
                    for(int round=0;round<2;round++)try(SocketChannel c=server.accept()){
                        Packet hello=read(c);check(hello.op()==0,"Handshake opcode");check(id.equals(JsonParser.parseString(hello.body()).getAsJsonObject().get("client_id").getAsString()),"Publisher ID");
                        send(c,1,"{\"evt\":\"READY\",\"cmd\":\"DISPATCH\"}");Packet packet=read(c);JsonObject message=JsonParser.parseString(packet.body()).getAsJsonObject();
                        JsonObject activity=message.getAsJsonObject("args").getAsJsonObject("activity");
                        check("Using EVATEX Background".equals(activity.get("details").getAsString()),"Brand activity");
                        String url="https://modrinth.com/mod/evatex-background";
                        check(url.equals(activity.get("details_url").getAsString()),"Clickable details URL");
                        check(url.equals(activity.getAsJsonObject("assets").get("large_url").getAsString()),"Clickable logo URL");
                        check(url.equals(activity.getAsJsonArray("buttons").get(0).getAsJsonObject().get("url").getAsString()),"Modrinth button URL");
                        JsonObject ack=new JsonObject();ack.add("nonce",message.get("nonce"));ack.addProperty("cmd","SET_ACTIVITY");ack.add("evt",JsonNull.INSTANCE);ack.add("data",activity);send(c,1,ack.toString());
                        send(c,3,"ping");Packet pong=read(c);check(pong.op()==4&&pong.body().equals("ping"),"Ping/pong");
                        (round==0?first:resumed).complete(null);
                        Packet cleared=read(c);check(JsonParser.parseString(cleared.body()).getAsJsonObject().getAsJsonObject("args").get("activity").isJsonNull(),"Old activity must clear before handoff");
                        (round==0?yielded:off).complete(null);
                    }
                }catch(Throwable e){for(var f:java.util.List.of(first,yielded,resumed,off))f.completeExceptionally(e);}
            },"EVATEX-test-peer");peer.setDaemon(true);peer.start();
            try(EvatexDiscordPresence presence=new EvatexDiscordPresence(id,discover?null:socket)){
                first.get(8,TimeUnit.SECONDS);
                var volt=new DiscordActivityCoordinator("volt");volt.eligible(true);
                yielded.get(8,TimeUnit.SECONDS);
                long deadline=System.nanoTime()+2_000_000_000L;
                while(!volt.acquire()&&System.nanoTime()<deadline)Thread.sleep(10);
                check(volt.owns(),"Volt must acquire after EVATEX clears");
                volt.eligible(false);volt.release();
                resumed.get(8,TimeUnit.SECONDS);
                presence.setEnabled(false);off.get(8,TimeUnit.SECONDS);
            }
        }finally{Files.deleteIfExists(socket);Files.deleteIfExists(folder);}
        System.out.println("PASS EVATEX IPC handshake, acknowledgement, ping/pong, clickable Modrinth details/logo/button, Volt handoff, menu restoration and disable cleanup");
    }
}
