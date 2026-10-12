import com.videomenu.CustomWidgetBounds;
import com.videomenu.ShortcutLayout.Rect;
import java.util.List;

public final class CustomWidgetBoundsRegression {
    public static class Widget {
        public double getX(){return 10.75;}
        public double getY(){return 20.25;}
        public double getWidth(){return 100.5;}
        public double getHeight(){return 14.5;}
        public boolean isVisible(){return true;}
        public List<?> getChildren(){return List.of(new Hidden());}
    }
    public static final class Hidden extends Widget {
        @Override public boolean isVisible(){return false;}
    }
    public static final class Broken extends Widget {
        @Override public double getX(){throw new IllegalStateException("not initialized");}
    }
    public static final class Invalid extends Widget {
        @Override public double getWidth(){return Double.NaN;}
    }
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception {
        check(CustomWidgetBounds.bounds(new Widget()).equals(new Rect(10,20,102,15)),"fractional bounds must reserve all painted pixels");
        check(!CustomWidgetBounds.visible(new Hidden()),"hidden custom control frees space");
        check(CustomWidgetBounds.bounds(new Broken()).equals(CustomWidgetBounds.UNKNOWN),"unreadable bounds cannot be treated as empty");
        check(CustomWidgetBounds.bounds(new Invalid()).equals(CustomWidgetBounds.UNKNOWN),"NaN cannot reserve zero space");
        check(CustomWidgetBounds.children(new Widget()).iterator().next() instanceof Hidden,"public nested widgets are discoverable");
        check(CustomWidgetBounds.bounds(new Object())==null,"unknown objects do not invent geometry");
        System.out.println("PASS public custom controls: fractional edges, visibility, nested children, unreadable and invalid bounds");
    }
}
