package com.videomenu;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/** The peer is a real Win32 named pipe, never a user's Discord socket. */
public final class WindowsDiscordPresenceRegression {
    static void await(BooleanSupplier condition, String message) throws Exception {
        long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(12);
        while (!condition.getAsBoolean() && System.nanoTime() < until) Thread.sleep(10);
        if (!condition.getAsBoolean()) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        if (!EvatexDiscordPresence.isWindows("Windows 11") || EvatexDiscordPresence.isWindows("Darwin"))
            throw new AssertionError("Platform detection");
        try (var presence = new EvatexDiscordPresence("123456789012345678", Path.of(args[0]))) {
            await(() -> "round-1".equals(presence.acceptedDetails), "Windows READY must publish and receive activity acknowledgement");
            presence.setEnabled(false);
            await(() -> "evatex.discord.off".equals(presence.status), "Idle read must not block disabling/clearing activity");
            presence.setEnabled(true);
            await(() -> "round-2".equals(presence.acceptedDetails), "Windows pipe must reconnect after disabling");
            presence.setEnabled(false);
            await(() -> "evatex.discord.off".equals(presence.status), "Second clear must complete");
        }
        System.out.println("PASS native Windows pipe: fragmented READY, idle reader/concurrent publication, acknowledgement, clear and reconnect");
    }
}
