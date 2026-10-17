package com.videomenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.class_310;
import net.minecraft.class_339;
import net.minecraft.class_4069;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_8021;

/** Reads visible native and public custom-widget bounds without loading optional clients. */
public final class MenuShortcut {
    private MenuShortcut() {}
    public static boolean customUi() { return false; }
    public static boolean optionsMenu(class_437 screen) { return screen instanceof net.minecraft.class_429; }
    public static boolean titleMenu(class_437 screen) {
        if (screen==null || class_310.method_1551().field_1687 != null) return false;
        if (screen instanceof EvatexTitleScreen) return false;
        if (screen instanceof class_442) return true;
        String name=screen.getClass().getSimpleName().toLowerCase(Locale.ROOT);
        return name.endsWith("mainmenu") || name.endsWith("mainmenuscreen") || name.endsWith("titlescreen")
            || name.endsWith("titlemenuscreen") || name.endsWith("homescreen");
    }
    public static List<ShortcutLayout.Rect> occupied(class_437 screen, Object shortcut) {
        List<ShortcutLayout.Rect> result=widgets(screen,shortcut);
        // Logos/splashes and text-only footer notices are not registered widgets.
        int w=screen.field_22789,h=screen.field_22790;
        if(titleMenu(screen))result.add(new ShortcutLayout.Rect(w/5,4,w*3/5,Math.min(85,h/3)));
        else result.add(new ShortcutLayout.Rect(0,0,w,32));
        result.add(new ShortcutLayout.Rect(0,Math.max(0,h-8),w,8));
        return result;
    }
    public static List<ShortcutLayout.Rect> widgets(class_437 screen,Object shortcut) {
        List<ShortcutLayout.Rect> result=new ArrayList<>();
        collect(screen,shortcut,result,Collections.newSetFromMap(new IdentityHashMap<>()),0);
        return result;
    }
    private static void collect(Object value,Object shortcut,List<ShortcutLayout.Rect> out,Set<Object> seen,int depth) {
        if (value==null || value==shortcut || !seen.add(value)) return;
        if (seen.size()>512 || depth>8) {
            // Unknown overflow: reject placement rather than assume unvisited controls are empty.
            out.add(new ShortcutLayout.Rect(0,0,Integer.MAX_VALUE/2,Integer.MAX_VALUE/2));return;
        }
        if (value instanceof class_339 widget && !widget.field_22764) return;
        if (value instanceof class_8021 widget) {
            if (widget.method_25368()>0 && widget.method_25364()>0)
                out.add(new ShortcutLayout.Rect(widget.method_46426(),widget.method_46427(),widget.method_25368(),widget.method_25364()));
        } else if (!(value instanceof class_437)) {
            if(!CustomWidgetBounds.visible(value))return;
            var bounds=CustomWidgetBounds.bounds(value);
            if(bounds!=null)out.add(bounds);
        }
        if (value instanceof class_4069 parent) {
            int count=0;
            for (Object child : parent.method_25396()) {if(++count>512){out.add(CustomWidgetBounds.UNKNOWN);break;}collect(child,shortcut,out,seen,depth+1);if(seen.size()>512)break;}
        }
        else if (!(value instanceof class_437)) try {
            int count=0;
            for(Object child:CustomWidgetBounds.children(value)) {if(++count>512){out.add(CustomWidgetBounds.UNKNOWN);break;}collect(child,shortcut,out,seen,depth+1);if(seen.size()>512)break;}
        }catch(ReflectiveOperationException|RuntimeException ignored){out.add(CustomWidgetBounds.UNKNOWN);}
    }
    public static final class State {
        private List<ShortcutLayout.Rect> last;
        private int lastWidth=-1,lastHeight=-1,lastButtonWidth=-1;
        private ShortcutLayout.Rect previous;
        public void place(class_437 screen,AstralButton button) {
            List<ShortcutLayout.Rect> occupied=occupied(screen,button);
            int width=screen.field_22789,height=screen.field_22790;
            int bw=Math.max(100,Math.min(144,class_310.method_1551().field_1772.method_1727(button.method_25369().getString())+20));
            if (width!=lastWidth || height!=lastHeight || bw!=lastButtonWidth || !occupied.equals(last)) {
                last=occupied;lastWidth=width;lastHeight=height;lastButtonWidth=bw;
                previous=ShortcutLayout.choose(width,height,bw,14,2,false,occupied,previous);
                var side=ShortcutLayout.chooseSide(width,height,bw,2,occupied);
                if (side!=null && (previous==null || previous.y()<height*.4)) previous=side;
                if (previous==null) previous=ShortcutLayout.choose(width,height,72,14,2,false,occupied,null);
            }
            button.field_22764=previous!=null;
            button.field_22763=previous!=null;
            if (previous!=null) {
                button.shortcutShape(previous.width(),previous.height(),previous.height()>previous.width(),previous.x()>width/2);
                button.method_46421(previous.x());button.method_46419(previous.y());
            }
        }
    }
}
