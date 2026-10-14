package com.videomenu;

import com.videomenu.video.VideoBackground;
import java.lang.reflect.Method;
import com.videomenu.EvatexPlatform;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_442;

/** Optional bridge. No Atlas classes, settings or binaries are copied into EVATEX. */
public final class AtlasCompatibility {
    public static final boolean SUPPORTED = EvatexPlatform.modVersion("atlas-client").equals("3.0.3");
    private static boolean failed;
    private AtlasCompatibility() {}
    private static final class Api {
        static final ClassLoader LOADER = AtlasCompatibility.class.getClassLoader();
        static final Class<?> GUI = find("imgui.ImGui"), DRAW = find("imgui.ImDrawList");
        static final Method VANILLA = method(find("me.koraizen.atlasclient.gui.UIModeManager"), "isVanillaMode");
        static final Method IMAGE = method(DRAW, "addImage", long.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, int.class);
        static final Method RECT = method(DRAW, "addRectFilled", float.class, float.class, float.class, float.class, int.class);
        static final Method GRADIENT = method(DRAW, "addRectFilledMultiColor", float.class, float.class, float.class, float.class, int.class, int.class, int.class, int.class);
        static final Method POS = method(GUI, "setNextWindowPos", float.class, float.class);
        static final Method SIZE = method(GUI, "setNextWindowSize", float.class, float.class);
        static final Method BEGIN = method(GUI, "begin", String.class, int.class);
        static final Method BUTTON = method(GUI, "button", String.class, float.class, float.class);
        static final Method END = method(GUI, "end");
        static final Method PUSH = method(GUI, "pushStyleColor", int.class, int.class);
        static final Method POP = method(GUI, "popStyleColor", int.class);
        static final Method PUSH_VECTOR = method(GUI, "pushStyleVar", int.class, float.class, float.class);
        static final Method PUSH_FLOAT = method(GUI, "pushStyleVar", int.class, float.class);
        static final Method POP_VARS = method(GUI, "popStyleVar", int.class);
        static final Method FONT_SIZE = method(GUI, "getFontSize");
        static final int WINDOW_PADDING = style("WindowPadding"), FRAME_PADDING = style("FramePadding"),
            BUTTON_ALIGN = style("ButtonTextAlign"), FRAME_ROUNDING = style("FrameRounding");
        private static int style(String name) {
            try { return find("imgui.flag.ImGuiStyleVar").getField(name).getInt(null); }
            catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
        }
        static final int TEXT = color("Text"), BUTTON_COLOR = color("Button"), HOVER = color("ButtonHovered"), ACTIVE = color("ButtonActive");
        private static int color(String name) {
            try { return find("imgui.flag.ImGuiCol").getField(name).getInt(null); }
            catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
        }
        static final Method AFTER = method(find("me.koraizen.atlasclient.gui.ImguiLoader"), "schedulePostFrame", Runnable.class);
        private static Class<?> find(String name) {
            try { return Class.forName(name, false, LOADER); }
            catch (ClassNotFoundException e) { throw new IllegalStateException(e); }
        }
        private static Method method(Class<?> type, String name, Class<?>... parameters) {
            try { return type.getMethod(name, parameters); }
            catch (NoSuchMethodException e) { throw new IllegalStateException(e); }
        }
    }
    public static boolean isCustomUi() {
        if (!SUPPORTED || failed) return false;
        try { return !(boolean)Api.VANILLA.invoke(null); }
        catch (ReflectiveOperationException | LinkageError e) { fail(e); return false; }
    }
    public static boolean drawBackground(Object list, float width, float height, float alpha) {
        if (!SUPPORTED || failed || !isCustomUi()) return false;
        var mc = class_310.method_1551();
        if (!(mc.field_1755 instanceof class_442 || mc.field_1755 instanceof net.minecraft.class_500) || mc.field_1687 != null) return false;
        var frame = VideoBackground.INSTANCE.atlasFrame();
        if (frame == null) return false;
        try {
            float scale = frame.fill() ? Math.max(width/frame.width(), height/frame.height()) : Math.min(width/frame.width(), height/frame.height());
            float dw = frame.width()*scale, dh = frame.height()*scale;
            // Crop through UV coordinates, so ImGui's existing clipping remains valid.
            float u0 = frame.fill() ? Math.max(0, (dw-width)/(2*dw)) : 0;
            float v0 = frame.fill() ? Math.max(0, (dh-height)/(2*dh)) : 0;
            float x = frame.fill() ? 0 : (width-dw)/2, y = frame.fill() ? 0 : (height-dh)/2;
            Api.RECT.invoke(list, 0f, 0f, width, height, 0xFF080808);
            Api.IMAGE.invoke(list, (long)frame.textureId(), x, y, frame.fill() ? width : x+dw, frame.fill() ? height : y+dh,
                u0, v0, 1-u0, 1-v0, 0xFFFFFFFF);
            int dark = (int)(frame.darkening()*255)<<24;
            Api.RECT.invoke(list, 0f, 0f, width, height, dark);
            if (frame.vignette()) {
                float edge = Math.min(width,height)/5;
                Api.GRADIENT.invoke(list, 0f, 0f, width, edge, 0x80000000, 0x80000000, 0, 0);
                Api.GRADIENT.invoke(list, 0f, height-edge, width, height, 0, 0, 0x80000000, 0x80000000);
            }
            return true;
        } catch (ReflectiveOperationException | LinkageError e) { fail(e); return false; }
    }
    public static void shortcut() {
        var mc = class_310.method_1551();
        if (!isCustomUi() || !(mc.field_1755 instanceof class_442)) return;
        try {
            float width = mc.method_22683().method_4480();
            // Atlas uses window pixels; Minecraft widgets use GUI-scaled coordinates.
            // Keep the lower-right anchor without overlapping the account card.
            float scale = Math.max(1f, width / Math.max(1, mc.method_22683().method_4486()));
            float buttonWidth = Math.min(Math.min(180f, Math.max(140f, 100f * scale)), Math.max(32f, width * .35f));
            float fontSize = ((Number)Api.FONT_SIZE.invoke(null)).floatValue();
            float buttonHeight = Math.max(fontSize + 10f, 28f);
            float height = mc.method_22683().method_4507();
            java.util.List<ShortcutLayout.Rect> occupied=new java.util.ArrayList<>();
            float verticalScale=height/Math.max(1,mc.field_1755.field_22790);
            for (var rect : MenuShortcut.widgets(mc.field_1755,null)) {
                occupied.add(new ShortcutLayout.Rect((int)Math.floor(rect.x()*scale),(int)Math.floor(rect.y()*verticalScale),
                    (int)Math.ceil(rect.width()*scale),(int)Math.ceil(rect.height()*verticalScale)));
            }
            var position=ShortcutLayout.choose((int)width,(int)height,(int)Math.ceil(buttonWidth),
                (int)Math.ceil(buttonHeight),24,true,occupied,null);
            if(position==null) return;
            Api.POS.invoke(null, (float)position.x(), (float)position.y());
            Api.SIZE.invoke(null, buttonWidth, buttonHeight);
            // No title bar, resize, move, scrollbar, saved layout or window background.
            int pushed = 0, variables = 0;
            try {
                int[] roles = {Api.TEXT, Api.BUTTON_COLOR, Api.HOVER, Api.ACTIVE};
                int[] colors = {0xFFF2F2F2, 0xFF252525, 0xFF484848, 0xFF626262};
                for (int i=0; i<roles.length; i++) { Api.PUSH.invoke(null, roles[i], colors[i]); pushed++; }
                // Explicit local overrides: Atlas style must not shift our label.
                Api.PUSH_VECTOR.invoke(null, Api.WINDOW_PADDING, 0f, 0f); variables++;
                Api.PUSH_VECTOR.invoke(null, Api.FRAME_PADDING, 8f, 4f); variables++;
                Api.PUSH_VECTOR.invoke(null, Api.BUTTON_ALIGN, .5f, .5f); variables++;
                Api.PUSH_FLOAT.invoke(null, Api.FRAME_ROUNDING, 6f); variables++;
            boolean visible = (boolean)Api.BEGIN.invoke(null, "##EVATEX-video-menu", 1|2|4|8|16|32|128|256);
            try {
                if (visible && (boolean)Api.BUTTON.invoke(null, class_2561.method_43471("evatex.title").getString(), buttonWidth, buttonHeight)) {
                    Api.AFTER.invoke(null, (Runnable)() -> mc.method_1507(new BackgroundLibraryScreen(mc.field_1755, true)));
                }
            } finally { Api.END.invoke(null); }
            } finally { try { if (variables > 0) Api.POP_VARS.invoke(null, variables); } finally { if (pushed > 0) Api.POP.invoke(null, pushed); } }
        } catch (ReflectiveOperationException | LinkageError e) { fail(e); }
    }
    private static void fail(Throwable e) {
        if (!failed) VideoMenuClient.LOGGER.warn("Atlas compatibility bridge disabled after an API error", e);
        failed = true;
    }
}
