package evatexqa;
import com.videomenu.*;
import com.videomenu.video.VideoBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
/** Actual native-loader GL smoke test. Never packaged in a release. */
@Mixin(Minecraft.class)
public abstract class NativeSmokeMixin {
 @Unique private int evatexqa$ticks,evatexqa$stage;
 @Unique private Screen evatexqa$parent;
 @Inject(method="isWindowActive",at=@At("HEAD"),cancellable=true)
 private void evatexqa$focus(CallbackInfoReturnable<Boolean> ci){ci.setReturnValue(!Boolean.getBoolean("evatex.qa.unfocused"));}
 @Inject(method="tick",at=@At("TAIL"))
 private void evatexqa$tick(CallbackInfo ci) {
  Minecraft mc=(Minecraft)(Object)this;
  if(mc.getOverlay()!=null||mc.screen==null)return;
  if(evatexqa$stage==0 && !(mc.screen instanceof TitleScreen))return;
  if(++evatexqa$ticks<100)return;
  evatexqa$ticks=0;
  var bg=VideoBackground.INSTANCE;
  System.out.println("EVATEX_NATIVE_QA stage="+evatexqa$stage+" screen="+mc.screen.getClass().getName()+"\n"+bg.diagnosticPlayback());
  int captureStage=evatexqa$stage;
  net.minecraft.client.Screenshot.takeScreenshot(mc.getMainRenderTarget(),image->{
   try{image.writeToFile(EvatexPlatform.gameDir().resolve("native-stage-"+captureStage+".png"));}catch(Exception e){throw new RuntimeException(e);}finally{image.close();}
  });
  switch(evatexqa$stage++) {
   case 0 -> {
    if(!bg.isActive())throw new AssertionError("Native title video inactive: "+bg.status());
    if(!net.minecraft.network.chat.Component.translatable("evatex.title").getString().equals("Backgrounds"))throw new AssertionError("Native resources not loaded");
    if(!bg.audioStatus().equals("muted"))throw new AssertionError("QA audio must stay muted");
    evatexqa$parent=mc.screen;mc.setScreen(new VideoSelectScreen(mc.screen,0));
   }
   case 1 -> {if(!bg.isActive())throw new AssertionError("Settings video inactive");mc.setScreen(new OptionsScreen(evatexqa$parent,mc.options));}
   case 2 -> {if(!bg.isActive())throw new AssertionError("Options video inactive");mc.setScreen(new SelectWorldScreen(evatexqa$parent));}
   case 3 -> {if(!bg.isActive())throw new AssertionError("World selection video inactive");mc.setScreen(new JoinMultiplayerScreen(evatexqa$parent));}
   case 4 -> {if(!bg.isActive())throw new AssertionError("Multiplayer video inactive");System.setProperty("evatex.qa.unfocused","true");}
   case 5 -> {if(!bg.status().equals("paused"))throw new AssertionError("Focus loss must pause: "+bg.status());System.setProperty("evatex.qa.unfocused","false");}
   case 6 -> {
    if(!bg.status().equals("playing"))throw new AssertionError("Focus resume failed: "+bg.status());
    var cfg=VideoMenuConfig.load();cfg.customMainMenu=true;cfg.save();bg.reload(cfg);mc.setScreen(new TitleScreen());
   }
   case 7 -> {if(!mc.screen.getClass().getName().equals("com.videomenu.EvatexTitleScreen"))throw new AssertionError("Astral home not installed");
    try{String ns=EvatexPlatform.loaderName().equals("NeoForge") ? "net.neoforged.neoforge" : "net.minecraftforge";
      mc.setScreen((Screen)Class.forName(ns+".client.gui.ModListScreen").getConstructor(Screen.class).newInstance(mc.screen));
    }catch(Exception e){throw new RuntimeException(e);}
   }
   case 8 -> {if(!bg.isActive()||!bg.supportsMenu(mc.screen,false))throw new AssertionError("Native mods screen video inactive");
    try {
     boolean neo=EvatexPlatform.loaderName().equals("NeoForge");String ns=neo ? "net.neoforged" : "net.minecraftforge";
     var list=Class.forName(ns+".fml.ModList").getMethod("get").invoke(null);
     var container=((java.util.Optional<?>)list.getClass().getMethod("getModContainerById",String.class).invoke(list,"evatexbackgroundvideo")).orElseThrow();
     Object info=container.getClass().getMethod("getModInfo").invoke(container);
     Class<?> api=Class.forName(neo ? ns+".neoforge.client.gui.IConfigScreenFactory" : ns+".client.ConfigScreenHandler");
     var lookup=java.util.Arrays.stream(api.getMethods()).filter(m->m.getName().equals(neo ? "getForMod" : "getScreenFactoryFor")).findFirst().orElseThrow();
     Object factory=((java.util.Optional<?>)lookup.invoke(null,info)).orElseThrow();
     if(neo)mc.setScreen((Screen)api.getMethod("createScreen",Class.forName(ns+".fml.ModContainer"),Screen.class).invoke(factory,container,mc.screen));
     else mc.setScreen((Screen)((java.util.function.BiFunction)factory).apply(mc,mc.screen));
    }catch(Exception e){throw new RuntimeException("Native config extension failed",e);}
   }
   case 9 -> {if(!(mc.screen instanceof VideoSelectScreen))throw new AssertionError("Registered config screen missing");
    System.out.println("EVATEX_NATIVE_QA COMPLETE "+EvatexPlatform.loaderName()+" "+EvatexPlatform.loaderVersion());mc.stop();}
  }
 }
}
