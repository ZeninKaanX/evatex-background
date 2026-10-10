package com.videomenu;

import com.videomenu.video.VideoBackground;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;

public final class VideoSelectScreen extends class_437 {
    private static final double[] QUALITY = {0.25, 0.5, 0.75, 1.0};
    private static final int[] FPS = {15, 24, 30, 45, 60, 90, 120};
    private static final double[] DARK = {0, 0.15, 0.25, 0.4, 0.6, 0.8};
    private final class_437 parent;
    private VideoMenuConfig config;
    private List<Path> videos = List.of();
    private int page, rows, left, right, colWidth, top, step;
    private String folderError;
    private final long openedAt = System.nanoTime();

    public VideoSelectScreen(class_437 parent, int page) {
        super(text("title")); this.parent = parent; this.page = page;
    }
    private static class_2561 text(String key, Object... args) { return class_2561.method_43469("evatex."+key, args); }
    private static class_2561 onOff(boolean value) { return class_2561.method_43471(value ? "options.on" : "options.off"); }
    @Override protected void method_25426() {
        if (config == null) config = VideoMenuConfig.load();
        videos = VideoFiles.discover(config.videoPath);
        colWidth = Math.min(240, Math.max(80, (field_22789-36)/2));
        left = field_22789/2-colWidth-6; right = field_22789/2+6;
        top = 62; step = Math.min(24, Math.max(16, (field_22790-118)/7));
        rows = Math.max(1, (field_22790-126)/step);
        int pages = Math.max(1, (videos.size()+rows-1)/rows);
        page = Math.max(0, Math.min(page, pages-1));
        Path current = VideoFiles.resolve(config.videoPath);
        for (int i = page*rows; i < Math.min(videos.size(), (page+1)*rows); i++) {
            Path video = videos.get(i);
            boolean selected = video.equals(current);
            add(left, top+(i-page*rows)*step, colWidth, step-2,
                class_2561.method_43470((selected ? "› " : "")+video.getFileName()), button -> {
                    config.videoPath = video.toString(); apply();
                }, selected);
        }
        if (pages > 1) {
            int y = field_22790-60;
            var prev = add(left, y, 28, 18, text("previous"), b -> { page--; rebuild(); }, false);
            var next = add(left+colWidth-28, y, 28, 18, text("next"), b -> { page++; rebuild(); }, false);
            prev.field_22763 = page > 0; next.field_22763 = page < pages-1;
        }
        add(right, top, colWidth, step-2, text("quality", Math.round(config.scale*100)), b -> { config.scale = cycle(QUALITY, config.scale); apply(); }, false);
        add(right, top+step, colWidth, step-2, text("fps", config.fps), b -> {
            int index = 0; while (index < FPS.length && FPS[index] <= config.fps) index++;
            config.fps = FPS[index % FPS.length]; apply();
        }, false);
        add(right, top+step*2, colWidth, step-2, text("fit", text(config.fillScreen ? "fill" : "contain")), b -> { config.fillScreen = !config.fillScreen; apply(); }, false);
        add(right, top+step*3, colWidth, step-2, text("darkening", Math.round(config.darkening*100)), b -> { config.darkening = cycle(DARK, config.darkening); apply(); }, false);
        add(right, top+step*4, colWidth, step-2, text("vignette", onOff(config.showVignette)), b -> { config.showVignette = !config.showVignette; apply(); }, false);
        add(right, top+step*5, colWidth, step-2, text("enabled", onOff(config.enabled)), b -> { config.enabled = !config.enabled; apply(); }, config.enabled);
        add(right, top+step*6, colWidth, step-2, text("transitions", onOff(config.themeTransitions)), b -> { config.themeTransitions = !config.themeTransitions; apply(); }, false);
        int total = colWidth*2+12, third = (total-12)/3, y = field_22790-30;
        add(left, y, third, 20, text("folder"), b -> openFolder(), false);
        add(left+third+6, y, third, 20, text("refresh"), b -> { folderError = null; rebuild(); VideoBackground.INSTANCE.reload(config); }, false);
        add(left+(third+6)*2, y, total-(third+6)*2, 20, class_2561.method_43471("gui.back"), b -> method_25419(), false);
    }
    private AstralButton add(int x, int y, int w, int h, class_2561 label, class_4185.class_4241 action, boolean selected) {
        return method_37063(new AstralButton(x, y, w, h, label, action, selected));
    }
    private void rebuild() {
        int focus = method_25396().indexOf(method_25399());
        method_37067(); method_25426();
        if (focus >= 0 && focus < method_25396().size()) method_48265(method_25396().get(focus));
    }
    private void apply() { config.save(); AstralTheme.animations(config.themeTransitions); VideoBackground.INSTANCE.reload(config); rebuild(); }
    @Override public void method_25419() { field_22787.method_1507(parent); }
    @Override public void method_25420(class_332 context, int mx, int my, float delta) {}
    @Override public boolean method_25421() { return false; }
    @Override public boolean method_25422() { return false; }
    @Override public void method_25394(class_332 ctx, int mx, int my, float delta) {
        var palette = AstralTheme.current();
        boolean preview = VideoBackground.INSTANCE.render(ctx, field_22789, field_22790);
        AstralBackdrop.render(ctx, field_22789, field_22790, preview, config.themeTransitions);
        ctx.method_25294(left-6, 44, left+colWidth+6, field_22790-38, palette.panel());
        ctx.method_25294(right-6, 44, right+colWidth+6, field_22790-38, palette.panel());
        ctx.method_25294(left-6, 44, left+colWidth+6, 45, palette.accent());
        ctx.method_25294(right-6, 44, right+colWidth+6, 45, palette.secondary());
        ctx.method_25300(field_22793, "EVATEX / ASTRAL", field_22789/2, 10, palette.text());
        ctx.method_25300(field_22793, text("subtitle").getString(), field_22789/2, 26, palette.muted());
        ctx.method_25303(field_22793, text("library", videos.size()).getString(), left, 49, palette.accent());
        ctx.method_25303(field_22793, text("settings").getString(), right, 49, palette.secondary());
        if (videos.isEmpty()) {
            ctx.method_25303(field_22793, field_22793.method_27523(text("empty").getString(), colWidth), left, top+6, palette.muted());
            ctx.method_25303(field_22793, "videos / videolar", left, top+22, palette.muted());
        }
        int pages = Math.max(1, (videos.size()+rows-1)/rows);
        if (pages > 1) ctx.method_25300(field_22793, (page+1)+" / "+pages, left+colWidth/2, field_22790-55, palette.muted());
        String status = text(folderError != null ? "folder_error" : "status."+VideoBackground.INSTANCE.status()).getString();
        ctx.method_25303(field_22793, field_22793.method_27523(status, colWidth), right, field_22790-52, palette.muted());
        super.method_25394(ctx, mx, my, delta);
        ctx.method_25300(field_22793, "EverVerity • EVATEX 1.8.0", field_22789/2, field_22790-9, palette.muted());
        if (config.themeTransitions) {
            double fade = Math.max(0, 1-(System.nanoTime()-openedAt)/280_000_000.0);
            if (fade>0) ctx.method_25294(0,0,field_22789,field_22790, (int)(fade*fade*180)<<24);
        }
    }
    private void openFolder() {
        try {
            Files.createDirectories(VideoFiles.directory());
            net.minecraft.class_156.method_668().method_672(VideoFiles.directory().toFile());
        } catch (Exception e) { folderError = "folder_error"; VideoMenuClient.LOGGER.warn("Cannot open videos folder", e); }
    }
    private static double cycle(double[] values, double current) {
        for (double value : values) if (value > current+0.001) return value;
        return values[0];
    }
}
