package com.videomenu.video;

import java.util.regex.Pattern;

/** Keep playback speed while avoiding duplicate full-frame transfers above source FPS. */
public final class FrameRatePolicy {
    private static final Pattern FPS = Pattern.compile("(?:^|[,\\s])([0-9]+(?:\\.[0-9]+)?)(?:/([0-9]+(?:\\.[0-9]+)?))?\\s+fps\\b");
    private FrameRatePolicy() {}
    public static double parse(String videoStream) {
        var match = FPS.matcher(videoStream);
        if (!match.find()) return Double.NaN;
        try {
            double rate = Double.parseDouble(match.group(1));
            if (match.group(2) != null) rate /= Double.parseDouble(match.group(2));
            return Double.isFinite(rate) && rate >= 1 && rate <= 1000 ? rate : Double.NaN;
        } catch (NumberFormatException ignored) { return Double.NaN; }
    }
    public static double limit(int configured, double source) {
        double ceiling = Math.max(1, Math.min(120, configured));
        return Double.isFinite(source) && source >= 1 && source <= 1000 ? Math.min(ceiling, source) : ceiling;
    }
}
