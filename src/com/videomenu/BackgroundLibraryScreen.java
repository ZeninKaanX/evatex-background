package com.videomenu;
import com.videomenu.library.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_342;
import net.minecraft.class_4185;

/** Curated discovery, with explicit source links and a single verified local download. */
public final class BackgroundLibraryScreen extends class_437 {
 private final class_437 parent;private final VideoMenuConfig config=VideoMenuConfig.load();
 private List<LibraryCatalog.Entry> catalogue=LibraryCatalog.initial(),filtered=List.of();
 private final LibraryThumbnails thumbs=new LibraryThumbnails();private final LibraryDownloads downloads=new LibraryDownloads();
 private String category="all",query="",status="";private LibraryCatalog.Entry selected;private int page;
 private class_342 search;private int margin,left,gridX,gridW,detailX,detailW,top,columns,rows,cardW,cardH,perPage;private boolean compact,closed;
 private long transitionAt=System.nanoTime();private boolean refreshing;
 private AstralButton primary,favorite;private volatile Path ready;private volatile String transferError;private volatile List<LibraryCatalog.Entry> refreshed;
 public BackgroundLibraryScreen(class_437 parent){super(text("title"));this.parent=parent;}
 private static class_2561 text(String key,Object...args){return class_2561.method_43469("evatex.catalog."+key,args);}
 private static String label(String key){return text(key).getString();}
 @Override protected void method_25426(){
  margin=Math.max(10,Math.min(20,field_22789/40));left=field_22789<600?90:108;compact=field_22789<560;
  detailW=compact?0:Math.max(160,Math.min(230,field_22789/4));detailX=field_22789-margin-detailW;
  gridX=margin+left+14;gridW=field_22789-gridX-margin-(compact?0:detailW+14);top=100;
  columns=Math.max(1,Math.min(3,gridW/150));cardW=Math.max(70,(gridW-(columns-1)*10)/columns);cardH=Math.max(83,Math.min(134,cardW*9/16+39));
  cardH=Math.max(50,Math.min(cardH,field_22790-top-(compact?68:57)));
  if(compact&&field_22790-top-68>=164)cardH=Math.min(cardH,(field_22790-top-78)/2);
  rows=Math.max(1,(field_22790-top-(compact?68:57)+10)/(cardH+10));perPage=columns*rows;
  add(margin,44,Math.min(92,field_22789/5),20,text("local"),b->field_22787.method_1507(new VideoSelectScreen(parent,0)),false);
  add(margin+Math.min(92,field_22789/5)+6,44,72,20,text("discover"),b->{},true);
  search=method_37063(new class_342(field_22787.field_1772,gridX,76,gridW,18,text("search")));search.method_1880(120);search.method_1852(query);search.method_47404(AstralTypography.styled(text("search")));search.method_73210((value,offset)->AstralTypography.text(value).method_30937());search.method_1863(value->{query=value;page=0;rebuild();});
  int y=80,categoryStep=Math.max(16,Math.min(29,(field_22790-134)/7));
  for(String key:List.of("all","anime","astral","nature","games","minimal","favorites")){final String next=key;add(margin,y,left,Math.min(22,categoryStep-3),text("category."+key),b->{category=next;page=0;rebuild();},category.equals(key));y+=categoryStep;}
  filter();int pages=Math.max(1,(filtered.size()+perPage-1)/perPage);page=Math.max(0,Math.min(page,pages-1));
  if(selected==null || !filtered.contains(selected))selected=filtered.isEmpty()?null:filtered.get(0);
  for(int n=page*perPage;n<Math.min(filtered.size(),(page+1)*perPage);n++){
   var entry=filtered.get(n);int slot=n-page*perPage,x=gridX+(slot%columns)*(cardW+10),cy=top+(slot/columns)*(cardH+10);
   var button=add(x,cy,cardW,cardH,class_2561.method_43470(""),b->{selected=entry;rebuild();},selected!=null&&selected.id().equals(entry.id()));button.method_25355(class_2561.method_43470(entry.title()));
  }
  int footer=field_22790-36;
  add(margin,footer,left,20,class_2561.method_43471("gui.back"),b->method_25419(),false);
  if(pages>1){var prev=add(gridX,footer,26,20,class_2561.method_43470("‹"),b->{page--;rebuild();},false);prev.field_22763=page>0;var next=add(gridX+gridW-26,footer,26,20,class_2561.method_43470("›"),b->{page++;rebuild();},false);next.field_22763=page<pages-1;}
  if(selected!=null){
   int x=compact?gridX:detailX,w=compact?gridW:detailW,buttonY=compact?footer-28:field_22790-75;
   primary=add(x,buttonY,compact?w-96:w,22,actionLabel(),b->activate(),false);primary.field_22763=true;
   {favorite=add(compact?x+w-90:x,compact?buttonY:buttonY-28,compact?90:w,compact?22:20,text(config.libraryFavorites.contains(selected.id())?"unfavorite":"favorite"),b->{if(!config.libraryFavorites.remove(selected.id()))config.libraryFavorites.add(selected.id());config.save();rebuild();},config.libraryFavorites.contains(selected.id()));}
  }
  int previewWidth=field_22789<420?70:102;
  var refresh=add(field_22789-margin-previewWidth-76,44,70,20,text("refresh"),b->refreshCatalogue(),false);refresh.field_22763=!refreshing;
  add(field_22789-margin-previewWidth,44,previewWidth,20,text(config.libraryOnlinePreviews?"previews_on":"previews_off"),b->{config.libraryOnlinePreviews=!config.libraryOnlinePreviews;config.save();rebuild();},config.libraryOnlinePreviews);
 }
 private void filter(){String q=query.toLowerCase(Locale.ROOT);filtered=catalogue.stream().filter(e->category.equals("all")||category.equals("favorites")&&config.libraryFavorites.contains(e.id())||category.equals(e.category())).filter(e->q.isBlank()||(e.title()+" "+e.category()+" "+label("category."+e.category())).toLowerCase(Locale.ROOT).contains(q)).toList();}
 private AstralButton add(int x,int y,int w,int h,class_2561 t,class_4185.class_4241 action,boolean selected){return method_37063(new AstralButton(x,y,w,h,t,action,selected));}
 private void rebuild(){if(closed)return;transitionAt=System.nanoTime();boolean typing=search!=null&&method_25399()==search;int cursor=search==null?query.length():search.method_1881();method_37067();method_25426();search.method_1875(Math.min(query.length(),cursor));if(typing)method_48265(search);}
 private class_2561 actionLabel(){if(selected==null)return text("select");if(downloads.busy())return text("cancel");if(Files.isRegularFile(downloads.target(selected)))return text("apply");return text(selected.builtin()?"create_apply":selected.downloadable()?"download":"source");}
 private void activate(){
  if(downloads.busy()){downloads.cancel();return;}
  var entry=selected;if(entry==null)return;Path target=downloads.target(entry);
  if(Files.isRegularFile(target)){apply(target);return;}
  if(!entry.downloadable()){
   try{net.minecraft.class_156.method_668().method_673(LibraryCatalog.https(entry.sourceUrl()));}catch(Exception e){status=label("source_error");}return;
  }
  downloads.download(entry).whenComplete((path,error)->{if(!closed){if(error==null)ready=path;else transferError=label(downloads.progress().phase().equals("cancelled")?"cancelled":"download_error");}});rebuild();
 }
 private void apply(Path path){config.videoPath=path.toString();config.save();com.videomenu.video.VideoBackground.INSTANCE.reload(config);status=label("applied");}
 public void refreshCatalogue(){if(refreshing)return;refreshing=true;status=label("refreshing");rebuild();LibraryCatalog.refresh(config.libraryCatalogUrl.isBlank()?LibraryCatalog.DEFAULT_URL:config.libraryCatalogUrl).whenComplete((list,error)->{if(!closed){if(error==null)refreshed=list;else transferError=label("catalog_error");}});}
 @Override public void method_25419(){closed=true;downloads.cancel();thumbs.close();field_22787.method_1507(parent);}
 @Override public void method_25432(){closed=true;downloads.cancel();thumbs.close();}
 @Override public void method_25420(class_332 c,int x,int y,float delta){}
 @Override public boolean method_25422(){return false;}
 @Override public boolean method_25421(){return false;}
 @Override public void method_25394(class_332 ctx,int mx,int my,float delta){
  if(ready!=null){Path p=ready;ready=null;apply(p);rebuild();}if(transferError!=null){refreshing=false;status=transferError;transferError=null;rebuild();}if(refreshed!=null){refreshing=false;status=label("updated");catalogue=refreshed;refreshed=null;page=0;rebuild();}
  ctx.method_25296(0,0,field_22789,field_22790,0xFF080A0B,0xFF141719);
  AstralTypography.brand(ctx,margin,14,0xFFF2F2F2);AstralDraw.displayText(ctx,"EVATEX",margin+25,12,1.55f,0xFFF2F2F2);AstralTypography.draw(ctx,label("subtitle"),margin+110,18,0xFF999C9D);
  ctx.method_25294(margin+left+6,76,margin+left+7,field_22790-48,0xFF292D2F);
  if(!compact){ctx.method_25294(detailX-7,76,detailX-6,field_22790-48,0xFF292D2F);}
  super.method_25394(ctx,mx,my,delta);
  for(int n=page*perPage;n<Math.min(filtered.size(),(page+1)*perPage);n++){
   var e=filtered.get(n);int slot=n-page*perPage,x=gridX+(slot%columns)*(cardW+10),y=top+(slot/columns)*(cardH+10),ph=cardH-34;
   if(!thumbs.draw(ctx,e,x+3,y+3,cardW-6,ph-3,config.libraryOnlinePreviews)){ctx.method_25294(x+3,y+3,x+cardW-3,y+ph,0xFF202529);AstralTypography.centered(ctx,label("category."+e.category()),x+cardW/2,y+ph/2-4,0xFF858D90);}
   if(config.themeTransitions){double elapsed=(System.nanoTime()-transitionAt)/1e9;double fade=Math.max(0,1-(elapsed-slot*.022)/.24);if(fade>0)ctx.method_25294(x+3,y+3,x+cardW-3,y+ph,((int)(Math.min(1,fade)*180)<<24)|0x080A0B);}
   if(mx>=x&&mx<x+cardW&&my>=y&&my<y+cardH)ctx.method_25294(x+3,y+ph-1,x+cardW-3,y+ph,0x99FFFFFF);
   AstralTypography.draw(ctx,AstralTypography.fit(e.title(),cardW-14),x+7,y+ph+5,0xFFECECEC);
   AstralTypography.draw(ctx,label(e.builtin()?"original":"external")+(config.libraryFavorites.contains(e.id())?"  •":""),x+7,y+ph+18,0xFF939A9C);
  }
  if(selected!=null&&!compact){
   var e=selected;int y=80,ph=detailW*9/16;
   if(!thumbs.draw(ctx,e,detailX,y,detailW,ph,config.libraryOnlinePreviews)){ctx.method_25294(detailX,y,detailX+detailW,y+ph,0xFF202529);AstralTypography.brand(ctx,detailX+detailW/2-7,y+ph/2-7,0xFFBBBBBB);}
   y+=ph+14;y=wrap(ctx,e.title(),detailX,y,detailW,0xFFF2F2F2)+8;
   AstralTypography.draw(ctx,label("category."+e.category())+"  /  "+(e.kind().equals("image")?"PNG":"VIDEO"),detailX,y,0xFF969D9F);y+=18;
   AstralTypography.draw(ctx,e.width()>0?e.width()+" × "+e.height():label("source_resolution"),detailX,y,0xFF969D9F);y+=22;
   y=wrap(ctx,label(e.builtin()?"original_description":"source_description"),detailX,y,detailW,0xFFABB0B2)+12;
   wrap(ctx,e.builtin()?e.creator():"MyLiveWallpapers · "+label("creator_source"),detailX,y,detailW,0xFF81898C);
  }
  if(filtered.isEmpty())AstralTypography.centered(ctx,label("empty"),gridX+gridW/2,top+35,0xFFB5B5B5);
  if(!compact)AstralTypography.centered(ctx,(page+1)+" / "+Math.max(1,(filtered.size()+perPage-1)/perPage),gridX+gridW/2,field_22790-29,0xFF909799);
  var progress=downloads.progress();if(downloads.busy()){int px=compact?gridX:detailX,pw=compact?gridW:detailW,py=compact?field_22790-39:field_22790-49;ctx.method_25294(px,py,px+pw,py+2,0xFF303537);double fraction=progress.total()>0?Math.min(1,(double)progress.current()/progress.total()):.2+.6*(.5+.5*Math.sin(System.nanoTime()/4e8));ctx.method_25294(px,py,px+(int)(pw*fraction),py+2,0xFFF0F0F0);}
  String message=downloads.busy()?label("downloading")+" "+(progress.total()>0?progress.current()*100/progress.total()+"%":"…"):status.isEmpty()?text("count",filtered.size(),catalogue.size()).getString():status;
  AstralTypography.centered(ctx,AstralTypography.fit(message,field_22789-24),field_22789/2,field_22790-11,0xFF93999B);
 }
 private int wrap(class_332 ctx,String value,int x,int y,int width,int color){String line="";for(String word:value.split(" ")){String next=line.isEmpty()?word:line+" "+word;if(!line.isEmpty()&&AstralTypography.width(next)>width){AstralTypography.draw(ctx,line,x,y,color);y+=13;line=word;}else line=next;}if(!line.isEmpty()){AstralTypography.draw(ctx,AstralTypography.fit(line,width),x,y,color);y+=13;}return y;}
}
