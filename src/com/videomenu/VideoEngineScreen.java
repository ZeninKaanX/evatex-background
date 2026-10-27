package com.videomenu;

import com.videomenu.video.ExternalVideoEngine;
import com.videomenu.video.VideoBackground;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import java.util.concurrent.CompletableFuture;

/** Explicit, optional engine download; no network operations on the game thread. */
public final class VideoEngineScreen extends class_437 {
    private final class_437 parent;
    private CompletableFuture<java.nio.file.Path> install;
    private boolean completed;
    private AstralButton download;
    public VideoEngineScreen(class_437 parent){super(text("title"));this.parent=parent;}
    private static class_2561 text(String key,Object... args){return class_2561.method_43469("evatex.engine."+key,args);}
    @Override protected void method_25426(){
        int w=Math.min(170,(field_22789-40)/2),y=field_22790-40;
        download=method_37063(new AstralButton(field_22789/2-w-5,y,w,22,text("download"),b->{
            install=ExternalVideoEngine.install(EvatexPlatform.gameDir());completed=false;download.field_22763=false;
            install.whenComplete((path,error)->net.minecraft.class_310.method_1551().execute(()->{
                if(error==null){VideoBackground.INSTANCE.reload(VideoMenuConfig.load());VideoBackground.INSTANCE.retryPlayback();}
                else VideoMenuClient.LOGGER.warn("EVATEX video engine setup failed",error);
            }));
        },false));
        method_37063(new AstralButton(field_22789/2+5,y,w,22,class_2561.method_43471("gui.back"),b->method_25419(),false));
        download.field_22763=install==null||install.isDone();
    }
    @Override public void method_25393(){
        if(install!=null&&install.isDone()&&!completed){completed=true;download.field_22763=true;
        }
    }
    @Override public void method_25394(class_332 ctx,int mx,int my,float delta){
        ctx.method_25296(0,0,field_22789,field_22790,0xFF08090B,0xFF111215);
        AstralTypography.centered(ctx,text("title").getString(),field_22789/2,24,0xFFF2F2F2);
        String[] lines={"body","consent","source","manual"};
        for(int i=0;i<lines.length;i++)AstralTypography.centered(ctx,AstralTypography.fit(text(lines[i]).getString(),field_22789-30),field_22789/2,48+i*16,0xFFADB2B7);
        String state=install!=null&&!install.isDone()?"downloading":install!=null&&install.isCompletedExceptionally()?"failed":completed?"ready":"idle";
        String value=text(state).getString();if(state.equals("downloading"))value+=" · "+(ExternalVideoEngine.downloaded/1048576)+" MiB";
        AstralTypography.centered(ctx,AstralTypography.fit(value,field_22789-30),field_22789/2,field_22790-69,0xFFE8E8E8);
        super.method_25394(ctx,mx,my,delta);
    }
    @Override public void method_25419(){field_22787.method_1507(parent);}
    @Override public boolean method_25421(){return parent!=null&&parent.method_25421();}
}
