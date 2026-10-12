package com.videomenu;

import java.lang.reflect.Method;

/** Public widget geometry only; no dependency on a client's private UI classes. */
public final class CustomWidgetBounds {
    public static final ShortcutLayout.Rect UNKNOWN=new ShortcutLayout.Rect(0,0,1<<27,1<<27);
    private CustomWidgetBounds() {}
    private static Method method(Class<?> type,String name) {
        try { return type.getMethod(name); }
        catch(NoSuchMethodException|SecurityException ignored){return null;}
    }
    private static final ClassValue<Method[]> ACCESS=new ClassValue<>() {
        @Override protected Method[] computeValue(Class<?> type) {
            return new Method[]{method(type,"getX"),method(type,"getY"),method(type,"getWidth"),method(type,"getHeight"),method(type,"isVisible"),method(type,"getChildren")};
        }
    };
    public static boolean visible(Object widget) {
        Method method=ACCESS.get(widget.getClass())[4];
        if(method==null)return true;
        try{return !Boolean.FALSE.equals(method.invoke(widget));}
        catch(ReflectiveOperationException|RuntimeException ignored){return true;}
    }
    public static ShortcutLayout.Rect bounds(Object widget) {
        Method[] methods=ACCESS.get(widget.getClass());
        for(int i=0;i<4;i++)if(methods[i]==null)return null;
        try {
            double x=((Number)methods[0].invoke(widget)).doubleValue(),y=((Number)methods[1].invoke(widget)).doubleValue();
            double w=((Number)methods[2].invoke(widget)).doubleValue(),h=((Number)methods[3].invoke(widget)).doubleValue();
            if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(w)||!Double.isFinite(h))return UNKNOWN;
            if(w<=0 || h<=0)return null;
            double left=Math.floor(x),top=Math.floor(y),right=Math.ceil(x+w),bottom=Math.ceil(y+h);
            if(left<Integer.MIN_VALUE/2 || top<Integer.MIN_VALUE/2 || right>Integer.MAX_VALUE/2 || bottom>Integer.MAX_VALUE/2)return UNKNOWN;
            return new ShortcutLayout.Rect((int)left,(int)top,(int)(right-left),(int)(bottom-top));
        }catch(ReflectiveOperationException|RuntimeException ignored){return UNKNOWN;}
    }
    public static Iterable<?> children(Object widget) throws ReflectiveOperationException {
        Method method=ACCESS.get(widget.getClass())[5];
        if(method==null)return java.util.List.of();
        Object children=method.invoke(widget);
        return children instanceof Iterable<?> iterable ? iterable : java.util.List.of();
    }
}
