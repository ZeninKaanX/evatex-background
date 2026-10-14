package com.videomenu;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4185;

/** Custom visuals with Minecraft's native focus, keyboard and narration behavior. */
public final class AstralButton extends class_4185 {
    private final boolean selected;
    private boolean surface,emphasized,card;
    private String visualLabel;
    private long pressedUntil;
    public AstralButton caption(String value){visualLabel=value;return this;}
    public AstralButton astralSurface(boolean emphasized){this.surface=true;this.emphasized=emphasized;return this;}
    public AstralButton catalogCard(){this.surface=true;this.card=true;return this;}
    private boolean vertical,clockwise;
    public void shortcutShape(int width,int height,boolean vertical,boolean clockwise) {
        this.field_22758=width;this.field_22759=height;this.vertical=vertical;this.clockwise=clockwise;
    }
    private double hoverAmount;
    private long hoverAt;
    public AstralButton(int x, int y, int width, int height, class_2561 text, class_4241 action, boolean selected) {
        super(x, y, width, height, text, action, field_40754);
        this.selected = selected;
    }
    @Override public void method_25354(net.minecraft.class_1144 manager){pressedUntil=System.nanoTime()+110_000_000L;AstralSound.click(manager);}
    @Override
    protected void method_48579(class_332 context, int mouseX, int mouseY, float delta) {
        int x = method_46426(), y = method_46427(), w = method_25368(), h = method_25364();
        boolean hover = field_22762 || method_25370();
        long now = System.nanoTime();
        double dt = hoverAt == 0 ? 0 : Math.min(.1, (now-hoverAt)/1e9); hoverAt = now;
        hoverAmount = AstralTheme.animated() ? hoverAmount + ((hover ? 1 : 0)-hoverAmount)*(1-Math.exp(-dt*18)) : (hover ? 1 : 0);
        var palette = AstralTheme.current();
        if(!card&&System.nanoTime()<pressedUntil)y+=1;
        int surface=AstralTheme.blend(0x9A181818,0xC83C3C3C,selected ? .65 : hoverAmount);
        if(this.surface){
            boolean light=field_22763&&!card&&(selected||emphasized);
            int border=selected||method_25370()?0xFFF1F1F1:AstralTheme.blend(0xFF292B2F,0xFF72757B,hoverAmount);
            AstralDraw.rounded(context,x,y,w,h,border);
            boolean pressed=System.nanoTime()<pressedUntil;
            int inner=light?AstralTheme.blend(0xFFE5E5E5,0xFFFFFFFF,hoverAmount):AstralTheme.blend(0xFF17191C,0xFF2A2D32,hoverAmount);
            AstralDraw.rounded(context,x+1,y+1,w-2,h-2,pressed?AstralTheme.blend(inner,light?0xFFBFC2C6:0xFF08090B,.55):inner);
            if(card)return;
        }else{
        context.method_25294(x,y,x+w,y+h,surface);
        context.method_25294(x,y+h-1,x+w,y+h,selected ? palette.accent() : AstralTheme.blend(0x44FFFFFF,0xCCFFFFFF,hoverAmount));
        if(selected)context.method_25294(x,y,x+2,y+h,palette.accent());
        }
        var font = class_310.method_1551().field_1772;
        String label = AstralTypography.fit(visualLabel==null?method_25369().getString():visualLabel, Math.max(0, (vertical ? h : w)-14));
        int textColor=!field_22763 ? 0xFF777777 : this.surface&&(selected||emphasized)?0xFF121316:palette.text();
        if (vertical) {
            var matrix=context.method_51448();
            matrix.method_22903();
            try {
                matrix.method_22904(x+w/2f,y+h/2f,0);
                matrix.method_22907(new org.joml.Quaternionf().rotateZ(clockwise ? (float)Math.PI/2 : -(float)Math.PI/2));
                AstralTypography.centered(context,label,0,-font.field_2000/2,textColor);
            } finally {matrix.method_22909();}
        } else AstralTypography.centered(context,label,x+w/2,y+(h-font.field_2000)/2,textColor);
    }
}
