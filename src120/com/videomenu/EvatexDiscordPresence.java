package com.videomenu;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/** Local Discord IPC only. No bot token, login, HTTP request, or account automation. */
public final class EvatexDiscordPresence implements AutoCloseable {
    public volatile String status = "evatex.discord.unconfigured";
    public volatile String acceptedDetails="";
    public volatile int acceptedDisplayType=-1;
    private volatile String transport="none",lastError="none";
    private volatile boolean acknowledged;
    public String diagnostics() {
        return "Discord activity: "+status+"\nDiscord enabled: "+enabled
            +"\nDiscord publisher configured: "+configured()+"\nDiscord IPC: "+transport
            +"\nDiscord acknowledged current connection: "+acknowledged
            +"\nDiscord last error: "+lastError+"\n";
    }
    private volatile boolean enabled=true;
    private final String applicationId;
    private final DiscordActivityCoordinator coordinator=new DiscordActivityCoordinator("evatex");
    private volatile long nextConnectAt;
    private final Supplier<String> activity;
    private final Path socketOverride;
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(task->{Thread thread=new Thread(task,"EVATEX-Discord");thread.setDaemon(true);return thread;});
    private volatile Closeable connection;
    private InputStream in;
    private OutputStream out;
    private String connectedId;
    private volatile boolean ready;
    private volatile String pendingNonce="",lastActivity="";
    private volatile long handshakeAt,lastPublished;
    private final java.util.concurrent.atomic.AtomicBoolean requested=new java.util.concurrent.atomic.AtomicBoolean();
    public void setEnabled(boolean value){enabled=value;coordinator.eligible(value&&configured());refresh();}
    private boolean configured(){return applicationId.matches("[0-9]{16,22}");}
    public void refresh(){
        if(worker.isShutdown()||!requested.compareAndSet(false,true))return;
        try{worker.schedule(()->{requested.set(false);update();},150,TimeUnit.MILLISECONDS);}catch(RejectedExecutionException ignored){requested.set(false);}
    }
    private final long started = System.currentTimeMillis() / 1000;
    public EvatexDiscordPresence(String applicationId){this(applicationId,null);}
    EvatexDiscordPresence(String applicationId,Path socketOverride){
        this.applicationId=applicationId==null?"":applicationId;this.activity=()->"Using EVATEX Background";
        this.socketOverride=socketOverride;
        worker.scheduleWithFixedDelay(this::update,1,1,TimeUnit.SECONDS);
    }
    private void update() {
        try {
            boolean eligible=enabled&&configured();
            coordinator.eligible(eligible);
            if(!eligible || !coordinator.acquire()){
                relinquish();
                status=!enabled?"evatex.discord.off":!configured()?"evatex.discord.unconfigured":"evatex.discord.waiting";
                return;
            }
            if (!applicationId.equals(connectedId)) disconnect();
            if(connection!=null&&!ready&&System.nanoTime()-handshakeAt>10_000_000_000L)disconnect();
            if (connection == null){if(System.nanoTime()<nextConnectAt)return;connect();}
            if (ready && !pendingNonce.isEmpty() && System.nanoTime()-lastPublished>10_000_000_000L) {
                lastError="activity-ack-timeout";nextConnectAt=System.nanoTime()+5_000_000_000L;
                disconnect();coordinator.release();status="evatex.discord.unavailable";return;
            }
            if (ready) {
                JsonObject a = new JsonObject(); a.addProperty("details", activity.get()); a.addProperty("state", "Minecraft · Astral");
                a.addProperty("type",0);a.addProperty("status_display_type",2);
                JsonObject time = new JsonObject(); time.addProperty("start", started); a.add("timestamps", time);
                // Upload an asset called 'evatex' in Discord's Developer Portal.
                JsonObject assets = new JsonObject(); assets.addProperty("large_image", "evatex"); assets.addProperty("large_text", "EVATEX Background"); assets.addProperty("large_url", "https://modrinth.com/mod/evatex-background");a.add("assets", assets);
                a.addProperty("details_url","https://modrinth.com/mod/evatex-background");
                JsonArray buttons=new JsonArray();JsonObject button=new JsonObject();button.addProperty("label","View on Modrinth");button.addProperty("url","https://modrinth.com/mod/evatex-background");buttons.add(button);a.add("buttons",buttons);
                String snapshot=a.toString();
                if(!snapshot.equals(lastActivity)||System.nanoTime()-lastPublished>=12_000_000_000L){setActivity(a);lastActivity=snapshot;lastPublished=System.nanoTime();if(!status.equals("evatex.discord.connected"))status="evatex.discord.connecting";}
            }
        } catch (Exception e) { lastError="ipc-"+e.getClass().getSimpleName();disconnect();coordinator.release();nextConnectAt=System.nanoTime()+5_000_000_000L; status = "evatex.discord.unavailable"; }
    }
    /** Include private Flatpak runtime sockets when its exported relay is absent.
     * Permission boundaries stay intact: inaccessible paths simply fail to connect. */
    static Set<String> socketRoots(Map<String,String> environment) {
        Set<String> roots = new LinkedHashSet<>();
        for (String key : List.of("XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP")) {
            String value=environment.get(key);
            if(value!=null&&!value.isBlank())roots.add(value);
        }
        roots.add("/tmp");
        String runtime=environment.get("XDG_RUNTIME_DIR");
        if(runtime!=null&&!runtime.isBlank()) {
            for(String app:List.of("com.discordapp.Discord","com.discordapp.DiscordCanary",
                    "com.discordapp.DiscordPTB","dev.vencord.Vesktop")) {
                roots.add(runtime+"/app/"+app);
                roots.add(runtime+"/.flatpak/"+app+"/xdg-run");
            }
        }
        return roots;
    }
    private void connect() throws IOException {
        if (socketOverride != null && isWindows(System.getProperty("os.name")) && socketOverride.toString().startsWith("\\\\.\\pipe\\")) {
            useWindowsPipe(socketOverride.toString());
        } else if (socketOverride != null) {
            SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX);
            try { channel.connect(UnixDomainSocketAddress.of(socketOverride)); useChannel(channel); }
            catch (IOException e) { channel.close(); throw e; }
        } else if (isWindows(System.getProperty("os.name"))) {
            for (int i = 0; i < 10; i++) {
                try {
                    useWindowsPipe("\\\\.\\pipe\\discord-ipc-" + i);transport="Windows pipe "+i;
                    break;
                } catch (FileNotFoundException ignored) { }
            }
        } else {
            Set<String> roots = socketRoots(System.getenv());
            outer: for (String root : roots) for (int i = 0; i < 10; i++) {
                SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX);
                try {
                    channel.connect(UnixDomainSocketAddress.of(Path.of(root, "discord-ipc-" + i)));
                    useChannel(channel);
                    break outer;
                } catch (IOException e) { channel.close(); }
            }
        }
        if (connection == null) throw new IOException("Discord desktop not running");
        connectedId = applicationId;handshakeAt=System.nanoTime();status="evatex.discord.connecting";
        JsonObject hello = new JsonObject(); hello.addProperty("v", 1); hello.addProperty("client_id", connectedId); send(0, hello.toString().getBytes(StandardCharsets.UTF_8));
        InputStream input = in;
        Closeable active = connection;
        Thread reader=new Thread(() -> {
            try {
                while (connection == active) {
                    byte[] header = input.readNBytes(8);
                    if (header.length != 8) break;
                    ByteBuffer b = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
                    int opcode = b.getInt(), length = b.getInt();
                    if (length < 0 || length > 65536) break;
                    byte[] payload = input.readNBytes(length);
                    if (payload.length != length || connection != active) break;
                    if (opcode == 3) send(4, payload);
                    else if (opcode == 2) {
                        lastError=rpcError("close",payload);break;
                    }
                    else if (opcode == 1) {
                        JsonObject response = JsonParser.parseString(new String(payload, StandardCharsets.UTF_8)).getAsJsonObject();
                        if (response.has("evt") && !response.get("evt").isJsonNull() && "READY".equals(response.get("evt").getAsString())) { ready = true; worker.execute(this::update); }
                        if(response.has("evt")&&!response.get("evt").isJsonNull()&&"ERROR".equals(response.get("evt").getAsString())){
                            lastError=rpcError("error",payload);nextConnectAt=System.nanoTime()+30_000_000_000L;
                            disconnect();coordinator.release();status="evatex.discord.rejected";break;
                        }
                        else if(response.has("nonce")&&!response.get("nonce").isJsonNull()&&pendingNonce.equals(response.get("nonce").getAsString())&&response.has("cmd")&&"SET_ACTIVITY".equals(response.get("cmd").getAsString())){
                            status="evatex.discord.connected";acknowledged=true;pendingNonce="";lastError="none";
                            if(response.has("data")&&response.get("data").isJsonObject()){
                                JsonObject data=response.getAsJsonObject("data");
                                if(data.has("details")&&!data.get("details").isJsonNull())acceptedDetails=data.get("details").getAsString();
                                if(data.has("status_display_type")&&!data.get("status_display_type").isJsonNull())acceptedDisplayType=data.get("status_display_type").getAsInt();
                            }
                        }
                    }
                }
            } catch (Exception failure) {if(connection==active)lastError="read-"+failure.getClass().getSimpleName();}
            finally { if (connection == active){
                nextConnectAt=System.nanoTime()+5_000_000_000L;disconnect();coordinator.release();status="evatex.discord.unavailable";
            } }
        },"EVATEX-Discord-read");reader.setDaemon(true);reader.start();
    }
    private static String rpcError(String kind,byte[] payload) {
        try {
            JsonObject message=JsonParser.parseString(new String(payload,StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject data=message.has("data")&&message.get("data").isJsonObject()?message.getAsJsonObject("data"):message;
            if(data.has("code"))return "rpc-"+kind+"-"+data.get("code").getAsInt();
        }catch(RuntimeException ignored){}
        return "rpc-"+kind;
    }
    static boolean isWindows(String name) {
        return name != null && name.toLowerCase(Locale.ROOT).startsWith("windows");
    }
    private void useWindowsPipe(String path) throws IOException {
        WindowsDiscordPipe pipe = new WindowsDiscordPipe(path);
        in = pipe.in; out = pipe.out;transport="Windows named pipe";connection = pipe;
    }
    private void useChannel(SocketChannel channel) {
        // Direct read/write use separate channel locks; Channels streams share blockingLock.
        in = new InputStream() {
            public int read() throws IOException { byte[] b = new byte[1]; return read(b, 0, 1) < 0 ? -1 : b[0] & 255; }
            public int read(byte[] b, int off, int len) throws IOException { return channel.read(ByteBuffer.wrap(b, off, len)); }
        };
        out = new OutputStream() {
            public void write(int b) throws IOException { write(new byte[]{(byte) b}); }
            public void write(byte[] b) throws IOException { ByteBuffer buffer = ByteBuffer.wrap(b); while (buffer.hasRemaining()) channel.write(buffer); }
        };
        transport="Unix socket";connection=channel;
    }
    private void setActivity(JsonElement activity) throws IOException {
        JsonObject args = new JsonObject(); args.addProperty("pid", ProcessHandle.current().pid()); args.add("activity", activity);
        JsonObject command = new JsonObject(); command.addProperty("cmd", "SET_ACTIVITY"); command.add("args", args); pendingNonce=UUID.randomUUID().toString(); command.addProperty("nonce", pendingNonce);
        send(1, command.toString().getBytes(StandardCharsets.UTF_8));
    }
    private synchronized void send(int opcode, byte[] payload) throws IOException {
        if (out == null) throw new IOException("Disconnected");
        out.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(opcode).putInt(payload.length).array()); out.write(payload); out.flush();
    }
    private synchronized void disconnect() {
        ready = false;acknowledged=false;lastActivity="";pendingNonce=""; Closeable old = connection; connection = null;
        try { if (old != null) old.close(); } catch (IOException ignored) { }
    }
    private void relinquish(){
        if(coordinator.owns())try{if(ready)setActivity(JsonNull.INSTANCE);}catch(IOException ignored){}
        disconnect();coordinator.release();
    }
    @Override public void close() {
        worker.shutdownNow();coordinator.eligible(false);relinquish();
    }
}
