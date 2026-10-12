package com.videomenu;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.IdentityHashMap;
import java.util.Optional;
/** Preserve the source pipeline state; correct coverage only for our RED8 GUI glyphs. */
public final class FontCoverage {
    private static final IdentityHashMap<RenderPipeline,RenderPipeline> CACHE=new IdentityHashMap<>();
    private FontCoverage() {}
    public static RenderPipeline pipeline(RenderPipeline original){
        return CACHE.computeIfAbsent(original,source->{
            var snippet=new RenderPipeline.Snippet(Optional.of(source.getVertexShader()),Optional.of(source.getFragmentShader()),
                Optional.of(source.getShaderDefines()),Optional.of(source.getSamplers()),Optional.of(source.getUniforms()),
                source.getBlendFunction(),Optional.of(source.getDepthTestFunction()),Optional.of(source.getPolygonMode()),
                Optional.of(source.isCull()),Optional.of(source.isWriteColor()),Optional.of(source.isWriteAlpha()),
                Optional.of(source.isWriteDepth()),Optional.of(source.getColorLogic()),Optional.of(source.getVertexFormat()),Optional.of(source.getVertexFormatMode()));
            return RenderPipeline.builder(snippet).withLocation(net.minecraft.class_2960.method_60655("evatexbackgroundvideo","pipeline/ui_text_"+CACHE.size()))
                .withFragmentShader(net.minecraft.class_2960.method_60655("evatexbackgroundvideo","core/ui_text")).withDepthBias(source.getDepthBiasScaleFactor(),source.getDepthBiasConstant()).build();
        });
    }
}
