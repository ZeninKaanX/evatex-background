package com.videomenu.library;
import com.videomenu.VideoFiles;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
public final class LocalLibrary {
 private LocalLibrary(){}
 public static Map<LibraryCatalog.Entry,Path> discover(String selected){
  Map<LibraryCatalog.Entry,Path> out=new LinkedHashMap<>();
  Map<String,String> names=new HashMap<>();for(var entry:LibraryCatalog.initial())names.put(entry.id(),entry.title());
  for(Path file:VideoFiles.discover(selected))try{
   String key=LibraryCatalog.digest((file.toString()+Files.size(file)+Files.getLastModifiedTime(file)).getBytes(StandardCharsets.UTF_8));
   String kind=VideoFiles.isStillImage(file)?"image":VideoFiles.isGif(file)?"gif":"video";
   String title=file.getFileName().toString().replaceFirst("(?i)\\.(mp4|webm|mkv|mov|m4v|avi|png|jpe?g|gif|bmp)$","");
   title=names.getOrDefault(title,title);
   out.put(new LibraryCatalog.Entry("local-"+key,title,"minimal",kind,"","","","","","",Files.size(file),0,0,false,false),file);
  }catch(Exception ignored){}return out;
 }
}
