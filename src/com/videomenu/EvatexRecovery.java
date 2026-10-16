package com.videomenu;

import com.videomenu.video.VideoBackground;
import net.minecraft.class_310;
import net.minecraft.class_442;

/** Handles only EVATEX-owned failures; never intercepts Minecraft's crash handler. */
public final class EvatexRecovery {
    private static final RecoveryCircuit CIRCUIT = new RecoveryCircuit();
    private static volatile boolean pending;
    private static String phase = "interface";
    private EvatexRecovery() {}
    public static boolean suspended() { return CIRCUIT.suspended(); }
    public static boolean run(String operation, Runnable action) {
        if (suspended()) return false;
        try { action.run(); return true; }
        catch (RuntimeException | LinkageError failure) { report(operation, failure); return false; }
    }
    public static void report(String operation, Throwable failure) {
        if (CIRCUIT.trip(failure)) {
            phase=operation;
            VideoMenuClient.LOGGER.error("EVATEX paused for this session after an unexpected {} error", operation, failure);
            pending=true;
        }
    }
    public static void tick(class_310 client) {
        if (!pending) return;
        pending=false;
        try { VideoBackground.INSTANCE.close(); }
        catch (RuntimeException | LinkageError cleanup) { VideoMenuClient.LOGGER.warn("EVATEX recovery cleanup",cleanup); }
        try { client.method_1507(new EvatexRecoveryScreen()); }
        catch (RuntimeException | LinkageError screenFailure) {
            VideoMenuClient.LOGGER.warn("EVATEX recovery screen unavailable; returning to Minecraft",screenFailure);
            resume(client);
        }
    }
    public static void resume(class_310 client) {
        client.method_1507(client.field_1687 == null ? new class_442() : null);
    }
    public static String diagnostics() {
        // Keep paths and exception messages out of the copied report; the log retains the full cause.
        Throwable failure=CIRCUIT.failure();
        return "EVATEX recovery diagnostics\nEVATEX: "+EvatexPlatform.modVersion("evatexbackgroundvideo")
            +"\nMinecraft: "+EvatexPlatform.modVersion("minecraft")
            +"\nLoader: "+EvatexPlatform.loaderName()+" "+EvatexPlatform.loaderVersion()
            +"\nOS: "+System.getProperty("os.name")+" "+System.getProperty("os.arch")
            +"\nJava: "+System.getProperty("java.version")+"\nOperation: "+phase
            +"\nCause: "+(failure==null?"none":failure.getClass().getName())
            +"\nEVATEX disabled for this session; media and saved preferences unchanged.\nSee latest.log for the full cause.\n";
    }
}
