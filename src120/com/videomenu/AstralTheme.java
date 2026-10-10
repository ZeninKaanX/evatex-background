package com.videomenu;

/** EVATEX's own monochrome Astral identity. No external themes. */
public final class AstralTheme {
    public record Palette(int background, int backdrop, int panel, int accent, int secondary, int text, int muted) {}
    private static final Palette PALETTE = new Palette(0xFF080808, 0xFF161616, 0xC8111111,
        0xFFEAEAEA, 0xFFAAAAAA, 0xFFEFEFEF, 0xFF999999);
    private static boolean animate = true;
    private AstralTheme() {}
    public static void animations(boolean enabled) { animate = enabled; }
    public static boolean animated() { return animate; }
    public static Palette current() { return PALETTE; }
    public static int blend(int a, int b, double t) {
        t = Math.max(0, Math.min(1, t));
        int result = 0;
        for (int shift=0; shift<=24; shift+=8)
            result |= ((int)Math.round(((a>>>shift)&255)*(1-t)+((b>>>shift)&255)*t))<<shift;
        return result;
    }
}
