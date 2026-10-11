package com.videomenu;

import java.util.ArrayList;
import java.util.List;

/** Bounded, deterministic placement in GUI coordinates; no client-specific class linking. */
public final class ShortcutLayout {
    private ShortcutLayout() {}
    public record Rect(int x, int y, int width, int height) {
        public int right() { return x + width; }
        public int bottom() { return y + height; }
        public boolean overlaps(Rect other, int gap) {
            return x < other.right() + gap && right() + gap > other.x
                && y < other.bottom() + gap && bottom() + gap > other.y;
        }
    }
    public static boolean clear(Rect candidate, int width, int height, List<Rect> occupied, int gap) {
        if (candidate == null || candidate.width <= 0 || candidate.height <= 0 || candidate.x < gap
            || candidate.y < gap || candidate.right() > width-gap || candidate.bottom() > height-gap) return false;
        for (Rect rect : occupied) if (rect.width > 0 && rect.height > 0 && candidate.overlaps(rect, gap)) return false;
        return true;
    }
    public static Rect choose(int width, int height, int buttonWidth, int buttonHeight, int gap,
                              boolean rightAnchor, List<Rect> occupied, Rect previous) {
        if (width < buttonWidth+gap*2 || height < buttonHeight+gap*2) return null;
        int x=rightAnchor ? width-buttonWidth-gap : (width-buttonWidth)/2;
        int y=rightAnchor ? height-buttonHeight-gap : height >= 220 ? height-buttonHeight-12 : gap+2;
        Rect preferred=new Rect(x,y,buttonWidth,buttonHeight);
        if (clear(preferred,width,height,occupied,gap)) return preferred;
        List<Rect> candidates=new ArrayList<>();
        int right=width-buttonWidth-gap, bottom=height-buttonHeight-gap;
        for (int cx : new int[]{x,gap,right}) for (int cy : new int[]{y,bottom,gap,(height-buttonHeight)/2})
            candidates.add(new Rect(cx,cy,buttonWidth,buttonHeight));
        // Follow actual widget edges, keeping aligned gaps instead of arbitrary pixel nudges.
        for (Rect rect : occupied) {
            int above=rect.y-buttonHeight-gap, below=rect.bottom()+gap;
            int left=rect.x-buttonWidth-gap, after=rect.right()+gap;
            for (int cx : new int[]{x,rect.x,rect.right()-buttonWidth}) {
                candidates.add(new Rect(cx,above,buttonWidth,buttonHeight));
                candidates.add(new Rect(cx,below,buttonWidth,buttonHeight));
            }
            for (int cy : new int[]{y,rect.y,rect.bottom()-buttonHeight}) {
                candidates.add(new Rect(left,cy,buttonWidth,buttonHeight));
                candidates.add(new Rect(after,cy,buttonWidth,buttonHeight));
            }
        }
        // Include interior holes which need not share a widget's edge.
        for (int row=0;row<=8;row++) for (int col=0;col<=8;col++)
            candidates.add(new Rect(gap+(right-gap)*col/8,gap+(bottom-gap)*row/8,buttonWidth,buttonHeight));
        Rect best=null; double score=Double.POSITIVE_INFINITY;
        for (Rect candidate : candidates) {
            if (!clear(candidate,width,height,occupied,gap)) continue;
            double dx=(candidate.x-x)/(double)Math.max(1,width), dy=(candidate.y-y)/(double)Math.max(1,height);
            double cost=dx*dx+dy*dy*1.5;
            if (cost < score) {score=cost;best=candidate;}
        }
        return best; // A completely full menu must not acquire an overlapping invisible hitbox.
    }
    /** Slim edge rail for menus which leave side columns but no useful horizontal gap. */
    public static Rect chooseSide(int width,int height,int desiredLength,int gap,List<Rect> occupied) {
        int top=height/3+gap;
        int length=Math.min(desiredLength,height-top-12);
        if (length<52 || width<18+gap*2) return null;
        int centre=Math.max(top,(height-length)/2),bottom=height-length-12;
        List<Integer> rows=new ArrayList<>();rows.add(centre);rows.add(bottom);
        for(Rect rect:occupied) {rows.add(rect.y-length-gap);rows.add(rect.bottom()+gap);}
        for(int y:rows) {
            if(y<top || y+length>height-12) continue;
            for(int x:new int[]{width-18-gap,gap}) {
                Rect rail=new Rect(x,y,18,length);
                if(clear(rail,width,height,occupied,gap)) return rail;
            }
        }
        return null;
    }
}
