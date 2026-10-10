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
            org.lwjgl.glfw.GLFW.glfwSetWindowPos(window,x[0]+100,y[0]+100);
            System.out.println("EVATEX_QA secondary display="+org.lwjgl.glfw.GLFW.glfwGetMonitorName(selected)+" position="+(x[0]+100)+","+(y[0]+100));
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
        System.out.println("EVATEX_QA stage="+evatexqa$stage+" screen="+client.field_1755.getClass().getName()+" status="+VideoBackground.INSTANCE.status());
        if (evatexqa$stage<8 && !VideoBackground.INSTANCE.status().equals("playing")) throw new AssertionError("Video did not continue in menu");
        if (evatexqa$stage==8 && !VideoBackground.INSTANCE.status().equals("idle")) throw new AssertionError("Disabled video kept playing");
        if (evatexqa$stage==1 && System.getProperty("evatex.qa.language","tr_tr").equals("tr_tr") && !net.minecraft.class_2561.method_43471("evatex.title").getString().equals("Video Menüsü")) throw new AssertionError("Turkish translations not loaded");
        evatexqa$capture(client, "stage-"+evatexqa$stage);
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
                    var method=net.minecraft.class_357.class.getDeclaredMethod("method_25347",double.class);method.setAccessible(true);method.invoke(slider,.35);
                    var volume=VideoSelectScreen.class.getDeclaredField("config");volume.setAccessible(true);
                    if(((VideoMenuConfig)volume.get(client.field_1755)).audioVolume!=.35)throw new AssertionError("Volume slider did not apply");
                    method.invoke(slider,1.0);
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
