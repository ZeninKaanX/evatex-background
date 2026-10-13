package com.videomenu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import com.videomenu.EvatexPlatform;

public final class VideoFiles {
    private VideoFiles() {}
    public static Path directory() { return EvatexPlatform.gameDir().resolve("videos"); }
    public static Path resolve(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            Path path = Path.of(value);
            if (!path.isAbsolute()) path = EvatexPlatform.gameDir().resolve(path);
            return path.toAbsolutePath().normalize();
        } catch (InvalidPathException e) { return null; }
    }
    public static List<Path> discover(String selected) {
        java.util.ArrayList<Path> result = new java.util.ArrayList<>();
        for (String folder : new String[]{"videos", "videolar", "backgrounds"}) {
            Path dir = EvatexPlatform.gameDir().resolve(folder);
            if (!Files.isDirectory(dir)) continue;
            try (Stream<Path> files = Files.list(dir)) {
                files.filter(Files::isRegularFile).filter(VideoFiles::isMedia)
                    .map(p -> p.toAbsolutePath().normalize()).forEach(result::add);
            } catch (IOException e) { VideoMenuClient.LOGGER.warn("Cannot read video folder {}", dir, e); }
        }
        Path current = resolve(selected);
        if (current != null && Files.isRegularFile(current) && !result.contains(current)) result.add(current);
        result.sort(Comparator.comparing(p -> p.getFileName().toString().toLowerCase(Locale.ROOT)));
        return result;
    }
    public static boolean isStillImage(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".bmp");
    }
    public static boolean isGif(Path path) { return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".gif"); }
    public static boolean isMedia(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        for (String extension : new String[]{".mp4", ".mov", ".webm", ".mkv", ".avi", ".m4v", ".gif", ".png", ".jpg", ".jpeg", ".bmp"}) {
            if (name.endsWith(extension)) return true;
        }
        return false;
    }
}
