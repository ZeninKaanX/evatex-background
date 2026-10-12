package com.videomenu;

import com.videomenu.video.VideoBackground;
import net.fabricmc.loader.api.FabricLoader;

/** User-initiated, local-only report. Excludes media paths, usernames and handshake secrets. */
final class PlaybackDiagnostics {
    private PlaybackDiagnostics() {}
    static String report(VideoMenuConfig config) {
        FabricLoader loader=FabricLoader.getInstance();
        String gpu="unavailable";
        try {String value=org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER);if(value!=null)gpu=value;}
        catch(RuntimeException | LinkageError ignored) {}
        return "EVATEX playback diagnostics\n"
            +"EVATEX: "+loader.getModContainer("evatexbackgroundvideo").orElseThrow().getMetadata().getVersion()+"\n"
            +"Minecraft: "+loader.getModContainer("minecraft").orElseThrow().getMetadata().getVersion()+"\n"
            +"Fabric Loader: "+loader.getModContainer("fabricloader").orElseThrow().getMetadata().getVersion()+"\n"
            +"OS: "+System.getProperty("os.name")+" "+System.getProperty("os.version")+" "+System.getProperty("os.arch")+"\n"
            +"Java: "+System.getProperty("java.version")+"\nGPU renderer: "+gpu+"\n"
            +VideoBackground.INSTANCE.diagnosticPlayback()
            +"Configured FPS: "+config.fps+"\nDecode height limit: "+config.maxDecodeHeight
            +"\nResolution scale: "+config.scale+"\nHardware acceleration requested: "+config.hardwareAcceleration
            +"\nInstalled mod count: "+loader.getAllMods().size()+"\n"
            +"Privacy: excludes background paths, account names and connection secrets.\n";
    }
}
