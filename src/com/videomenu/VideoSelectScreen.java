package com.videomenu;

import com.videomenu.video.VideoBackground;
import java.util.*;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;

/** Settings only; local media and folder actions belong to My Library. */
public final class VideoSelectScreen extends class_437 {
 private static final double[] QUALITY={.25,.5,.75,1};
 private static final int[] FPS={15,24,30,45,60,90,120},LIMITS={720,1080,1440,2160,4320};
 private static final double[] DARK={0,.15,.25,.4,.6,.8};
 private static final int[][] CATEGORIES={com.videomenu.video.ExternalVideoEngine.externalDistribution()?new int[]{0,1,2,3,4,5,17}:new int[]{0,1,2,3,4,5},{6,7,8,9},{10,11,15,12,13,16,14}};
 private static final String[] CATEGORY_KEYS={"playback","appearance","menus"};
 private final class_437 parent;private VideoMenuConfig config;private int settingsPage;
 private int margin,side,contentX,contentW,columns,cardW,rows,cardH,top;
 private boolean volumeDirty,diagnosticCopied;private long volumeChangedAt;
 private record Row(int setting,int x,int y,int width){}
 private final List<Row> cards=new ArrayList<>();
 public VideoSelectScreen(class_437 parent,int page){super(text("settings"));this.parent=parent;}
 private static class_2561 text(String key,Object...args){return class_2561.method_43469("evatex."+key,args);}
 private static class_2561 onOff(boolean value){return class_2561.method_43471(value?"options.on":"options.off");}
 private static class_2561 catalog(String key){return class_2561.method_43471("evatex.catalog."+key);}
 @Override protected void method_25426(){
  if(EvatexRecovery.suspended())return;
  try {
  if(config==null)config=VideoMenuConfig.load();cards.clear();
  margin=Math.max(10,Math.min(20,field_22789/40));side=field_22789<600?76:100;
  contentX=margin+side+14;contentW=field_22789-contentX-margin;columns=contentW>=440?2:1;cardW=(contentW-(columns-1)*12)/columns;
  top=field_22790<300?108:119;cardH=field_22790<300?32:42;rows=Math.max(1,Math.min(3,(field_22790-top-55)/(cardH+10)));
  settingsPage=Math.max(0,Math.min(settingsPage,settingGroups().size()-1));
  int navW=Math.min(100,(field_22789-2*margin-16)/3);
  add(margin,44,navW,24,catalog("local"),b->openLibrary(true),false);
  add(margin+navW+8,44,navW,24,catalog("discover"),b->openLibrary(false),false);
  add(margin+2*(navW+8),44,navW,24,catalog("settings"),b->{},true);
  for(int i=0;i<3;i++){final int section=i;add(margin,82+i*29,side,22,text("category."+CATEGORY_KEYS[i]),b->{settingsPage=pageForSetting(CATEGORIES[section][0]);rebuild();},categoryKey().equals(CATEGORY_KEYS[i]));}
  int[] group=settingGroups().get(settingsPage);
  for(int n=0;n<group.length;n++){
   final int setting=group[n];int x=contentX+n%columns*(cardW+12),y=top+n/columns*(cardH+10);cards.add(new Row(setting,x,y,cardW));
   int chipW=Math.max(56,Math.min(94,cardW/3)),cy=y+(cardH-24)/2;
   if(setting==2){
    int sliderW=Math.max(80,cardW/2);method_37063(new AstralVolumeSlider(x+cardW-sliderW-8,cy,sliderW,24,config.audioVolume,value->{config.audioVolume=value;volumeDirty=true;volumeChangedAt=System.nanoTime();VideoBackground.INSTANCE.reload(config);}));
   }else{
    String label=settingLabel(setting).getString();int colon=label.lastIndexOf(":");String value=colon>=0?label.substring(colon+1).strip():text(setting==17?"engine.open":diagnosticCopied?"diagnostics_copied":"settings_copy").getString();
    add(x+cardW-chipW-8,cy,chipW,24,settingLabel(setting),b->{if(setting==14){field_22787.field_1774.method_1455(PlaybackDiagnostics.report(config));diagnosticCopied=true;rebuild();}else if(setting==17){field_22787.method_1507(new VideoEngineScreen(this));}else{changeSetting(setting);apply();}},false).caption(value);
   }
  }
  int footer=field_22790-36;add(margin,footer,side,20,class_2561.method_43471("gui.back"),b->method_25419(),false);
  if(settingGroups().size()>3){
   var previous=add(contentX,footer,26,20,class_2561.method_43470("‹"),b->{settingsPage--;rebuild();},false);previous.field_22763=settingsPage>0;
   var next=add(contentX+contentW-26,footer,26,20,class_2561.method_43470("›"),b->{settingsPage++;rebuild();},false);next.field_22763=settingsPage<settingGroups().size()-1;
  }

  }catch(RuntimeException|LinkageError failure){EvatexRecovery.report("VideoSelectScreen",failure);}
 }
 private void openLibrary(boolean local){if(volumeDirty){config.save();volumeDirty=false;}if(parent instanceof BackgroundLibraryScreen library){field_22787.method_1507(library);library.openTab(local);}else field_22787.method_1507(new BackgroundLibraryScreen(parent,local));}
 private List<int[]> settingGroups(){var groups=new ArrayList<int[]>();int capacity=Math.max(1,Math.min(6,columns*rows));for(var section:CATEGORIES)for(int start=0;start<section.length;start+=capacity)groups.add(Arrays.copyOfRange(section,start,Math.min(section.length,start+capacity)));return groups;}
 private int pageForSetting(int target){var groups=settingGroups();for(int i=0;i<groups.size();i++)for(int setting:groups.get(i))if(setting==target)return i;return 0;}
 private String categoryKey(){int first=settingGroups().get(settingsPage)[0];return CATEGORY_KEYS[first<6||first==17?0:first<10?1:2];}
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
            case 16 -> text("discord_activity",onOff(config.discordPresence));
            case 17 -> text("engine.setting");
            case 15 -> text("ingame_background",onOff(config.inGameMenuBackground));
            case 14 -> text(diagnosticCopied ? "diagnostics_copied" : "diagnostics_copy");
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
            case 16 -> config.discordPresence=!config.discordPresence;
            case 15 -> config.inGameMenuBackground=!config.inGameMenuBackground;
        }
    }

 private AstralButton add(int x,int y,int w,int h,class_2561 label,class_4185.class_4241 action,boolean selected){return method_37063(new AstralButton(x,y,w,h,label,action,selected).astralSurface(false));}
 private void rebuild(){int focus=method_25396().indexOf(method_25399());method_37067();method_25426();if(focus>=0&&focus<method_25396().size())method_48265(method_25396().get(focus));}
 private void apply(){config.save();EvatexDiscord.configure(config);AstralTheme.animations(config.themeTransitions);VideoBackground.INSTANCE.reload(config);rebuild();}
 @Override public void method_25419(){
  if(EvatexRecovery.suspended())return;
  try {if(volumeDirty)config.save();field_22787.method_1507(parent);
  }catch(RuntimeException|LinkageError failure){EvatexRecovery.report("VideoSelectScreen",failure);}
 }
 @Override public void method_25420(class_332 context,int mx,int my,float delta){}
 @Override public boolean method_25421(){return parent!=null && parent.method_25421();}
 @Override public boolean method_25422(){return false;}
 @Override public void method_25394(class_332 ctx,int mx,int my,float delta){
  if(EvatexRecovery.suspended())return;
  try {
  if(volumeDirty&&System.nanoTime()-volumeChangedAt>250_000_000L){config.save();volumeDirty=false;}
  ctx.method_25296(0,0,field_22789,field_22790,0xFF08090B,0xFF111215);
  AstralTypography.brand(ctx,margin,14,0xFFF2F2F2);AstralDraw.displayText(ctx,"EVATEX",margin+25,12,1.55f,0xFFF2F2F2);
  if(field_22789>400)AstralTypography.draw(ctx,AstralTypography.fit(catalog("subtitle").getString(),field_22789-margin-130),margin+110,18,0xFF999C9D);
  AstralDraw.rounded(ctx,margin-3,76,side+6,field_22790-123,0xFF101114);
  AstralTypography.draw(ctx,text("category."+categoryKey()).getString(),contentX,82,0xFFF2F2F2);
  if(field_22790>=300)AstralTypography.draw(ctx,AstralTypography.fit(text("settings_description."+categoryKey()).getString(),contentW),contentX,99,0xFF999C9D);
  for(var row:cards){
   AstralDraw.rounded(ctx,row.x(),row.y(),row.width(),cardH,0xFF17191C);
   String label=settingLabel(row.setting()).getString();int colon=label.lastIndexOf(":");if(colon>=0)label=label.substring(0,colon);
   int available=row.setting()==2?row.width()/2-20:row.width()-Math.max(56,Math.min(94,row.width()/3))-28;
   AstralTypography.draw(ctx,AstralTypography.fit(label,available),row.x()+12,row.y()+(cardH-9)/2,0xFFE8E8E8);
  }
  super.method_25394(ctx,mx,my,delta);
  if(settingGroups().size()>3)AstralTypography.centered(ctx,(settingsPage+1)+" / "+settingGroups().size(),contentX+contentW/2,field_22790-29,0xFF999C9D);
  AstralTypography.centered(ctx,AstralTypography.fit("EVATEX · "+EvatexPlatform.modVersion("evatexbackgroundvideo"),field_22789-24),field_22789/2,field_22790-11,0xFF93999B);

  }catch(RuntimeException|LinkageError failure){EvatexRecovery.report("VideoSelectScreen",failure);}
 }
    private static int cycle(int[] values,int current) {for(int value:values)if(value>current)return value;return values[0];}
    private static double cycle(double[] values,double current) {for(double value:values)if(value>current+0.001)return value;return values[0];}
}
