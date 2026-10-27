package com.videomenu;
import java.net.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.concurrent.*;

/** Rejected activity requests must retain a useful private-safe reason and back off. */
public final class DiscordPresenceFailureRegression {
    public static void main(String[] args)throws Exception {
        Path root=Files.createTempDirectory("evatex-rpc-error-");Path socket=root.resolve("ipc");
        var rejected=new CompletableFuture<Void>();
        try(var server=ServerSocketChannel.open(StandardProtocolFamily.UNIX)) {
            server.bind(UnixDomainSocketAddress.of(socket));
            Thread peer=new Thread(()->{
                try(var client=server.accept()) {
                    DiscordPresenceRegression.read(client);
                    DiscordPresenceRegression.send(client,1,"{\"evt\":\"READY\",\"cmd\":\"DISPATCH\"}");
                    DiscordPresenceRegression.read(client);
                    DiscordPresenceRegression.send(client,1,"{\"evt\":\"ERROR\",\"data\":{\"code\":4000,\"message\":\"private-path-and-account-fixture\"}}");
                    rejected.complete(null);
                }catch(Throwable error){rejected.completeExceptionally(error);}
            });peer.setDaemon(true);peer.start();
            try(var presence=new EvatexDiscordPresence("123456789012345678",socket)) {
                rejected.get(5,TimeUnit.SECONDS);long until=System.nanoTime()+2_000_000_000L;
                while(!presence.status.equals("evatex.discord.rejected")&&System.nanoTime()<until)Thread.sleep(10);
                if(!presence.status.equals("evatex.discord.rejected"))throw new AssertionError("RPC rejection not shown");
                String report=presence.diagnostics();
                if(!report.contains("rpc-error-4000")||report.contains("private-path-and-account-fixture")||report.contains(socket.toString()))throw new AssertionError("Diagnostics must expose the code without server messages or paths");
                server.configureBlocking(false);Thread.sleep(1250);
                try(var extra=server.accept()){if(extra!=null)throw new AssertionError("Rejected request immediately reconnected instead of backing off");}
                if(!presence.status.equals("evatex.discord.rejected"))throw new AssertionError("Backoff hid the rejection reason");
            }
        }finally{Files.deleteIfExists(socket);Files.deleteIfExists(root);}
        System.out.println("PASS RPC rejection code, privacy-safe diagnostics and bounded reconnect backoff");
    }
}
