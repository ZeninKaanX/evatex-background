package evatexqa;
import com.videomenu.AstralTheme;
import com.videomenu.VideoMenuConfig;
import com.videomenu.VideoSelectScreen;
import com.videomenu.video.VideoBackground;
import java.nio.file.Path;
import net.minecraft.class_310;
import net.minecraft.class_318;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_500;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Test-only harness; never included in a distribution jar. */
@Mixin(class_310.class)
public abstract class SmokeMixin {
    @Unique private int evatexqa$ticks;
    @Unique private boolean evatexqa$positioned;
    @Unique private int evatexqa$stage;
    @Unique private int evatexqa$waiting;
    @Unique private class_437 evatexqa$title;
    @Unique private Object evatexqa$decoder;
    @Unique private long evatexqa$minimizedFrames;
    @Unique private net.minecraft.class_4185 evatexqa$competitor;
    @Unique private com.videomenu.ShortcutLayout.Rect evatexqa$anchor;
    @Inject(method="method_1569",at=@At("HEAD"),cancellable=true)
    private void evatexqa$focus(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> ci) {
        if(Boolean.getBoolean("evatex.qa.hiddenWindow"))ci.setReturnValue(!Boolean.getBoolean("evatex.qa.unfocused"));
    }
    @Inject(method="method_1574", at=@At("TAIL"))
    private void evatexqa$scenes(CallbackInfo ci) {
        class_310 client=(class_310)(Object)this;
        if (!evatexqa$positioned) {
            long window=client.method_22683().method_4490();
            org.lwjgl.PointerBuffer monitors=org.lwjgl.glfw.GLFW.glfwGetMonitors();
            long selected=0; int rightmost=Integer.MIN_VALUE;
            if(monitors!=null) for(int i=0;i<monitors.limit();i++) {
                long monitor=monitors.get(i); int[] x={0},y={0};
                org.lwjgl.glfw.GLFW.glfwGetMonitorPos(monitor,x,y);
                if(monitor!=org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor() && x[0]>rightmost) { selected=monitor;rightmost=x[0]; }
            }
            if(selected==0) throw new AssertionError("Secondary display required for QA");
            int[] x={0},y={0}; org.lwjgl.glfw.GLFW.glfwGetMonitorPos(selected,x,y);
            var mode=org.lwjgl.glfw.GLFW.glfwGetVideoMode(selected);
            int offsetX=mode!=null && Integer.getInteger("evatex.qa.width",1280)>=mode.width() ? 0 : 100;
            int offsetY=mode!=null && Integer.getInteger("evatex.qa.height",720)>=mode.height() ? 0 : 100;
            org.lwjgl.glfw.GLFW.glfwSetWindowPos(window,x[0]+offsetX,y[0]+offsetY);
            System.out.println("EVATEX_QA secondary display="+org.lwjgl.glfw.GLFW.glfwGetMonitorName(selected)+" position="+(x[0]+offsetX)+","+(y[0]+offsetY));
            evatexqa$positioned=true;
        }
        if (client.method_18506()!=null || client.field_1755==null) return;
        if (evatexqa$stage==0 && !(client.field_1755 instanceof class_442)) {
            if (++evatexqa$waiting==60) {
                System.out.println("EVATEX_QA WAIT "+client.field_1755.getClass().getName());
                evatexqa$capture(client,"waiting");
            }
            return;
        }
        int stageTicks=Integer.getInteger("evatex.qa.stageTicks",100);
        if (++evatexqa$ticks<(evatexqa$stage==0 ? stageTicks*3 : stageTicks)) return;
        evatexqa$ticks=0;
        if(Boolean.getBoolean("evatex.qa.fontProbe")) {
            if(evatexqa$stage++==0)try {client.method_1507((class_437)Class.forName("evatextest.FontProbeScreen").getConstructor().newInstance());}catch(Exception e){throw new RuntimeException(e);}
            else {evatexqa$capture(client,"font-probe");System.out.println("EVATEX_QA COMPLETE font probe");client.method_1592();}
            return;
        }
        if (Boolean.getBoolean("evatex.qa.astralMenus")) { evatexqa$astralMenus(client); return; }
        if (Boolean.getBoolean("evatex.qa.homeMenu")) { evatexqa$home(client); return; }
        if (Boolean.getBoolean("evatex.qa.collisionLayout")) { evatexqa$collision(client); return; }
        if (Boolean.getBoolean("evatex.qa.reportedMenus")) { evatexqa$reportedMenus(client); return; }
        if (Boolean.getBoolean("evatex.qa.extendedMenus")) { evatexqa$menus(client); return; }
        if (Boolean.getBoolean("evatex.qa.reviewDiagnostics")) {evatexqa$reviewDiagnostics(client);return;}
        if (Boolean.getBoolean("evatex.qa.shortcutOnly") && evatexqa$stage==1) {
            System.out.println("EVATEX_QA COMPLETE shortcut layout");client.method_1592();return;
        }
        System.out.println("EVATEX_QA stage="+evatexqa$stage+" screen="+client.field_1755.getClass().getName()+" status="+VideoBackground.INSTANCE.status());
        if (evatexqa$stage<8 && !VideoBackground.INSTANCE.status().equals("playing")) throw new AssertionError("Video did not continue in menu");
        if (client.field_1755 instanceof class_442) {
            for (var child : client.field_1755.method_25396()) {
                if (child instanceof com.videomenu.AstralButton button && button.method_25369().getString().equals(net.minecraft.class_2561.method_43471("evatex.title").getString())) {
                    if (button.field_22764 && !com.videomenu.ShortcutLayout.clear(
                        new com.videomenu.ShortcutLayout.Rect(button.method_46426(),button.method_46427(),button.method_25368(),button.method_25364()),
                        client.field_1755.field_22789,client.field_1755.field_22790,com.videomenu.MenuShortcut.occupied(client.field_1755,button),2))
                        throw new AssertionError("Background shortcut overlaps a visible menu control");
                }
            }
        }
        if (evatexqa$stage==8 && !VideoBackground.INSTANCE.status().equals("idle")) throw new AssertionError("Disabled video kept playing");
        if (evatexqa$stage==1 && System.getProperty("evatex.qa.language","tr_tr").equals("tr_tr") && !net.minecraft.class_2561.method_43471("evatex.title").getString().equals("Arka Plan")) throw new AssertionError("Turkish translations not loaded");
        evatexqa$capture(client, "stage-"+evatexqa$stage);
        if (Boolean.getBoolean("evatex.qa.shortcutOnly")) {evatexqa$stage++;return;}
        switch (evatexqa$stage++) {
            case 0 -> {
                try {
                    var field=net.minecraft.class_4008.class.getDeclaredField("field_17906");field.setAccessible(true);
                    var pool=(java.util.List<?>)field.get(client.method_18095());
                    var names=pool.stream().map(value->value instanceof String ? (String)value : ((net.minecraft.class_2561)value).getString()).toList();
                    if(com.videomenu.SplashMessages.all().size()!=20 || names.size()<=20 || !names.containsAll(com.videomenu.SplashMessages.all()))throw new AssertionError("Custom splashes missing or vanilla pool replaced");
                    System.out.println("EVATEX_QA splash pool="+names.size()+" custom="+com.videomenu.SplashMessages.all().size());
                }catch(Exception e){throw new RuntimeException(e);}
                evatexqa$title=client.field_1755; client.method_1507(new VideoSelectScreen(evatexqa$title,0)); }
            case 1 -> {
                evatexqa$invoke(client.field_1755,"changeSetting",new Class<?>[]{int.class},1);
                evatexqa$invoke(client.field_1755,"apply",new Class<?>[]{});
                VideoMenuConfig cfg=VideoMenuConfig.load();  cfg.enabled=true; cfg.fps=30; cfg.fillScreen=true; cfg.save(); AstralTheme.animations(cfg.themeTransitions);
                VideoBackground.INSTANCE.reload(cfg); client.method_1507(new VideoSelectScreen(evatexqa$title,0));
            }
            case 2 -> {
                try {
                    Object slider=client.field_1755.method_25396().stream().filter(child->child instanceof com.videomenu.AstralVolumeSlider).findFirst().orElseThrow();
                    var method=net.minecraft.class_357.class.getDeclaredMethod("method_25347",double.class);method.setAccessible(true);method.invoke(slider,.05);
                    var volume=VideoSelectScreen.class.getDeclaredField("config");volume.setAccessible(true);
                    if(((VideoMenuConfig)volume.get(client.field_1755)).audioVolume!=.05)throw new AssertionError("Volume slider did not apply");
                    method.invoke(slider,.1);
                }catch(Exception e){throw new RuntimeException(e);}
                if (!VideoBackground.INSTANCE.audioStatus().equals("playing")) throw new AssertionError("Audio unavailable: "+VideoBackground.INSTANCE.audioStatus());
                evatexqa$invoke(client.field_1755,"changeSetting",new Class<?>[]{int.class},1);
                evatexqa$invoke(client.field_1755,"apply",new Class<?>[]{});
                try { var page=VideoSelectScreen.class.getDeclaredField("settingsPage");page.setAccessible(true);page.setInt(client.field_1755,1); }
                catch(Exception e){throw new RuntimeException(e);}
                evatexqa$invoke(client.field_1755,"rebuild",new Class<?>[]{});
            }
            case 3 -> {
                if (!VideoBackground.INSTANCE.audioStatus().equals("muted")) throw new AssertionError("Audio did not mute");
                VideoMenuConfig cfg=VideoMenuConfig.load(); cfg.themeTransitions=false; cfg.save(); AstralTheme.animations(cfg.themeTransitions);
                VideoBackground.INSTANCE.reload(cfg); client.method_1507(new class_500(evatexqa$title));
            }
            case 4 -> {
                try {
                    var field=net.minecraft.class_442.class.getDeclaredField("field_2586");field.setAccessible(true);
                    field.set(evatexqa$title,new net.minecraft.class_8519(net.minecraft.class_2561.method_43470("C₄H₉N₃O₂ · H₂O")));
                }catch(Exception e){throw new RuntimeException(e);}
                client.method_1507(evatexqa$title);
            }
            case 5 -> {
                if (FabricLoader.getInstance().isModLoaded("atlas-client")) try {
                    Class.forName("me.koraizen.atlasclient.gui.UIModeManager").getMethod("setVanillaMode",boolean.class).invoke(null,true);
                } catch (Exception e) { throw new RuntimeException(e); }
            }
            case 6 -> client.method_1507(new class_500(evatexqa$title));
            case 7 -> {
                VideoMenuConfig cfg=VideoMenuConfig.load(); cfg.enabled=false; cfg.save(); VideoBackground.INSTANCE.reload(cfg);
                client.method_1507(evatexqa$title);
            }
            case 8 -> {
                System.out.println("EVATEX_QA COMPLETE"); client.method_1592();
            }
        }
    }
    @Unique private void evatexqa$reviewDiagnostics(class_310 client) {
        if(!VideoBackground.INSTANCE.status().equals("playing"))throw new AssertionError("Review video not playing");
        if(evatexqa$stage++==0) {
            evatexqa$capture(client,"review-vanilla");
            evatexqa$title=client.field_1755;client.method_1507(new VideoSelectScreen(evatexqa$title,0));return;
        }
        VideoSelectScreen screen=(VideoSelectScreen)client.field_1755;
        try {
            var page=VideoSelectScreen.class.getDeclaredField("settingsPage");page.setAccessible(true);page.setInt(screen,2);
            var rebuild=VideoSelectScreen.class.getDeclaredMethod("rebuild");rebuild.setAccessible(true);rebuild.invoke(screen);
            String previous=client.field_1774.method_1460();
            try {
                evatexqa$press(evatexqa$button(screen,net.minecraft.class_2561.method_43471("evatex.diagnostics_copy")));
                String report=client.field_1774.method_1460();
                if(!report.contains("EVATEX playback diagnostics") || !report.contains("Decoder: SOFTWARE") || !report.contains("GPU renderer:") || !report.contains("Configured FPS:"))throw new AssertionError("Incomplete clipboard diagnostic report");
                if(report.contains("http://") || report.contains("/home/") || report.contains(".mp4"))throw new AssertionError("Diagnostic report leaked a connection secret or file path");
                evatexqa$button(screen,net.minecraft.class_2561.method_43471("evatex.diagnostics_copied"));
                System.out.println("EVATEX_QA PASS diagnostic action, real clipboard, report privacy, translated feedback and software playback");
            } finally {client.field_1774.method_1455(previous);}
            System.out.println("EVATEX_QA COMPLETE review diagnostics");client.method_1592();
        }catch(ReflectiveOperationException e){throw new RuntimeException(e);}
    }
    @Unique private void evatexqa$press(net.minecraft.class_4185 button) {
        try {
            for(var method:button.getClass().getMethods()) if(method.getName().equals("method_25306")) {
                if(method.getParameterCount()==0) method.invoke(button);else if(method.getParameterCount()==1) method.invoke(button,new Object[]{null});
                return;
            }
            throw new AssertionError("Missing native button action");
        }catch(ReflectiveOperationException e){throw new RuntimeException(e);}
    }
    @Unique private net.minecraft.class_4185 evatexqa$button(class_437 screen,net.minecraft.class_2561 label) {
        for(var child:screen.method_25396()) if(child instanceof net.minecraft.class_4185 button && button.method_25369().getString().equals(label.getString())) return button;
        throw new AssertionError("Button missing: "+label.getString());
    }
    @Unique private boolean evatexqa$atlasCustom() {
        try {return (boolean)Class.forName("com.videomenu.AtlasCompatibility").getMethod("isCustomUi").invoke(null);}
        catch(ReflectiveOperationException e){throw new RuntimeException(e);}
    }
    @Unique private void evatexqa$homeGeometry(class_437 screen) {
        java.util.List<com.videomenu.ShortcutLayout.Rect> bounds=new java.util.ArrayList<>();
        for(var child:screen.method_25396()) if(child instanceof net.minecraft.class_339 widget && widget.field_22764) {
            var rect=new com.videomenu.ShortcutLayout.Rect(widget.method_46426(),widget.method_46427(),widget.method_25368(),widget.method_25364());
            if(!com.videomenu.ShortcutLayout.clear(rect,screen.field_22789,screen.field_22790,bounds,0)) throw new AssertionError("Astral home controls overlap or leave the window: "+rect);
            bounds.add(rect);
        }
    }
    @Unique private void evatexqa$astralMenus(class_310 client) {
        var screen=client.field_1755;
        System.out.println("EVATEX_QA Astral menus stage="+evatexqa$stage+" screen="+screen.getClass().getName());
        switch(evatexqa$stage++) {
            case 0 -> {
                var config=VideoMenuConfig.load();config.customMainMenu=true;config.save();VideoBackground.INSTANCE.reload(config);
                var servers=new class_500(screen);client.method_1507(servers);
                var info=new net.minecraft.class_642("EVATEX Local Test",System.getProperty("evatex.qa.server","127.0.0.1:9"),net.minecraft.class_642.class_8678.field_45611);
                servers.method_2529().method_2988(info,false);
                for(var child:servers.method_25396())if(child instanceof net.minecraft.class_4267 list){
                    list.method_20125(servers.method_2529());list.method_25313(list.method_25396().get(0));
                }
            }
            case 1 -> {
                if(!com.videomenu.AstralMenus.active(screen))throw new AssertionError("Server skin missing");
                for(var child:screen.method_25396())if(child instanceof net.minecraft.class_4267 list){
                    if(!(list.method_25334() instanceof net.minecraft.class_4267.class_4270 selected) || !selected.method_20133().field_3752.equals("EVATEX Local Test"))
                        throw new AssertionError("Selected server must be available to the details card");
                }
                evatexqa$homeGeometry(screen);evatexqa$capture(client,"astral-servers");
                evatexqa$press(evatexqa$button(screen,net.minecraft.class_2561.method_43471("selectServer.add")));
            }
            case 2 -> {if(screen instanceof class_500)throw new AssertionError("Add server action failed");screen.method_25419();}
            case 3 -> {if(!(screen instanceof class_500))throw new AssertionError("Server editor return failed");screen.method_25419();}
            case 4 -> client.method_1507(new net.minecraft.class_526(screen));
            case 5 -> {
                if(!com.videomenu.AstralMenus.active(screen))throw new AssertionError("World skin missing");
                evatexqa$homeGeometry(screen);
                for(var child:screen.method_25396())if(child instanceof net.minecraft.class_528 list && !list.method_25396().isEmpty())list.method_25313(list.method_25396().get(0));
                evatexqa$stage=50;
            }
            case 50 -> {evatexqa$capture(client,"astral-worlds");screen.method_25419();evatexqa$stage=6;
            }
            case 6 -> client.method_1507(new net.minecraft.class_429(screen,client.field_1690));
            case 7 -> {
                if(!com.videomenu.AstralMenus.active(screen))throw new AssertionError("Options skin missing");
                evatexqa$homeGeometry(screen);evatexqa$capture(client,"astral-options");
                evatexqa$press(evatexqa$button(screen,net.minecraft.class_2561.method_43471("options.resourcepack")));
            }
            case 8 -> {if(screen instanceof net.minecraft.class_429)throw new AssertionError("Pack settings action failed");evatexqa$capture(client,"astral-packs");screen.method_25419();}
            case 9 -> {
                if(!(screen instanceof net.minecraft.class_429))throw new AssertionError("Options return failed");
                if(FabricLoader.getInstance().isModLoaded("modmenu"))try {client.method_1507((class_437)Class.forName("com.terraformersmc.modmenu.gui.ModsScreen").getConstructor(class_437.class).newInstance(screen));
                    if(Boolean.getBoolean("evatex.qa.gallery")) {
                        var field=client.field_1755.getClass().getDeclaredField("modList");field.setAccessible(true);Object list=field.get(client.field_1755);
                        for(Object entry:(java.util.List<?>)list.getClass().getMethod("method_25396").invoke(list)) {
                            Object mod=entry.getClass().getMethod("getMod").invoke(entry);
                            if(Class.forName("com.terraformersmc.modmenu.util.mod.Mod").getMethod("getId").invoke(mod).equals("evatexbackgroundvideo")) {
                                list.getClass().getMethod("select",Class.forName("com.terraformersmc.modmenu.gui.widget.entries.ModListEntry")).invoke(list,entry);break;
                            }
                        }
                    }
                    evatexqa$stage=90;}catch(ReflectiveOperationException error){throw new RuntimeException(error);}
                else client.method_1507(new VideoSelectScreen(screen,0));
            }
            case 90 -> {
                if(!VideoBackground.isModsScreen(screen) || !VideoBackground.INSTANCE.supportsMenu(screen,false) || !VideoBackground.INSTANCE.status().equals("playing"))throw new AssertionError("Mod Menu wallpaper did not play");
                evatexqa$capture(client,"astral-mods");
                if(Boolean.getBoolean("evatex.qa.modMenuBridge"))try {
                    var api=Class.forName("com.terraformersmc.modmenu.api.ModMenuApi");
                    Object bridge=FabricLoader.getInstance().getEntrypoints("modmenu",api).stream().filter(value->value.getClass().getName().equals("com.videomenu.ModMenuBridge")).findFirst().orElseThrow();
                    Object factory=api.getMethod("getModConfigScreenFactory").invoke(bridge);
                    var settings=(class_437)Class.forName("com.terraformersmc.modmenu.api.ConfigScreenFactory").getMethod("create",class_437.class).invoke(factory,screen);
                    client.method_1507(settings);
                    if(!(client.field_1755 instanceof VideoSelectScreen))throw new AssertionError("Mod Menu config did not open EVATEX");
                    settings.method_25419();
                    if(client.field_1755!=screen)throw new AssertionError("Mod Menu config return lost its parent");
                    System.out.println("EVATEX_QA PASS optional Mod Menu settings entrypoint and return");
                }catch(ReflectiveOperationException error){throw new RuntimeException(error);}

                if(Boolean.getBoolean("evatex.qa.captureModsOnly")){System.out.println("EVATEX_QA COMPLETE embedded mod icon capture");client.method_1592();return;}
                screen.method_25419();evatexqa$stage=91;
            }
            case 91 -> {client.method_1507(new VideoSelectScreen(screen,0));evatexqa$stage=10;}

            case 10 -> {
                evatexqa$capture(client,"astral-backgrounds");
                evatexqa$press(evatexqa$button(screen,net.minecraft.class_2561.method_43471("evatex.category.appearance")));evatexqa$stage=100;
            }
            case 100 -> {
                evatexqa$capture(client,"astral-appearance");
                evatexqa$press(evatexqa$button(screen,net.minecraft.class_2561.method_43471("evatex.category.menus")));evatexqa$stage=101;
            }
            case 101 -> {
                evatexqa$capture(client,"astral-menu-settings");
                var config=VideoMenuConfig.load();config.customMainMenu=false;config.save();VideoBackground.INSTANCE.reload(config);
                client.method_1507(new class_500(new class_442()));evatexqa$stage=11;
            }
            case 11 -> {
                if(com.videomenu.AstralMenus.active(screen))throw new AssertionError("Native menus not restored");
                var config=VideoMenuConfig.load();config.customMainMenu=true;config.save();VideoBackground.INSTANCE.reload(config);
                client.method_1507(new class_500(new class_442()));
            }
            case 12 -> {
                if(!com.videomenu.AstralMenus.active(screen))throw new AssertionError("Re-enabled style missing");
                @SuppressWarnings("unchecked") var children=(java.util.List<net.minecraft.class_364>)(java.util.List<?>)screen.method_25396();
                children.add(new evatextest.MainMenuScreen.UnreadableWidget());
            }
            case 13 -> {
                if(com.videomenu.AstralMenus.active(screen))throw new AssertionError("Late foreign control must restore native layout");
                if(evatexqa$button(screen,net.minecraft.class_2561.method_43471("selectServer.select")).method_46427()==62)throw new AssertionError("Foreign-control fallback did not restore original bounds");
                org.lwjgl.glfw.GLFW.glfwSetWindowSize(client.method_22683().method_4490(),640,720);
            }
            case 14 -> {client.method_15993();client.method_1507(new class_500(new class_442()));}
            case 15 -> {
                System.out.println("EVATEX_QA compact screen="+screen.field_22789+"x"+screen.field_22790);
                if(screen.field_22789>=360)evatexqa$compact(client,screen);
                if(com.videomenu.AstralMenus.active(screen))throw new AssertionError("Compact menus must retain native layout");
                System.out.println("EVATEX_QA COMPLETE Astral native menus: geometry, native actions, return paths, opt-out, compact fallback and late foreign-control recovery");client.method_1592();
            }
        }
    }
    @Unique private static void evatexqa$compact(class_310 client,class_437 screen) {
        try {
            for(var method:class_437.class.getMethods())if(method.getName().equals("method_25423")) {
                if(method.getParameterCount()==2)method.invoke(screen,320,270);
                else if(method.getParameterCount()==3)method.invoke(screen,client,320,270);
                else continue;
                if(screen.field_22789>=360)throw new AssertionError("Compact screen init failed");
                System.out.println("EVATEX_QA compact screen geometry exercised directly; hidden framebuffer resize declined");return;
            }
            throw new AssertionError("Screen init API unavailable");
        }catch(ReflectiveOperationException error){throw new RuntimeException(error);}
    }

