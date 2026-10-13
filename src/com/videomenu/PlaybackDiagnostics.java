package com.videomenu;

import com.videomenu.video.VideoBackground;
import com.videomenu.EvatexPlatform;

/** User-initiated, local-only report. Excludes media paths, usernames and handshake secrets. */
final class PlaybackDiagnostics {
    private PlaybackDiagnostics() {}
    static String report(VideoMenuConfig config) {
        String gpu="unavailable";
        try {String value=org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER);if(value!=null)gpu=value;}
        catch(RuntimeException | LinkageError ignored) {}
        return "EVATEX playback diagnostics\n"
            +"EVATEX: "+EvatexPlatform.modVersion("evatexbackgroundvideo")+"\n"
            +"Minecraft: "+EvatexPlatform.modVersion("minecraft")+"\n"
            +EvatexPlatform.loaderName()+": "+EvatexPlatform.loaderVersion()+"\n"
            +"OS: "+System.getProperty("os.name")+" "+System.getProperty("os.version")+" "+System.getProperty("os.arch")+"\n"
            +"Java: "+System.getProperty("java.version")+"\nGPU renderer: "+gpu+"\n"
            +VideoBackground.INSTANCE.diagnosticPlayback()
            +"Configured FPS: "+config.fps+"\nDecode height limit: "+config.maxDecodeHeight
            +"\nResolution scale: "+config.scale+"\nHardware acceleration requested: "+config.hardwareAcceleration
            +"\nInstalled mod count: "+EvatexPlatform.modCount()+"\n"
            +"Privacy: excludes background paths, account names and connection secrets.\n";
    }
}
