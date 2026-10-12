package evatextest;
/** Test-only client-style screen; never shipped in EVATEX. */
public final class MainMenuScreen extends net.minecraft.class_437 {
    public MainMenuScreen() {super(net.minecraft.class_2561.method_43470("Custom main menu QA"));}
    @Override protected void method_25426() {
        method_37063(new com.videomenu.AstralButton(24,0,field_22789-48,field_22790-10,
            net.minecraft.class_2561.method_43470("Client menu control"),button->{},false));
    }
    public static final class UnreadableWidget implements net.minecraft.class_364 {
        public int getX(){throw new IllegalStateException("Custom bounds temporarily unavailable");}
        public int getY(){return 0;}
        public int getWidth(){return 200;}
        public int getHeight(){return 20;}
        @Override public void method_25365(boolean focused){}
        @Override public boolean method_25370(){return false;}
    }
}
