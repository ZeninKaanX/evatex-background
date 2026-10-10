package com.videomenu.video;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
/** Use linear sampling when decoded pixels are scaled to the window. */
final class VideoTexture extends class_1043 {
    VideoTexture(class_1011 image) {
        super(() -> "EVATEX video", image);
        field_63613 = RenderSystem.getSamplerCache().method_75297(FilterMode.LINEAR);
    }
}