    @Unique private void evatexqa$home(class_310 client) {
        var screen=client.field_1755;
        System.out.println("EVATEX_QA home stage="+evatexqa$stage+" screen="+screen.getClass().getName()+" size="+screen.field_22789+"x"+screen.field_22790);
        try {
            switch(evatexqa$stage++) {
                case 0 -> {
                    if(VideoMenuConfig.load().customMainMenu || screen instanceof com.videomenu.EvatexTitleScreen) throw new AssertionError("Custom home must default off");
                    client.method_1507(new VideoSelectScreen(screen,0));
                }
                case 1,10 -> {
                    if(!(screen instanceof VideoSelectScreen)) throw new AssertionError("Missing EVATEX settings");
                    var page=VideoSelectScreen.class.getDeclaredField("settingsPage");page.setAccessible(true);
                    var targetPage=VideoSelectScreen.class.getDeclaredMethod("pageForSetting",int.class);targetPage.setAccessible(true);page.setInt(screen,(int)targetPage.invoke(screen,13));
                    var rebuild=VideoSelectScreen.class.getDeclaredMethod("rebuild");rebuild.setAccessible(true);rebuild.invoke(screen);
                    boolean on=evatexqa$stage==11;
                    evatexqa$capture(client,on ? "home-setting-on" : "home-setting-off");
                    evatexqa$press(evatexqa$button(screen,net.minecraft.class_2561.method_43469("evatex.custom_main_menu",net.minecraft.class_2561.method_43471(on ? "options.on" : "options.off"))));
                    if(VideoMenuConfig.load().customMainMenu==on) throw new AssertionError("Home setting failed to persist");
                    screen.method_25419();
                    if(!on) evatexqa$stage=3;
                }
                case 3 -> {
                    if(!(screen instanceof com.videomenu.EvatexTitleScreen)) throw new AssertionError("Optional home did not open");
                    if(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("atlas-client") && evatexqa$atlasCustom()) throw new AssertionError("Atlas must leave the standalone EVATEX home visible");
                    evatexqa$homeGeometry(screen);evatexqa$capture(client,"home-wide");
                    if(Boolean.getBoolean("evatex.qa.captureHomeOnly")) {
                        System.out.println("EVATEX_QA COMPLETE home capture");client.method_1592();return;
                    }
                    long visible=screen.method_25396().stream().filter(child->child instanceof net.minecraft.class_4185 button && !(child instanceof net.minecraft.class_7077) && button.field_22764).count();
                    if(visible!=6)throw new AssertionError("Home should show only five primary actions and More: "+visible);
                    evatexqa$press(evatexqa$button(screen,net.minecraft.class_2561.method_43471("evatex.home.more")));
                    evatexqa$stage=12;
                }
                case 12 -> {
                    evatexqa$homeGeometry(screen);evatexqa$capture(client,"home-more");
                    var sourcesField=com.videomenu.EvatexTitleScreen.class.getDeclaredField("nativeSources");sourcesField.setAccessible(true);
                    @SuppressWarnings("unchecked") var sources=(java.util.Map<net.minecraft.class_4185,net.minecraft.class_4185>)sourcesField.get(screen);
                    var languageWrapper=evatexqa$button(screen,net.minecraft.class_2561.method_43471("options.language"));
                    var languageSource=sources.get(languageWrapper);if(languageSource==null)throw new AssertionError("Missing preserved language action");
                    languageSource.field_22763=false;((com.videomenu.EvatexTitleScreen)screen).captureLateButtons();
                    if(languageWrapper.field_22763)throw new AssertionError("Disabled native control remained enabled");
                    evatexqa$press(languageWrapper);if(client.field_1755!=screen)throw new AssertionError("Disabled native action was invoked");
                    languageSource.field_22763=true;languageSource.field_22764=false;((com.videomenu.EvatexTitleScreen)screen).captureLateButtons();
                    for(var child:screen.method_25396())if(child instanceof net.minecraft.class_4185 button && button.field_22764 && button.method_25369().getString().equals(net.minecraft.class_2561.method_43471("options.language").getString()))throw new AssertionError("Hidden external control remained visible");
                    languageSource.field_22764=true;((com.videomenu.EvatexTitleScreen)screen).captureLateButtons();
                    evatexqa$homeGeometry(screen);
                    if(evatexqa$button(screen,net.minecraft.class_2561.method_43471("menu.singleplayer")).field_22764)throw new AssertionError("Secondary menu must hide play controls");
                    evatexqa$press(evatexqa$button(screen,net.minecraft.class_2561.method_43471("options.language")));
                    evatexqa$stage=13;
                }
                case 13 -> {
                    if(screen instanceof com.videomenu.EvatexTitleScreen)throw new AssertionError("Secondary language action failed");
                    screen.method_25419();evatexqa$stage=15;
                }
                case 15 -> {
                    evatexqa$homeGeometry(screen);
                    if(!evatexqa$button(screen,net.minecraft.class_2561.method_43471("gui.back")).field_22764)throw new AssertionError("Secondary menu return path failed");
                    screen.method_25419();evatexqa$homeGeometry(screen);
                    if(!evatexqa$button(screen,net.minecraft.class_2561.method_43471("menu.singleplayer")).field_22764)throw new AssertionError("Closing secondary menu must restore play controls");
                    evatexqa$stage=14;
                }
                case 14 -> {
                    evatexqa$press(evatexqa$button(screen,net.minecraft.class_2561.method_43471("menu.options")));
                    evatexqa$stage=4;
                }
                case 4 -> {
                    if(!(screen instanceof net.minecraft.class_429)) throw new AssertionError("Native options action failed");
                    evatexqa$capture(client,"home-options");screen.method_25419();
                }
                case 5 -> {
                    if(!(screen instanceof com.videomenu.EvatexTitleScreen)) throw new AssertionError("Options did not return to Astral home");
                    evatexqa$press(evatexqa$button(screen,net.minecraft.class_2561.method_43471("menu.singleplayer")));
                }
                case 6 -> {
                    if(!(screen instanceof net.minecraft.class_526)) throw new AssertionError("Native singleplayer action failed");
                    evatexqa$capture(client,"home-worlds");screen.method_25419();
                }
                case 7 -> org.lwjgl.glfw.GLFW.glfwSetWindowSize(client.method_22683().method_4490(),640,720);
                case 8 -> {
                    client.method_15993();
                    screen=client.field_1755;
                    if(!(screen instanceof com.videomenu.EvatexTitleScreen)) throw new AssertionError("Worlds did not return to Astral home");
                    if(screen.field_22789>=360)evatexqa$compact(client,screen);
                    evatexqa$homeGeometry(screen);evatexqa$capture(client,"home-compact");
                    org.lwjgl.glfw.GLFW.glfwSetWindowSize(client.method_22683().method_4490(),Integer.getInteger("evatex.qa.width",1280),Integer.getInteger("evatex.qa.height",720));
                }
                case 9 -> evatexqa$press(evatexqa$button(screen,net.minecraft.class_2561.method_43471("evatex.title")));
                case 11 -> {
                    if(!(screen instanceof class_442) || screen instanceof com.videomenu.EvatexTitleScreen || VideoMenuConfig.load().customMainMenu) throw new AssertionError("Home did not disable cleanly");
                    if(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("atlas-client") && !evatexqa$atlasCustom()) throw new AssertionError("Atlas custom menu mode was not restored");
                    evatexqa$capture(client,"home-disabled");
                    if(com.videomenu.MenuShortcut.titleMenu(null))throw new AssertionError("Null screen must not be a title menu");
                    var config=VideoMenuConfig.load();config.customMainMenu=true;config.save();VideoBackground.INSTANCE.reload(config);
                    client.method_1507(new evatextest.MainMenuScreen());evatexqa$stage=20;
                }
                case 20 -> {
                    if(!(screen instanceof evatextest.MainMenuScreen))throw new AssertionError("EVATEX replaced another mod's custom main menu");
                    var unknown=new evatextest.MainMenuScreen.UnreadableWidget();
                    @SuppressWarnings("unchecked") var children=(java.util.List<net.minecraft.class_364>)(java.util.List<?>)screen.method_25396();
                    children.add(unknown);((com.videomenu.ShortcutHost)screen).evatex$refreshShortcut();
                    for(var child:children)if(child instanceof com.videomenu.AstralButton button && button.method_25369().getString().equals(net.minecraft.class_2561.method_43471("evatex.title").getString()) && button.field_22764)throw new AssertionError("Unreadable custom bounds must hide the shortcut");
                    children.remove(unknown);((com.videomenu.ShortcutHost)screen).evatex$refreshShortcut();
                    evatexqa$button(screen,net.minecraft.class_2561.method_43471("evatex.title"));
                    client.method_1507(new class_442());evatexqa$stage=21;
                }
                case 21 -> {
                    if(screen.getClass()!=class_442.class)throw new AssertionError("EVATEX fought the external menu owner");
                    var config=VideoMenuConfig.load();config.customMainMenu=false;config.save();VideoBackground.INSTANCE.reload(config);evatexqa$stage=22;
                }
                case 22 -> {
                    var config=VideoMenuConfig.load();config.customMainMenu=true;config.save();VideoBackground.INSTANCE.reload(config);evatexqa$stage=23;
                }
                case 23 -> {
                    if(!(screen instanceof com.videomenu.EvatexTitleScreen))throw new AssertionError("Explicit user enable must restore own home");
                    client.method_1507(new class_442());evatexqa$stage=24;
                }
                case 24 -> {
                    if(screen.getClass()!=class_442.class)throw new AssertionError("External native-title replacement loop");
                    var config=VideoMenuConfig.load();config.customMainMenu=false;config.save();VideoBackground.INSTANCE.reload(config);
                    var field=VideoBackground.class.getDeclaredField("decoder");field.setAccessible(true);evatexqa$decoder=field.get(VideoBackground.INSTANCE);
                    System.setProperty("evatex.qa.unfocused","true");VideoBackground.INSTANCE.setForeground(false);evatexqa$stage=30;
                }
                case 30 -> {
                    var decoder=(com.videomenu.video.VideoDecoder)evatexqa$decoder;
                    if(!decoder.isPaused() || !VideoBackground.INSTANCE.status().equals("paused"))throw new AssertionError("Focus loss did not pause playback");
                    evatexqa$minimizedFrames=decoder.publishedFrames();evatexqa$stage=31;
                }
                case 31 -> {
                    if(((com.videomenu.video.VideoDecoder)evatexqa$decoder).publishedFrames()!=evatexqa$minimizedFrames)throw new AssertionError("Hidden Minecraft kept producing video frames");
                    System.clearProperty("evatex.qa.unfocused");VideoBackground.INSTANCE.setForeground(true);evatexqa$stage=32;
                }
                case 32 -> {
                    var field=VideoBackground.class.getDeclaredField("decoder");field.setAccessible(true);
                    if(field.get(VideoBackground.INSTANCE)!=evatexqa$decoder || ((com.videomenu.video.VideoDecoder)evatexqa$decoder).publishedFrames()<=evatexqa$minimizedFrames)throw new AssertionError("Focus return did not resume existing decoder");
                    System.out.println("EVATEX_QA COMPLETE cooperative home ownership, unreadable bounds, dynamic native buttons, focus pause/resume without decoder restart");client.method_1592();
                }
            }
        }catch(ReflectiveOperationException e){throw new RuntimeException(e);}
    }

