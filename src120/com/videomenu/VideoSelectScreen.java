package com.videomenu;

import com.videomenu.video.VideoBackground;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;

public final class VideoSelectScreen extends class_437 {
    private static final double[] QUALITY = {0.25, 0.5, 0.75, 1.0};
    private static final int[] FPS = {15, 24, 30, 45, 60, 90, 120};
    private static final int[] LIMITS = {720,1080,1440,2160,4320};
    private static final double[] DARK = {0, 0.15, 0.25, 0.4, 0.6, 0.8};
    private static final int[][] CATEGORIES={{0,1,2,3,4,5},{6,7,8,9},{10,11,12,13}};
    private static final String[] CATEGORY_KEYS={"playback","appearance","menus"};
    private final class_437 parent;
    private VideoMenuConfig config;
    private List<Path> videos = List.of();
    private int page, settingsPage;
    private AstralLayout layout;
    private boolean showSettings = true;
    private volatile boolean openingFolder;
    private volatile String folderError;
    private boolean volumeDirty;
    private long volumeChangedAt;
    private final long openedAt = System.nanoTime();

    public VideoSelectScreen(class_437 parent, int page) {
        super(text("title")); this.parent = parent; this.page = page;
    }
    private static class_2561 text(String key, Object... args) { return class_2561.method_43469("evatex."+key, args); }
    private static class_2561 onOff(boolean value) { return class_2561.method_43471(value ? "options.on" : "options.off"); }
    @Override protected void method_25426() {
        if (config == null) config = VideoMenuConfig.load();
        videos = VideoFiles.discover(config.videoPath);
        layout = AstralLayout.of(field_22789,field_22790);
        int rows=layout.rows(), left=layout.left(), right=layout.right(), width=layout.colWidth();
        int videoPages=pages(videos.size()), settingPages=settingGroups().size();
        page=Math.max(0,Math.min(page,videoPages-1));
        settingsPage=Math.max(0,Math.min(settingsPage,settingPages-1));
        if (layout.singleColumn()) {
            int half=(width-6)/2;
            add(left,40,half,18,text("library_tab"),b->{showSettings=false;rebuild();},!showSettings);
            add(left+half+6,40,width-half-6,18,text("settings"),b->{showSettings=true;rebuild();},showSettings);
        }
        if (!layout.singleColumn() || !showSettings) {
            Path current=VideoFiles.resolve(config.videoPath);
            for (int i=page*rows;i<Math.min(videos.size(),(page+1)*rows);i++) {
                Path video=videos.get(i); boolean selected=video.equals(current);
                add(left,layout.top()+(i-page*rows)*layout.step(),width,layout.step()-2,
                    class_2561.method_43470((selected ? "› " : "")+video.getFileName()),b->{config.videoPath=video.toString();apply();},selected);
            }
            if (videoPages>1) pagination(left,videoPages,false);
        }
        if (!layout.singleColumn() || showSettings) {
            int[] group=settingGroups().get(settingsPage);
            for(int row=0;row<group.length;row++) {
                final int setting=group[row];
                int y=layout.top()+row*layout.step();
                if(setting==2) {method_37063(new AstralVolumeSlider(right,y,width,layout.step()-2,config.audioVolume,value->{config.audioVolume=value;volumeDirty=true;volumeChangedAt=System.nanoTime();VideoBackground.INSTANCE.reload(config);}));continue;}
                add(right,y,width,layout.step()-2,settingLabel(setting),b->{changeSetting(setting);apply();},setting==0&&config.enabled || setting==1&&config.audioEnabled || setting==10&&config.worldMenuBackground || setting==11&&config.optionsMenuBackground || setting==12&&config.menuMeteors || setting==13&&config.customMainMenu);
            }
            if(settingPages==3) {
                int tab=(width-8)/3;
                for(int i=0;i<3;i++) {final int category=i;add(right+i*(tab+4),layout.pagerY(),i==2 ? width-2*(tab+4) : tab,18,text("category."+CATEGORY_KEYS[i]),b->{settingsPage=category;rebuild();},settingsPage==i);}
            } else if(settingPages>1)pagination(right,settingPages,true);
        }
        int total=layout.singleColumn() ? width : width*2+12, third=(total-12)/3, y=layout.footerY();
        add(left,y,third,20,text("folder"),b->{if(!openingFolder)openFolder();},false);
        add(left+third+6,y,third,20,text("refresh"),b->{folderError=null;rebuild();VideoBackground.INSTANCE.reload(config);},false);
        add(left+(third+6)*2,y,total-(third+6)*2,20,class_2561.method_43471("gui.back"),b->method_25419(),false);
    }
    private java.util.List<int[]> settingGroups() {
        var groups=new java.util.ArrayList<int[]>();int capacity=Math.min(6,layout.rows());
        for(var category:CATEGORIES)for(int start=0;start<category.length;start+=capacity)groups.add(java.util.Arrays.copyOfRange(category,start,Math.min(category.length,start+capacity)));
        return groups;
    }
    private int pageForSetting(int setting){var groups=settingGroups();for(int i=0;i<groups.size();i++)for(int value:groups.get(i))if(value==setting)return i;return 0;}
    private String categoryKey(){int first=settingGroups().get(settingsPage)[0];return CATEGORY_KEYS[first<6 ? 0 : first<10 ? 1 : 2];}
    private int pages(int count) { return Math.max(1,(count+layout.rows()-1)/layout.rows()); }
    private void pagination(int x,int count,boolean settings) {
        int active=settings ? settingsPage : page;
        var prev=add(x,layout.pagerY(),26,18,text("previous"),b->{if(settings)settingsPage--;else page--;rebuild();},false);
        var next=add(x+layout.colWidth()-26,layout.pagerY(),26,18,text("next"),b->{if(settings)settingsPage++;else page++;rebuild();},false);
        prev.field_22763=active>0;next.field_22763=active<count-1;
    }
    private class_2561 settingLabel(int i) {
        return switch(i) {
            case 0 -> text("enabled",onOff(config.enabled));
            case 1 -> text("audio",onOff(config.audioEnabled));
            case 3 -> text("quality",Math.round(config.scale*100));
            case 4 -> text("decode_limit",config.maxDecodeHeight);
            case 5 -> text("fps",config.fps);
            case 6 -> text("fit",text(config.fillScreen ? "fill" : "contain"));
            case 7 -> text("darkening",Math.round(config.darkening*100));
            case 8 -> text("vignette",onOff(config.showVignette));
            case 9 -> text("transitions",onOff(config.themeTransitions));
            case 10 -> text("world_background",onOff(config.worldMenuBackground));
            case 11 -> text("options_background",onOff(config.optionsMenuBackground));
            case 12 -> text("menu_meteors",onOff(config.menuMeteors));
            case 13 -> text("custom_main_menu",onOff(config.customMainMenu));
            default -> text("volume",Math.round(config.audioVolume*100));
        };
    }
    private void changeSetting(int i) {
        switch(i) {
            case 0 -> config.enabled=!config.enabled;
            case 1 -> config.audioEnabled=!config.audioEnabled;
            case 3 -> config.scale=cycle(QUALITY,config.scale);
            case 4 -> config.maxDecodeHeight=cycle(LIMITS,config.maxDecodeHeight);
            case 5 -> config.fps=cycle(FPS,config.fps);
            case 6 -> config.fillScreen=!config.fillScreen;
            case 7 -> config.darkening=cycle(DARK,config.darkening);
            case 8 -> config.showVignette=!config.showVignette;
            case 9 -> config.themeTransitions=!config.themeTransitions;
            case 10 -> config.worldMenuBackground=!config.worldMenuBackground;
            case 11 -> config.optionsMenuBackground=!config.optionsMenuBackground;
            case 12 -> config.menuMeteors=!config.menuMeteors;
            case 13 -> config.customMainMenu=!config.customMainMenu;
        }
    }
    private AstralButton add(int x,int y,int w,int h,class_2561 label,class_4185.class_4241 action,boolean selected) {
        return method_37063(new AstralButton(x,y,w,h,label,action,selected));
    }
    private void rebuild() {
        int focus=method_25396().indexOf(method_25399());method_37067();method_25426();
        if(focus>=0&&focus<method_25396().size())method_48265(method_25396().get(focus));
    }
    private void apply() {config.save();AstralTheme.animations(config.themeTransitions);VideoBackground.INSTANCE.reload(config);rebuild();}
    @Override public void method_25419() {if(volumeDirty)config.save();field_22787.method_1507(parent);}
    @Override public void method_25420(class_332 context) {}
    @Override public boolean method_25421() {return false;}
    @Override public boolean method_25422() {return false;}
    @Override public void method_25394(class_332 ctx,int mx,int my,float delta) {
        if(volumeDirty && System.nanoTime()-volumeChangedAt>250_000_000L) {config.save();volumeDirty=false;}
        var palette=AstralTheme.current();
        boolean preview=VideoBackground.INSTANCE.render(ctx,field_22789,field_22790);
        AstralBackdrop.render(ctx,field_22789,field_22790,preview,config.themeTransitions);
        int left=layout.left(),right=layout.right(),width=layout.colWidth(),header=layout.headerY();
        if(!layout.singleColumn()||!showSettings)panel(ctx,left,header,palette.accent());
        if(!layout.singleColumn()||showSettings)panel(ctx,right,header,palette.secondary());
        AstralTypography.centered(ctx,AstralTypography.fit("EVATEX / ASTRAL",field_22789-12),field_22789/2,8,palette.text());
        AstralTypography.centered(ctx,AstralTypography.fit(text("subtitle").getString(),field_22789-12),field_22789/2,24,palette.muted());
        if(!layout.singleColumn()||!showSettings) {
            AstralTypography.draw(ctx,text("library",videos.size()).getString(),left,header,palette.accent());
            if(videos.isEmpty()) {
                AstralTypography.draw(ctx,AstralTypography.fit(text("empty").getString(),width),left,layout.top()+6,palette.muted());
                AstralTypography.draw(ctx,"videos / backgrounds",left,layout.top()+22,palette.muted());
            }
            if(pages(videos.size())>1)AstralTypography.centered(ctx,(page+1)+" / "+pages(videos.size()),left+width/2,layout.pagerY()+5,palette.muted());
        }
        if(!layout.singleColumn()||showSettings) {
            AstralTypography.draw(ctx,text("category."+categoryKey()).getString(),right,header,palette.secondary());
            if(settingGroups().size()>3)AstralTypography.centered(ctx,text("category."+categoryKey()).getString()+"  "+(settingsPage+1)+" / "+settingGroups().size(),right+width/2,layout.pagerY()+5,palette.muted());
        }
        String playback=VideoBackground.INSTANCE.status();
        String key=openingFolder ? "folder_opening" : folderError!=null ? "folder_error" : playback.equals("error") || playback.startsWith("engine_") ? "status."+playback : config.audioEnabled&&VideoBackground.INSTANCE.audioStatus().equals("unavailable") ? "audio_error" : config.audioEnabled&&VideoBackground.INSTANCE.audioStatus().equals("no_track") ? "audio_no_track" : "status."+playback;
        int center=layout.singleColumn() ? left+width/2 : right+width/2;
        AstralTypography.centered(ctx,AstralTypography.fit(text(key).getString(),width),center,layout.statusY(),palette.muted());
        super.method_25394(ctx,mx,my,delta);
        AstralTypography.centered(ctx,"EverVerity • EVATEX "+net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("evatexbackgroundvideo").orElseThrow().getMetadata().getVersion().getFriendlyString(),field_22789/2,field_22790-9,palette.muted());
        if(config.themeTransitions) {
            double fade=Math.max(0,1-(System.nanoTime()-openedAt)/280_000_000.0);
            if(fade>0)ctx.method_25294(0,0,field_22789,field_22790,(int)(fade*fade*180)<<24);
        }
    }
    private void panel(class_332 ctx,int x,int header,int accent) {
        var palette=AstralTheme.current();
        ctx.method_25294(x-6,header-5,x+layout.colWidth()+6,field_22790-36,palette.panel());
        ctx.method_25294(x-6,header-5,x+layout.colWidth()+6,header-4,accent);
    }
    private void openFolder() {
        openingFolder=true;folderError=null;
        FolderOpener.open(VideoFiles.directory()).thenAccept(success->{folderError=success ? null : "folder_error";openingFolder=false;});
    }
    private static int cycle(int[] values,int current) {for(int value:values)if(value>current)return value;return values[0];}
    private static double cycle(double[] values,double current) {for(double value:values)if(value>current+0.001)return value;return values[0];}
}
