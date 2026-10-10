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
    @Unique private int evatexqa$stage;
    @Unique private int evatexqa$waiting;
    @Unique private class_437 evatexqa$title;
    @Inject(method="method_1574", at=@At("TAIL"))
    private void evatexqa$scenes(CallbackInfo ci) {
        class_310 client=(class_310)(Object)this;
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
        if (evatexqa$stage==1 && !net.minecraft.class_2561.method_43471("evatex.title").getString().equals("Video Menüsü")) throw new AssertionError("Turkish translations not loaded");
        evatexqa$capture(client, "stage-"+evatexqa$stage);
        switch (evatexqa$stage++) {
            case 0 -> { evatexqa$title=client.field_1755; client.method_1507(new VideoSelectScreen(evatexqa$title,0)); }
            case 1 -> {
                VideoMenuConfig cfg=VideoMenuConfig.load();  cfg.enabled=true; cfg.fps=30; cfg.fillScreen=true; cfg.save(); AstralTheme.animations(cfg.themeTransitions);
                VideoBackground.INSTANCE.reload(cfg); client.method_1507(new VideoSelectScreen(evatexqa$title,0));
            }
            case 2 -> {
                VideoMenuConfig cfg=VideoMenuConfig.load(); cfg.themeTransitions=false; cfg.save(); AstralTheme.animations(cfg.themeTransitions);
                VideoBackground.INSTANCE.reload(cfg); client.method_1507(new VideoSelectScreen(evatexqa$title,0));
            }
            case 3 -> client.method_1507(new class_500(evatexqa$title));
            case 4 -> client.method_1507(evatexqa$title);
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
    @Unique private static void evatexqa$capture(class_310 client, String name) {
        Path file=FabricLoader.getInstance().getGameDir().resolve(name+".png");
        class_318.method_1663(client.method_1522(), image -> {
            try { image.method_4314(file); }
            catch (Exception e) { throw new RuntimeException(e); }
            finally { image.close(); }
        });
    }
}