    @Unique private void evatexqa$collision(class_310 client) {
        if (evatexqa$stage==5 || evatexqa$stage==7) {
            if (!(client.field_1755 instanceof VideoSelectScreen)) throw new AssertionError("Shortcut action failed");
            evatexqa$capture(client,"layout-settings-"+evatexqa$stage);
            if (evatexqa$stage++==5) {client.method_1507(new evatextest.MainMenuScreen());return;}
            System.out.println("EVATEX_QA COMPLETE adaptive layout: late controls, visibility, resize, uniqueness, custom main menu and actions");
            client.method_1592();return;
        }
        var screen=client.field_1755;
        com.videomenu.AstralButton shortcut=null;int count=0;
        for(var child:screen.method_25396()) if(child instanceof com.videomenu.AstralButton button && button.method_25369().getString().equals(net.minecraft.class_2561.method_43471("evatex.title").getString())) {shortcut=button;count++;}
        if(count!=1 || shortcut==null || !shortcut.field_22764) throw new AssertionError("Missing/duplicated shortcut");
        var rect=new com.videomenu.ShortcutLayout.Rect(shortcut.method_46426(),shortcut.method_46427(),shortcut.method_25368(),shortcut.method_25364());
        if(!com.videomenu.ShortcutLayout.clear(rect,screen.field_22789,screen.field_22790,com.videomenu.MenuShortcut.occupied(screen,shortcut),2))
            throw new AssertionError("Actual title shortcut collision");
        if(evatexqa$stage==6 && rect.height()<=rect.width()) throw new AssertionError("Dense custom menu did not choose a vertical rail");
        System.out.println("EVATEX_QA layout stage="+evatexqa$stage+" size="+screen.field_22789+"x"+screen.field_22790+" position="+rect);
        evatexqa$capture(client,"layout-"+evatexqa$stage);
        try {
            switch(evatexqa$stage++) {
                case 0 -> {
                    evatexqa$title=screen;evatexqa$anchor=rect;
                    System.out.println("EVATEX_QA occupied="+com.videomenu.MenuShortcut.occupied(screen,shortcut));
                    evatexqa$competitor=new com.videomenu.AstralButton(rect.x(),rect.y(),rect.width(),rect.height(),net.minecraft.class_2561.method_43470("Client control"),button->{},false);
                    var add=class_437.class.getDeclaredMethod("method_37063",net.minecraft.class_364.class);add.setAccessible(true);add.invoke(screen,evatexqa$competitor);
                }
                case 1 -> {
                    if(rect.equals(evatexqa$anchor)) throw new AssertionError("Shortcut failed to move around late-added button");
                    evatexqa$competitor.field_22764=false;
                }
                case 2 -> {
                    if(!rect.equals(evatexqa$anchor)) throw new AssertionError("Shortcut failed to restore freed preferred anchor");
                    org.lwjgl.glfw.GLFW.glfwSetWindowSize(client.method_22683().method_4490(),960,540);
                }
                case 3 -> org.lwjgl.glfw.GLFW.glfwSetWindowSize(client.method_22683().method_4490(),Integer.getInteger("evatex.qa.width",1280),Integer.getInteger("evatex.qa.height",720));
                case 4,6 -> {
                    for(var method:shortcut.getClass().getMethods()) if(method.getName().equals("method_25306")) {
                        if(method.getParameterCount()==0) method.invoke(shortcut);else if(method.getParameterCount()==1) method.invoke(shortcut,new Object[]{null});
                        break;
                    }
                }
            }
        } catch(ReflectiveOperationException e) {throw new RuntimeException(e);}
    }

