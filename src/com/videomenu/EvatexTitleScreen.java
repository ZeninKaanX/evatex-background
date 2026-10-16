package com.videomenu;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_442;
import com.videomenu.video.VideoBackground;

/** Optional standalone home. All game actions come from Minecraft's own title buttons. */
public final class EvatexTitleScreen extends class_442 {
    private VideoMenuConfig settings;
    private int footerPage;
    private boolean moreOpen;
    private final List<class_4185> primaryWidgets=new ArrayList<>();
    private final java.util.Map<class_4185,class_4185> nativeSources=new java.util.IdentityHashMap<>();
    private List<class_4185> visibleExtras=List.of();
    private boolean hasCredits;
    private List<class_4185> extraSources=List.of();
    private final List<class_4185> footerWidgets=new ArrayList<>();
    private final java.util.Set<Object> ownWidgets=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
    private int footerMargin,footerStart;
    private static final Method PRESS=findPress();
    private static Method findPress() {
        for(Method method:class_4185.class.getMethods())
            if(method.getName().equals("method_25306") && method.getParameterCount()<=1) return method;
        throw new IllegalStateException("Minecraft button action API unavailable");
    }
    private static void press(class_4185 original) {
        if(!original.field_22763 || !original.field_22764)return;
        try {if(PRESS.getParameterCount()==0) PRESS.invoke(original);else PRESS.invoke(original,new Object[]{null});}
        catch(ReflectiveOperationException e) {VideoMenuClient.LOGGER.error("Cannot invoke Minecraft title action",e);}
    }
    public EvatexTitleScreen() {super(false);}
    @Override protected void method_25426() {
  if(EvatexRecovery.suspended())return;
  try {
        super.method_25426();
        settings=VideoMenuConfig.load();
        List<class_4185> original=new ArrayList<>();
        net.minecraft.class_7077 creditsSource=null;
        for(var child:method_25396()) {
            if(child instanceof net.minecraft.class_7077 footer && (footer.method_25369().getString().equals("© Mojang Studios") || footer.method_25369().getString().equals(class_2561.method_43471("title.credits").getString()))) creditsSource=footer;
            else if(child instanceof class_4185 button) original.add(button);
        }
        method_37067();
        primaryWidgets.clear();nativeSources.clear();visibleExtras=List.of();
        int margin=Math.max(10,(int)(field_22789*.028));
        boolean wide=field_22789>=360 && field_22790>=220;
        int group=wide ? (int)(field_22789*.492) : field_22789-margin*2;
        int tileHeight=wide ? Math.max(30,(int)(field_22790*.12)) : field_22790>=220 ? 30 : field_22790>=180 ? 22 : 18;
        int y=wide ? (int)(field_22790*.417) : field_22790>=180 ? (int)(field_22790*.35) : 44;
        int tileWidth=wide ? (group-8)/2 : group;
        class_4185 single=find(original,"menu.singleplayer");
        if(single==null) single=find(original,"menu.playdemo");
        if(single==null && !original.isEmpty()) single=original.get(0);
        class_4185 multiplayer=find(original,"menu.multiplayer");
        addTile(margin,y,tileWidth,tileHeight,single,"menu.singleplayer");
        addTile(wide ? margin+tileWidth+8 : margin,wide ? y : y+tileHeight+6,tileWidth,tileHeight,multiplayer,"menu.multiplayer");
        original.remove(single);original.remove(multiplayer);
        int secondaryY=y+(wide ? tileHeight : tileHeight*2+6)+8;
        int smallHeight=field_22790>=220 ? 24 : 18;
        int third=wide ? Math.min((group-12)/3,Math.max(70,(int)(field_22789*.074))) : (group-12)/3;
        class_4185 options=find(original,"menu.options"),quit=find(original,"menu.quit");
        method_37063(new AstralHomeButton(margin,secondaryY,third,smallHeight,class_2561.method_43471("evatex.title"),
            button -> class_310.method_1551().method_1507(new BackgroundLibraryScreen(this,true))));
        addSmall(margin+third+6,secondaryY,third,smallHeight,class_2561.method_43471("menu.options"),options);
        addSmall(margin+(third+6)*2,secondaryY,third,smallHeight,class_2561.method_43471("menu.quit"),quit);
        original.remove(options);original.remove(quit);
        for(var child:method_25396()) if(child instanceof class_4185 button) primaryWidgets.add(button);
        hasCredits=creditsSource!=null;
        if(creditsSource!=null) {
            var source=creditsSource;var font=class_310.method_1551().field_1772;
            var label=AstralTypography.text("© Mojang Studios");int textWidth=font.method_27525(label);
            var credits=method_37063(new net.minecraft.class_7077(Math.max(margin,field_22789-margin-textWidth),field_22790-10,textWidth,10,label,button->press(source),font));
            credits.method_47400(net.minecraft.class_7919.method_47407(class_2561.method_43471("title.credits")));
        }
        extraSources=new ArrayList<>(original);footerMargin=margin;footerStart=secondaryY+smallHeight;
        footerWidgets.clear();buildFooter();
        ownWidgets.clear();ownWidgets.addAll(method_25396());

  }catch(RuntimeException|LinkageError failure){EvatexRecovery.report("EvatexTitleScreen",failure);}
 }
    /** Native/mod buttons added after screen initialization join the secondary menu. */
    public void captureLateButtons() {
        boolean changed=false;
        for(var child:new ArrayList<>(method_25396())) if(!ownWidgets.contains(child) && child instanceof class_4185 button) {
            if(extraSources.stream().noneMatch(existing->existing==button)){extraSources.add(button);changed=true;}
            method_37066(button);
        }
        var visible=extraSources.stream().filter(button->button.field_22764).toList();
        if(!visible.equals(visibleExtras)){visibleExtras=visible;changed=true;}
        if(changed)rebuildSecondary();
        for(var entry:nativeSources.entrySet()) {
            var wrapper=entry.getKey();var source=entry.getValue();
            wrapper.field_22763=source.field_22763 && source.field_22764;
            wrapper.field_22764=source.field_22764 && (!primaryWidgets.contains(wrapper) || !moreOpen);
        }
    }
    private void rebuildSecondary() {
        method_25395(null);
        for(var button:footerWidgets){method_37066(button);nativeSources.remove(button);}
        footerWidgets.clear();buildFooter();ownWidgets.clear();ownWidgets.addAll(method_25396());
    }
    private void buildFooter() {
        for(var button:primaryWidgets)button.field_22764=!moreOpen;
        var availableSources=extraSources.stream().filter(button->button.field_22764).toList();
        if(availableSources.isEmpty()){moreOpen=false;for(var button:primaryWidgets)button.field_22764=true;return;}
        int margin=footerMargin,footerY=field_22790-Math.max(24,(int)(field_22790*.10));
        if(footerY<footerStart+5 && !moreOpen)footerY=Math.min(field_22790-30,footerStart+5);
        footerWidgets.add(method_37063(new AstralHomeButton(margin,footerY,86,18,
            class_2561.method_43471(moreOpen ? "gui.back" : "evatex.home.more"),button->{
                moreOpen=!moreOpen;footerPage=0;rebuildSecondary();
            })));
        if(!moreOpen)return;
        // Secondary actions replace the play controls rather than covering them.
        int y=field_22790>=220 ? Math.max(104,(int)(field_22790*.417)) : 64;
        int rows=Math.max(1,Math.min(4,(footerY-y-6)/24));
        int pages=(availableSources.size()+rows-1)/rows;footerPage=Math.floorMod(footerPage,pages);
        int actionWidth=Math.min(220,field_22789-margin*2);
        int count=Math.min(rows,availableSources.size()-footerPage*rows);
        for(int i=0;i<count;i++) {
            var source=availableSources.get(footerPage*rows+i);
            var label=source.method_25369();
            if(label.getString().equals(class_2561.method_43471("narrator.button.language").getString()))label=class_2561.method_43471("options.language");
            if(label.getString().equals(class_2561.method_43471("narrator.button.accessibility").getString()))label=class_2561.method_43471("options.accessibility.title");
            int before=method_25396().size();
            addSmall(margin,y+i*24,actionWidth,18,label,source);
            footerWidgets.add((class_4185)method_25396().get(before));
        }
        if(pages>1)footerWidgets.add(method_37063(new AstralHomeButton(margin+92,footerY,24,18,
            class_2561.method_43470("›"),button->{footerPage++;rebuildSecondary();})));
    }
    private static class_4185 find(List<class_4185> original,String key) {
        String label=class_2561.method_43471(key).getString();
        for(var button:original) if(button.method_25369().getString().equals(label)) return button;
        return null;
    }
    private void addTile(int x,int y,int w,int h,class_4185 source,String key) {
        var tile=method_37063(new AstralHomeButton(x,y,w,h,source==null ? class_2561.method_43471(key) : source.method_25369(),button->{if(source!=null)press(source);}));
        tile.field_22763=source!=null && source.field_22763;
        if(source!=null)nativeSources.put(tile,source);
    }
    private void addSmall(int x,int y,int w,int h,class_2561 label,class_4185 source) {
        var button=method_37063(new AstralHomeButton(x,y,w,h,label,pressed->{if(source!=null)press(source);}));
        button.field_22763=source!=null && source.field_22763;
        if(source!=null)nativeSources.put(button,source);
    }
    @Override public void method_25393() {
  if(EvatexRecovery.suspended())return;
  try {
  }catch(RuntimeException|LinkageError failure){EvatexRecovery.report("EvatexTitleScreen",failure);}
 } // Native notification panels must not paint over this home.
    @Override public void method_25420(class_332 ctx,int mx,int my,float delta) {}
    @Override public void method_25394(class_332 ctx,int mx,int my,float delta) {
  if(EvatexRecovery.suspended())return;
  try {
        boolean video=VideoBackground.INSTANCE.render(ctx,field_22789,field_22790);
        // The wallpaper renderer already applies the user's dimming and vignette.
        AstralBackdrop.render(ctx,field_22789,field_22790,video,settings.menuMeteors && settings.themeTransitions);
        int margin=Math.max(10,(int)(field_22789*.028));
        var font=class_310.method_1551().field_1772;
        boolean wide=field_22789>=360 && field_22790>=220;
        int brandY=wide ? (int)(field_22790*.24) : field_22790>=180 ? 22 : 12;
        float brandScale=wide ? field_22790/100f : 1.8f;
        if(!AstralWordmark.draw(ctx,margin,brandY,Math.round(font.field_2000*brandScale*.84f)))AstralDraw.wallpaperText(ctx,"EVATEX",margin,brandY,brandScale,0xFFF4F4F4);
        int sub=brandY+(int)(font.field_2000*brandScale)+10;
        ctx.method_25294(margin,sub+3,margin+14,sub+4,0xFFBBBBBB);
        AstralDraw.wallpaperText(ctx,"A S T R A L",margin+20,sub,1f,0xFFDADDE2);
        int cardWidth=Math.min(80,field_22789/3),cardX=field_22789-margin-cardWidth;
        AstralDraw.rounded(ctx,cardX,Math.max(10,(int)(field_22790*.052)),cardWidth,20,0xA0121212);
        int cardY=Math.max(10,(int)(field_22790*.052));
        AstralDraw.rounded(ctx,cardX+5,cardY+3,14,14,0xFF333333);
        AstralTypography.brand(ctx,cardX+5,cardY+3,0xFFF4F4F4);
        String username=class_310.method_1551().method_1548().method_1676();
        AstralDraw.wallpaperText(ctx,AstralTypography.fit(username,cardWidth-27),cardX+22,cardY+6,1f,0xFFDDDDDD);
        for(var child:method_25396()) if(child instanceof net.minecraft.class_4068 drawable && !(child instanceof net.minecraft.class_7077)) drawable.method_25394(ctx,mx,my,delta);
        AstralDraw.wallpaperText(ctx,"EVATEX "+EvatexPlatform.modVersion("evatexbackgroundvideo"),margin,field_22790-10,1f,0xFFE8E8E8);
        AstralTypography.brand(ctx,margin,Math.max(12,(int)(field_22790*.06))-2,0xFFF4F4F4);
        AstralDraw.wallpaperText(ctx,"Minecraft "+EvatexPlatform.modVersion("minecraft"),margin+20,Math.max(12,(int)(field_22790*.06))+2,1f,0xFFE5E7EB);
        String credits="© Mojang Studios";
        AstralDraw.wallpaperText(ctx,credits,Math.max(margin,field_22789-margin-AstralTypography.width(credits)),field_22790-10,1f,0xFFE8E8E8);

  }catch(RuntimeException|LinkageError failure){EvatexRecovery.report("EvatexTitleScreen",failure);}
 }
    @Override public boolean method_25422() {return moreOpen;}
    @Override public void method_25419() {
  if(EvatexRecovery.suspended())return;
  try {
        if(moreOpen){moreOpen=false;footerPage=0;rebuildSecondary();}

  }catch(RuntimeException|LinkageError failure){EvatexRecovery.report("EvatexTitleScreen",failure);}
 }
}
