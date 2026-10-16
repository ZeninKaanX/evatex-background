package com.videomenu;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.*;

/** Presentation only: native screens still own selection, connection, saves, input and narration. */
public final class AstralMenus {
    private static State state;
    private static boolean nativeText;
    private static final class State {
        final class_437 screen;
        boolean chromeOnly;
        final List<class_339> controls=new ArrayList<>();
        final List<class_364> originalChildren=new ArrayList<>();
        final List<Bounds> originalBounds=new ArrayList<>();
        int originalListX,originalListY,originalListWidth,originalListHeight,originalScreenHeight;
        class_350<?> list;
        int margin,top,bottom,listWidth,detailX,detailWidth;
        State(class_437 screen){this.screen=screen;}
    }
    private record Bounds(class_339 widget,int x,int y,int width) {
        void restore(){widget.method_46421(x);widget.method_46419(y);widget.method_25358(width);}
    }
    private AstralMenus() {}
    public static void beforeInit(){restore();nativeText=false;}
    private static void restore(){
        var previous=state;state=null;
        if(previous==null || previous.chromeOnly)return;
        for(var bounds:previous.originalBounds)bounds.restore();
        if(previous.list!=null && previous.originalListWidth>0)resize(previous.list,previous.originalListWidth,previous.originalListHeight,previous.originalListX,previous.originalListY,previous.originalScreenHeight);
    }
    private static boolean listBounds(State s){
        try {
            try {
                s.originalListX=(int)s.list.getClass().getMethod("method_46426").invoke(s.list);
                s.originalListY=(int)s.list.getClass().getMethod("method_46427").invoke(s.list);
                s.originalListWidth=(int)s.list.getClass().getMethod("method_25368").invoke(s.list);
                s.originalListHeight=(int)s.list.getClass().getMethod("method_25364").invoke(s.list);
            } catch(NoSuchMethodException legacy) {
                s.originalListX=legacyField(s.list,"field_19088");s.originalListY=legacyField(s.list,"field_19085");
                s.originalListWidth=legacyField(s.list,"field_22742");s.originalListHeight=legacyField(s.list,"field_19086")-s.originalListY;
            }
            return true;
        } catch(ReflectiveOperationException|RuntimeException error){return false;}
    }
    private static int legacyField(Object list,String name)throws ReflectiveOperationException {
        var field=class_350.class.getDeclaredField(name);field.setAccessible(true);return field.getInt(list);
    }
    private static void checkOwner(class_437 screen){
        if(!active(screen))return;
        for(var child:screen.method_25396())if(!state.originalChildren.contains(child)){restore();return;}
    }
    public static void prepare(class_437 screen) {
        if(EvatexRecovery.suspended())return;
        state=null;
        var client=class_310.method_1551();
        if(client.field_1687!=null || !VideoMenuConfig.load().customMainMenu) return;
        boolean primary=screen.getClass()==class_500.class || screen.getClass()==class_526.class || screen.getClass()==class_429.class;
        boolean secondary=screen.getClass().getName().startsWith("net.minecraft.") && (screen instanceof class_4667 || screen.getClass()==class_5375.class || screen.getClass()==class_8219.class || screen.getClass()==class_5235.class || screen.getClass()==class_524.class || screen.getClass()==class_525.class);
        if(!primary && !secondary)return;
        if(screen.field_22789<480 || screen.field_22790<270) return; // Native compact layout remains usable.
        var next=new State(screen);next.chromeOnly=!primary;
        for(var child:screen.method_25396()) {
            // Leave layouts owned by another mod untouched.
            if(!child.getClass().getName().startsWith("net.minecraft."))return;
            next.originalChildren.add(child);
            if(child instanceof class_339 widget)next.originalBounds.add(new Bounds(widget,widget.method_46426(),widget.method_46427(),widget.method_25368()));
            if(child instanceof class_350<?> list)next.list=list;
            else if(child instanceof class_339 widget && !(child instanceof class_7842))next.controls.add(widget);
        }
        if(next.chromeOnly){state=next;next.margin=Math.max(14,(int)(screen.field_22789*.03));return;}
        if(screen.getClass()!=class_429.class && next.list==null)return;
        int w=screen.field_22789,h=screen.field_22790;
        next.originalScreenHeight=h;
        if(next.list!=null && !listBounds(next))return;
        next.margin=Math.max(14,(int)(w*.03));next.top=82;next.bottom=h-72;
        if(screen instanceof class_500){next.top=92;next.bottom=h-53;}
        next.listWidth=(int)(w*.58);next.detailX=next.margin+next.listWidth+16;
        next.detailWidth=w-next.margin-next.detailX;
        state=next;
        if(next.list!=null && !resize(next.list,next.listWidth,next.bottom-next.top,next.margin,next.top,h)){state=null;return;}
        var buttons=new ArrayList<class_339>();
        for(var control:next.controls) {
            if(control instanceof class_342 search) {
                search.method_46421(next.margin+8);search.method_46419(59);search.method_25358(next.listWidth-16);
            } else buttons.add(control);
        }
        if(screen instanceof class_429) {
            var settings=new ArrayList<class_339>();var exits=new ArrayList<class_339>();
            for(var button:buttons) {
                String label=button.method_25369().getString();
                if(label.equals(text("gui.done")) || label.equals(text("gui.back")))exits.add(button);else settings.add(button);
            }
            int content=Math.min(560,w-48);next.margin=(w-content)/2;
            int cell=(content-12)/2,rows=(settings.size()+1)/2;
            int step=Math.min(32,Math.max(22,(h-126)/Math.max(1,rows)));
            next.bottom=76+rows*step;
            for(int i=0;i<settings.size();i++)place(settings.get(i),next.margin+(i%2)*(cell+12),76+(i/2)*step,cell,20);
            int exitWidth=Math.min(220,(content-6*Math.max(0,exits.size()-1))/Math.max(1,exits.size()));
            int exitStart=(w-(exitWidth*exits.size()+6*Math.max(0,exits.size()-1)))/2;
            for(int i=0;i<exits.size();i++)place(exits.get(i),exitStart+i*(exitWidth+6),next.bottom+12,exitWidth,20);
        } else if(screen instanceof class_500) {
            var topButtons=new ArrayList<class_339>();var footerButtons=new ArrayList<class_339>();
            for(var button:buttons) {
                String label=button.method_25369().getString();
                if(label.equals(text("selectServer.select"))||label.equals(text("selectServer.edit"))||label.equals(text("selectServer.delete")))topButtons.add(button);
                else footerButtons.add(button);
            }
            int cell=(next.listWidth-12)/Math.max(1,topButtons.size());
            for(int i=0;i<topButtons.size();i++)place(topButtons.get(i),next.margin+i*(cell+6),62,cell,20);
            cell=(w-next.margin*2-6*(footerButtons.size()-1))/Math.max(1,footerButtons.size());
            for(int i=0;i<footerButtons.size();i++)place(footerButtons.get(i),next.margin+i*(cell+6),h-39,cell,20);
        } else {
            int columns=4,cell=(w-next.margin*2-18)/columns;
            for(int i=0;i<buttons.size();i++)place(buttons.get(i),next.margin+(i%columns)*(cell+6),h-59+(i/columns)*27,cell,22);
        }
    }
    private static void place(class_339 button,int x,int y,int width,int height) {
        button.method_46421(x);button.method_46419(y);button.method_25358(width);
        // Height is already native 20; retain native slider and click semantics.
    }
    private static boolean resize(class_350<?> list,int width,int height,int x,int y,int screenHeight) {
        try {
            try {
                // Modern entries cache their absolute row positions. Set x before
                // reflow, otherwise the rows remain at x=0 and icons are clipped.
                try {list.getClass().getMethod("method_73369",int.class,int.class,int.class,int.class).invoke(list,width,height,x,y);}
                catch(NoSuchMethodException older) {
                    list.getClass().getMethod("method_57714",int.class,int.class,int.class).invoke(list,width,height,y);
                    list.getClass().getMethod("method_46421",int.class).invoke(list,x);
                }
            } catch(NoSuchMethodException legacy) {
                list.getClass().getMethod("method_25323",int.class,int.class,int.class,int.class).invoke(list,width,screenHeight,y,y+height);
                list.getClass().getMethod("method_25333",int.class).invoke(list,x);
            }
            return true;
        } catch(ReflectiveOperationException|RuntimeException error) {
            VideoMenuClient.LOGGER.warn("Keeping native menu layout: unsupported list geometry",error);return false;
        }
    }
    public static boolean active(class_437 screen){return !EvatexRecovery.suspended() && state!=null && state.screen==screen && class_310.method_1551().field_1687==null;}
    public static boolean ownsList(Object list){return state!=null && !state.chromeOnly && state.list==list && active(class_310.method_1551().field_1755);}
    public static boolean nativeText(){return nativeText && active(class_310.method_1551().field_1755);}
    public static void beginFrame(class_437 screen){
        checkOwner(screen);
        nativeText=active(screen) && state.chromeOnly;
        if(nativeText)AstralTypography.beginNativeText();
    }
    public static void endFrame(){nativeText=false;}
    public static void chrome(class_332 ctx,class_437 screen){
        if(active(screen) && state.chromeOnly) {
            int margin=state.margin;
            panel(ctx,margin-5,31,screen.field_22789-margin*2+10,Math.max(1,screen.field_22790-65));
        }
    }
    public static int rowWidth(){return Math.max(32,state.listWidth-32);}
    public static int scrollbarX(){return state.margin+state.listWidth-6;}
    public static boolean nativeButton(class_339 button) {
        return active(class_310.method_1551().field_1755) && button.getClass().getName().startsWith("net.minecraft.");
    }
    public static boolean render(class_437 screen,class_332 ctx,int mx,int my,float delta,boolean background) {
        if(!active(screen) || state.chromeOnly)return false;
        var s=state;
        if(background)screen.method_25420(ctx);
        // Also works when wallpaper playback is disabled: a quiet, readable surface.
        if(!com.videomenu.video.VideoBackground.INSTANCE.status().equals("playing"))ctx.method_25294(0,0,screen.field_22789,screen.field_22790,0x44000000);
        // Local contrast for bright wallpapers; never dim the whole screen for a heading.
        String title=screen.method_25440().getString();
        int headingWidth=Math.min(screen.field_22789-s.margin*2,Math.max(180,(int)Math.ceil(AstralTypography.width(title)*2.1)+24));
        ctx.method_25294(s.margin-6,10,s.margin+headingWidth,58,0xA01A1A1A);
        AstralDraw.displayText(ctx,"EVATEX / ASTRAL",s.margin,14,1f,0xFFC2C2C2);
        AstralDraw.displayText(ctx,screen.method_25440().getString(),s.margin,32,2.1f,0xFFF3F3F3);
        if(s.list!=null) {
            panel(ctx,s.margin-3,s.top-3,s.listWidth+6,s.bottom-s.top+6);
            panel(ctx,s.detailX,s.top-3,s.detailWidth,s.bottom-s.top+6);
            details(ctx,s);
        } else {
            int end=screen instanceof class_429 ? s.bottom : screen.field_22790-20;
            ctx.method_25294(s.margin-6,68,screen.field_22789-s.margin+6,end,0x88101010);
            ctx.method_25294(s.margin-6,68,screen.field_22789-s.margin+6,69,0x44FFFFFF);
        }
        AstralTypography.beginNativeText();nativeText=true;
        try {for(var child:screen.method_25396())if(child instanceof class_4068 drawable && !(child instanceof class_7842 label && label.method_25369().getString().equals(screen.method_25440().getString())))drawable.method_25394(ctx,mx,my,delta);}
        finally {nativeText=false;}
        AstralDraw.displayText(ctx,"EVATEX",s.margin,screen.field_22790-12,1f,0xFFBFC3C9);
        return true;
    }
    public static void panel(class_332 ctx,int x,int y,int width,int height) {
        ctx.method_25294(x,y,x+width,y+height,0xBB101010);
        ctx.method_25294(x,y,x+width,y+1,0x44FFFFFF);
        ctx.method_25294(x,y+height-1,x+width,y+height,0x22FFFFFF);
    }
    private static String text(String key){return class_2561.method_43471(key).getString();}
    private static void line(class_332 ctx,String text,int x,int y,int width,float scale,int color) {
        String fit=text;
        while(fit.length()>1 && AstralTypography.width(fit)*scale>width)fit=fit.substring(0,fit.offsetByCodePoints(fit.length(),-1));
        if(!fit.equals(text) && fit.length()>3)fit=fit.substring(0,fit.length()-3)+"...";
        AstralDraw.displayText(ctx,fit,x,y,scale,color);
    }
    private static void details(class_332 ctx,State s) {
        int x=s.detailX+12,y=s.top+14,width=s.detailWidth-24;
        Object entry=s.list.method_25334();
        line(ctx,text(s.screen instanceof class_500 ? "evatex.menu.server_details" : "evatex.menu.world_details"),x,y,width,1f,0xFFBFC3C9);
        if(entry instanceof class_4267.class_4270 server) {
            var info=server.method_20133();
            line(ctx,info.field_3752,x,y+24,width,1.3f,0xFFFFFFFF);
            line(ctx,info.field_3761,x,y+43,width,1f,0xFFAAAAAA);
            ctx.method_25294(x,y+66,x+width,y+67,0x33FFFFFF);
            line(ctx,text("evatex.menu.players"),x,y+82,width,1f,0xFFBFC3C9);
            String players=info.field_3753==null ? "" : info.field_3753.getString();
            boolean unavailable=players.isEmpty() || players.equals(text("multiplayer.status.cannot_connect")) || players.equals(text("multiplayer.status.cannot_resolve")) || players.equals(text("multiplayer.status.pinging"));
            line(ctx,unavailable ? "—" : players,x,y+99,width,1f,0xFFF0F0F0);
            line(ctx,text("evatex.menu.latency"),x,y+122,width,1f,0xFFBFC3C9);
            line(ctx,unavailable || info.field_3758<0 ? "—" : info.field_3758+" ms",x,y+139,width,1f,0xFFF0F0F0);
            if(s.bottom-y>185)line(ctx,info.field_3757==null ? "" : info.field_3757.getString(),x,y+172,width,1f,0xFFCCCCCC);
        } else if(entry!=null && s.screen instanceof class_526) {
            try {
                class_34 info;
                try {info=(class_34)entry.getClass().getMethod("method_73463").invoke(entry);}
                catch(NoSuchMethodException old) {
                    var field=entry.getClass().getDeclaredField("field_19138");field.setAccessible(true);info=(class_34)field.get(entry);
                }
                line(ctx,info.method_252(),x,y+24,width,1.3f,0xFFFFFFFF);
                line(ctx,info.method_248(),x,y+44,width,1f,0xFFAAAAAA);
                ctx.method_25294(x,y+66,x+width,y+67,0x33FFFFFF);
                line(ctx,info.method_27429().getString(),x,y+86,width,1f,0xFFCCCCCC);
            } catch(ReflectiveOperationException ignored) {line(ctx,text("evatex.menu.world_selected"),x,y+34,width,1f,0xFFEAEAEA);}
        } else {
            line(ctx,text(s.screen instanceof class_500 ? "evatex.menu.select_server" : "evatex.menu.select_world"),x,y+35,width,1f,0xFFDDDDDD);
        }
    }
    public static void button(class_332 ctx,class_339 widget,int mx,int my) {
        int x=widget.method_46426(),y=widget.method_46427(),w=widget.method_25368(),h=widget.method_25364();
        boolean focus=widget.method_25405(mx,my)||widget.method_25370();
        ctx.method_25294(x,y,x+w,y+h,focus ? 0xC83C3C3C : 0xA01A1A1A);
        ctx.method_25294(x,y+h-1,x+w,y+h,focus ? 0xCCFFFFFF : 0x44FFFFFF);
        String label=widget.method_25369().getString();
        float scale=1f;
        while(label.length()>1 && AstralTypography.width(label)*scale>w-16)label=label.substring(0,label.offsetByCodePoints(label.length(),-1));
        AstralDraw.displayText(ctx,label,x+8,y+(h-8)/2,scale,widget.field_22763 ? 0xFFF2F2F2 : 0xFF777777);
    }
}
