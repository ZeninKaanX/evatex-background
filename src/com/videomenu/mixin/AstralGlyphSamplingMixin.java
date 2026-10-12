package com.videomenu.mixin;
import net.minecraft.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Deferred GUI glyphs otherwise ignore the font texture's linear sampler. */
@Mixin(class_11243.class)
public abstract class AstralGlyphSamplingMixin {
    @Shadow public abstract class_11767 comp_4637();
    @Inject(method="comp_4055",at=@At("HEAD"),cancellable=true)
    private void evatex$coverage(CallbackInfoReturnable<com.mojang.blaze3d.pipeline.RenderPipeline> ci){
        var view=comp_4637().comp_4629();
        if(view!=null && com.videomenu.FontSmoothing.owns(view.texture()) && view.texture().getFormat()==com.mojang.blaze3d.textures.TextureFormat.RED8)
            ci.setReturnValue(com.videomenu.FontCoverage.pipeline(comp_4637().method_73400()));
    }
    @Inject(method="comp_4056",at=@At("HEAD"),cancellable=true)
    private void evatex$sample(CallbackInfoReturnable<class_11231> ci){
        var view=comp_4637().comp_4629();
        if(view!=null && com.videomenu.FontSmoothing.owns(view.texture()))ci.setReturnValue(class_11231.method_70902(view,RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR)));
    }
}
