package com.videomenu;

import java.awt.Desktop;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/** Launch the system file manager without blocking Minecraft's render thread. */
public final class FolderOpener {
    private FolderOpener() {}
    public static CompletableFuture<Boolean> open(Path directory) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path folder = directory.toAbsolutePath().normalize();
                Files.createDirectories(folder);
                for (List<String> command : commands(System.getProperty("os.name", ""), folder)) {
                    try {
                        ProcessBuilder builder = new ProcessBuilder(command);
                        // Game-native libraries must not override the file manager's libraries/plugins.
                        for (String name : List.of("LD_LIBRARY_PATH", "LD_PRELOAD", "QT_PLUGIN_PATH", "QML2_IMPORT_PATH", "GTK_PATH")) builder.environment().remove(name);
                        builder.redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD);
                        Process process = builder.start();
                        if (!process.waitFor(5, TimeUnit.SECONDS) || process.exitValue() == 0) return true;
                    } catch (java.io.IOException ignored) { /* Try the next installed desktop opener. */ }
                }
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(folder.toFile()); return true;
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            catch (Exception e) { VideoMenuClient.LOGGER.warn("Cannot open video folder", e); }
            return false;
        });
    }
    public static List<List<String>> commands(String os, Path folder) {
        String platform = os.toLowerCase(Locale.ROOT);
        if (platform.contains("mac") || platform.contains("darwin")) return List.of(List.of("open", folder.toString()));
        if (platform.contains("win")) return List.of(List.of("explorer.exe", folder.toString()));
        return List.of(List.of("xdg-open", folder.toString()), List.of("gio", "open", folder.toUri().toString()), List.of("kioclient", "exec", folder.toUri().toString()));
    }
}
