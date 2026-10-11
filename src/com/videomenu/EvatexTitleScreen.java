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
        try {if(PRESS.getParameterCount()==0) PRESS.invoke(original);else PRESS.invoke(original,new Object[]{null});}
        catch(ReflectiveOperationException e) {VideoMenuClient.LOGGER.error("Cannot invoke Minecraft title action",e);}
    }
    public EvatexTitleScreen() {super(false);}
    @Override protected void method_25426() {
        super.method_25426();
        settings=VideoMenuConfig.load();
        List<class_4185> original=new ArrayList<>();
        net.minecraft.class_7077 creditsSource=null;
        for(var child:method_25396()) {
            if(child instanceof net.minecraft.class_7077 footer && (footer.method_25369().getString().equals("© Mojang Studios") || footer.method_25369().getString().equals(class_2561.method_43471("title.credits").getString()))) creditsSource=footer;
            else if(child instanceof class_4185 button) original.add(button);
        }
        method_37067();
        int margin=Math.max(8,Math.min(16,field_22789/20));
        boolean wide=field_22789>=360 && field_22790>=220;
        int group=Math.min(340,field_22789-margin*2);
        if(wide) group=Math.min(group,(int)(field_22789*.70));
        int tileHeight=wide ? 44 : field_22790>=220 ? 30 : field_22790>=180 ? 22 : 18;
        int y=wide ? (int)(field_22790*.47) : field_22790>=180 ? (int)(field_22790*.35) : 44;
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
        int third=(group-12)/3;
        class_4185 options=find(original,"menu.options"),quit=find(original,"menu.quit");
        addSmall(margin,secondaryY,third,smallHeight,class_2561.method_43471("menu.options"),options);
        method_37063(new AstralButton(margin+third+6,secondaryY,third,smallHeight,class_2561.method_43471("evatex.title"),
            button -> class_310.method_1551().method_1507(new VideoSelectScreen(this,0)),false));
        addSmall(margin+(third+6)*2,secondaryY,group-(third+6)*2,smallHeight,class_2561.method_43471("menu.quit"),quit);
        original.remove(options);original.remove(quit);
        hasCredits=creditsSource!=null;
        if(creditsSource!=null) {
            var source=creditsSource;var font=class_310.method_1551().field_1772;
            var label=class_2561.method_43470("© Mojang Studios");int textWidth=font.method_1727(label.getString());
            var credits=method_37063(new net.minecraft.class_7077(Math.max(margin,field_22789-margin-textWidth),field_22790-10,textWidth,10,label,button->press(source),font));
            credits.method_47400(net.minecraft.class_7919.method_47407(class_2561.method_43471("title.credits")));
        }
        extraSources=new ArrayList<>(original);footerMargin=margin;footerStart=secondaryY+smallHeight;
        footerWidgets.clear();buildFooter();
        ownWidgets.clear();ownWidgets.addAll(method_25396());
    }
    /** Native/mod buttons added after screen initialization join our paged footer. */
    public void captureLateButtons() {
        boolean changed=false;
        for(var child:new ArrayList<>(method_25396())) if(!ownWidgets.contains(child) && child instanceof class_4185 button) {
            extraSources.add(button);method_37066(button);changed=true;
        }
        if(changed) {
            for(var button:footerWidgets)method_37066(button);
            footerWidgets.clear();buildFooter();ownWidgets.clear();ownWidgets.addAll(method_25396());
        }
    }
    private void buildFooter() {
        int margin=footerMargin,footerY=field_22790-37;
        if(footerY>=footerStart+5 && !extraSources.isEmpty()) {
            int pages=(extraSources.size()+2)/3;footerPage=Math.floorMod(footerPage,pages);
            int available=field_22789-margin*2-(pages>1 ? 26 : 0);
            int count=Math.min(3,extraSources.size()-footerPage*3),width=(available-(count-1)*6)/count;
            for(int i=0;i<count;i++) {
                var source=extraSources.get(footerPage*3+i);
                var label=source.method_25369();
                if(label.getString().equals(class_2561.method_43471("narrator.button.language").getString())) label=class_2561.method_43471("options.language");
                if(label.getString().equals(class_2561.method_43471("narrator.button.accessibility").getString())) label=class_2561.method_43471("options.accessibility.title");
                int before=method_25396().size();
                addSmall(margin+i*(width+6),footerY,width,18,label,source);
                footerWidgets.add((class_4185)method_25396().get(before));
            }
            if(pages>1)footerWidgets.add(method_37063(new AstralButton(field_22789-margin-20,footerY,20,18,class_2561.method_43470("›"),button->{
                footerPage++;for(var footer:footerWidgets)method_37066(footer);footerWidgets.clear();buildFooter();ownWidgets.clear();ownWidgets.addAll(method_25396());
            },false)));
        }
    }
    private static class_4185 find(List<class_4185> original,String key) {
        String label=class_2561.method_43471(key).getString();
        for(var button:original) if(button.method_25369().getString().equals(label)) return button;
        return null;
    }
    private void addTile(int x,int y,int w,int h,class_4185 source,String key) {
        var tile=method_37063(new AstralHomeButton(x,y,w,h,source==null ? class_2561.method_43471(key) : source.method_25369(),button->{if(source!=null)press(source);}));
        tile.field_22763=source!=null && source.field_22763;
    }
    private void addSmall(int x,int y,int w,int h,class_2561 label,class_4185 source) {
        var button=method_37063(new AstralButton(x,y,w,h,label,pressed->{if(source!=null)press(source);},false));
        button.field_22763=source!=null && source.field_22763;
    }
    @Override public void method_25393() {} // Native notification panels must not paint over this home.
    @Override public void method_25420(class_332 ctx,int mx,int my,float delta) {}
    @Override public void method_25394(class_332 ctx,int mx,int my,float delta) {
        boolean video=VideoBackground.INSTANCE.render(ctx,field_22789,field_22790);
        if(video) AstralBackdrop.renderMenu(ctx,field_22789,field_22790,settings.menuMeteors && settings.themeTransitions);
        else AstralBackdrop.render(ctx,field_22789,field_22790,false,settings.menuMeteors && settings.themeTransitions);
        int margin=Math.max(8,Math.min(16,field_22789/20));
        var font=class_310.method_1551().field_1772;
        boolean wide=field_22789>=360 && field_22790>=220;
        int brandY=wide ? Math.min(60,field_22790/4) : field_22790>=180 ? 22 : 12;
        float brandScale=wide ? 3f : 1.8f;
        AstralDraw.displayText(ctx,"EVATEX",margin,brandY,brandScale,0xFFF4F4F4);
        int sub=brandY+(int)(font.field_2000*brandScale)+10;
        ctx.method_25294(margin,sub+3,margin+22,sub+4,0xFFBBBBBB);
        ctx.method_25303(font,"A S T R A L",margin+30,sub,0xFFBBBBBB);
        int cardWidth=Math.min(122,field_22789/3),cardX=field_22789-margin-cardWidth;
        AstralDraw.rounded(ctx,cardX,10,cardWidth,24,0xA0121212);
        AstralDraw.rounded(ctx,cardX+5,15,14,14,0xFF333333);
        ctx.method_25300(font,"E",cardX+12,18,0xFFF4F4F4);
        String username=class_310.method_1551().method_1548().method_1676();
        ctx.method_25303(font,font.method_27523(username,Math.max(0,cardWidth-31)),cardX+25,18,0xFFDDDDDD);
        for(var child:method_25396()) if(child instanceof net.minecraft.class_4068 drawable) drawable.method_25394(ctx,mx,my,delta);
        ctx.method_25303(font,"EVATEX "+net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("evatexbackgroundvideo").orElseThrow().getMetadata().getVersion().getFriendlyString(),margin,field_22790-10,0xFF888888);
        String credits="© Mojang Studios";
        if(!hasCredits) ctx.method_25303(font,credits,Math.max(margin,field_22789-margin-font.method_1727(credits)),field_22790-10,0xFF888888);
    }
    @Override public boolean method_25422() {return false;}
}
