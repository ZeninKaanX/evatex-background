package com.videomenu;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4185;

/** Custom visuals with Minecraft's native focus, keyboard and narration behavior. */
public final class AstralButton extends class_4185 {
    private final boolean selected;
    private double hoverAmount;
    private long hoverAt;
    public AstralButton(int x, int y, int width, int height, class_2561 text, class_4241 action, boolean selected) {
        super(x, y, width, height, text, action, field_40754);
        this.selected = selected;
    }
    @Override
    protected void method_48579(class_332 context, int mouseX, int mouseY, float delta) {
        int x = method_46426(), y = method_46427(), w = method_25368(), h = method_25364();
        boolean hover = field_22762 || method_25370();
        long now = System.nanoTime();
        double dt = hoverAt == 0 ? 0 : Math.min(.1, (now-hoverAt)/1e9); hoverAt = now;
        hoverAmount = AstralTheme.animated() ? hoverAmount + ((hover ? 1 : 0)-hoverAmount)*(1-Math.exp(-dt*18)) : (hover ? 1 : 0);
        var palette = AstralTheme.current();
        int border = !field_22763 ? 0xFF333333 : AstralTheme.blend(selected ? palette.accent() : 0xFF454545, palette.secondary(), hoverAmount);
        AstralDraw.rounded(context, x, y, w, h, border);
        AstralDraw.rounded(context, x+1, y+1, w-2, h-2, AstralTheme.blend(palette.panel(), palette.accent(), selected ? .18 : hoverAmount*.16));
        if (selected) context.method_25294(x+1, y+1, x+3, y+h-1, palette.accent());
        var font = class_310.method_1551().field_1772;
        String label = font.method_27523(method_25369().getString(), Math.max(0, w-14));
        context.method_25300(font, label, x+w/2, y+(h-8)/2, !field_22763 ? 0xFF777777 : palette.text());
    }
}