    @Unique private void evatexqa$reportedMenus(class_310 client) {
        try {
            var background=VideoBackground.INSTANCE;
            var field=VideoBackground.class.getDeclaredField("decoder");field.setAccessible(true);
            var decoder=(com.videomenu.video.VideoDecoder)field.get(background);
            if(!background.status().equals(evatexqa$stage==6 ? "paused" : "playing"))throw new AssertionError("Reported menu not playing: "+client.field_1755.getClass());
            if(evatexqa$stage==0)evatexqa$decoder=decoder;
            if(decoder!=evatexqa$decoder)throw new AssertionError("Reported menu transition restarted decoder");
            if(background.supportsMenu(client.field_1755,true))throw new AssertionError("In-world wallpaper exclusion lost");
            var cfg=VideoMenuConfig.load();if(cfg.audioEnabled || cfg.audioVolume!=.1)throw new AssertionError("QA must be muted at 10%");
            System.out.println("EVATEX_QA REPORTED stage="+evatexqa$stage+" screen="+client.field_1755.getClass().getName()+" frames="+decoder.publishedFrames());
            if(evatexqa$stage!=7)evatexqa$capture(client,"reported-"+evatexqa$stage);
            switch(evatexqa$stage++) {
                case 0 -> {evatexqa$title=client.field_1755;client.method_1507(new net.minecraft.class_5375(client.method_1520(),packs->{},client.method_1479(),net.minecraft.class_2561.method_43471("resourcePack.title")));}
                case 1 -> client.method_1507(new net.minecraft.class_8219(evatexqa$title));
                case 2 -> {
                    net.minecraft.class_1928 rules;
                    try {rules=net.minecraft.class_1928.class.getConstructor().newInstance();}
                    catch(NoSuchMethodException e) {rules=net.minecraft.class_1928.class.getConstructor(net.minecraft.class_7699.class).newInstance(net.minecraft.class_7701.field_40183);}
                    client.method_1507(new net.minecraft.class_5235(rules,result->{}));
                }
                case 3 -> {
                    class_437 videoScreen;
                    try {videoScreen=(class_437)net.minecraft.class_446.class.getConstructor(class_437.class,class_310.class,net.minecraft.class_315.class).newInstance(evatexqa$title,client,client.field_1690);}
                    catch(NoSuchMethodException e) {videoScreen=(class_437)net.minecraft.class_446.class.getConstructor(class_437.class,net.minecraft.class_315.class).newInstance(evatexqa$title,client.field_1690);}
                    client.method_1507(videoScreen);
                }
                case 4 -> {
                    if(FabricLoader.getInstance().isModLoaded("sodium")) {
                        var type=Class.forName("net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen");
                        client.method_1507((class_437)type.getMethod("createScreen",class_437.class).invoke(null,evatexqa$title));
                    } else client.method_1507(new net.minecraft.class_443(evatexqa$title,client.field_1690));
                }
                case 5 -> {
                    long before=decoder.publishedFrames();
                    // A render/upload may stall while holding the background object.
                    // Frame publication must use a separate lock and keep progressing.
                    synchronized(background) {Thread.sleep(350);}
                    if(decoder.publishedFrames()<=before)throw new AssertionError("Render lock blocked decoder publication");
                    System.setProperty("evatex.qa.unfocused","true");background.setForeground(false);Thread.sleep(200);
                    evatexqa$minimizedFrames=decoder.publishedFrames();
                    org.lwjgl.glfw.GLFW.glfwIconifyWindow(client.method_22683().method_4490());
                }
                case 6 -> {
                    if(org.lwjgl.glfw.GLFW.glfwGetWindowAttrib(client.method_22683().method_4490(),org.lwjgl.glfw.GLFW.GLFW_ICONIFIED)==0)throw new AssertionError("Window did not minimize");
                    if(decoder.publishedFrames()!=evatexqa$minimizedFrames)throw new AssertionError("Minimized Minecraft kept decoding video");
                    System.clearProperty("evatex.qa.unfocused");background.setForeground(true);
                    org.lwjgl.glfw.GLFW.glfwRestoreWindow(client.method_22683().method_4490());
                }
                case 7 -> {
                    if(org.lwjgl.glfw.GLFW.glfwGetWindowAttrib(client.method_22683().method_4490(),org.lwjgl.glfw.GLFW.GLFW_ICONIFIED)!=0)throw new AssertionError("Window did not restore");
                    client.method_1507(evatexqa$title);
                }
                case 8 -> {System.out.println("EVATEX_QA COMPLETE reported menus, render-lock independence, minimize/restore, loaded-world exclusion");client.method_1592();}
            }
        }catch(Exception e){throw new RuntimeException(e);}
    }
    @Unique private void evatexqa$menus(class_310 client) {
        try {
            var background=VideoBackground.INSTANCE;
            var decoder=VideoBackground.class.getDeclaredField("decoder"); decoder.setAccessible(true);
            String expected=evatexqa$stage==4 || evatexqa$stage==6 ? "idle" : "playing";
            if(!background.status().equals(expected))throw new AssertionError("Menu stage "+evatexqa$stage+" expected "+expected+" got "+background.status()+" screen="+client.field_1755.getClass().getName());
            if(evatexqa$stage==0)evatexqa$decoder=decoder.get(background);
            if(evatexqa$stage>0 && evatexqa$stage<4 && evatexqa$decoder!=decoder.get(background))throw new AssertionError("Decoder restarted during enabled menu transition");
            if(background.supportsMenu(client.field_1755,true))throw new AssertionError("Wallpaper enabled with a world loaded");
            var cfg=VideoMenuConfig.load();
            if(cfg.audioEnabled || cfg.audioVolume!=.1)throw new AssertionError("QA audio must remain muted at 10%");
            System.out.println("EVATEX_QA MENU stage="+evatexqa$stage+" screen="+client.field_1755.getClass().getName()+" status="+background.status());
            evatexqa$capture(client,"menu-"+evatexqa$stage);
            switch(evatexqa$stage++) {
                case 0 -> {evatexqa$title=client.field_1755;client.method_1507(new net.minecraft.class_526(evatexqa$title));}
                case 1 -> client.method_1507(new net.minecraft.class_429(evatexqa$title,client.field_1690));
                case 2 -> client.method_1507(new net.minecraft.class_443(client.field_1755,client.field_1690));
                case 3 -> {cfg.worldMenuBackground=false;cfg.save();background.reload(cfg);client.method_1507(new net.minecraft.class_526(evatexqa$title));}
                case 4 -> {cfg.worldMenuBackground=true;cfg.menuMeteors=false;cfg.save();background.reload(cfg);client.method_1507(new net.minecraft.class_526(evatexqa$title));}
                case 5 -> {cfg.optionsMenuBackground=false;cfg.save();background.reload(cfg);client.method_1507(new net.minecraft.class_429(evatexqa$title,client.field_1690));}
                case 6 -> {cfg.optionsMenuBackground=true;cfg.menuMeteors=true;cfg.save();background.reload(cfg);client.method_1507(new VideoSelectScreen(evatexqa$title,0));var page=VideoSelectScreen.class.getDeclaredField("settingsPage");page.setAccessible(true);page.setInt(client.field_1755,1);evatexqa$invoke(client.field_1755,"rebuild",new Class<?>[]{});}
                case 7 -> {
                    // Exercise the same actions used by the three new UI buttons and verify persistence.
                    for(int setting=10;setting<=12;setting++)evatexqa$invoke(client.field_1755,"changeSetting",new Class<?>[]{int.class},setting);
                    evatexqa$invoke(client.field_1755,"apply",new Class<?>[]{});
                    var saved=VideoMenuConfig.load();var copy=saved.copy();
                    if(copy.worldMenuBackground || copy.optionsMenuBackground || copy.menuMeteors)throw new AssertionError("Menu switches not saved/copied");
                    for(int setting=10;setting<=12;setting++)evatexqa$invoke(client.field_1755,"changeSetting",new Class<?>[]{int.class},setting);
                    evatexqa$invoke(client.field_1755,"apply",new Class<?>[]{});
                    System.out.println("EVATEX_QA COMPLETE menu continuity, independent toggles, persistence, loaded-world exclusion");client.method_1592();
                }
            }
        }catch(Exception e){throw new RuntimeException(e);}
    }
    @Unique private static void evatexqa$invoke(Object target,String name,Class<?>[] types,Object... args) {
        try {var method=VideoSelectScreen.class.getDeclaredMethod(name,types);method.setAccessible(true);method.invoke(target,args);}
        catch(Exception e){throw new RuntimeException(e);}
    }
    @Unique private static void evatexqa$capture(class_310 client, String name) {
        Path file=FabricLoader.getInstance().getGameDir().resolve(name+".png");
        class_318.method_1663(client.method_1522(), image -> {
            try { image.method_4314(file); }
            catch (Exception e) { throw new RuntimeException(e); }
            finally { image.close(); }
        });
    }
}
