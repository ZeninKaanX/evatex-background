package evatexqa;
import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(class_11231.class)
public abstract class FontProbeMixin {
    @Unique private static final java.util.Set<String> SEEN=new java.util.HashSet<>();
    @Inject(method={"method_70902","method_70900"},at=@At("RETURN"))
    private static void probe(CallbackInfoReturnable<class_11231> ci){
        var setup=ci.getReturnValue();var view=setup.comp_4052();
        if(view==null)return;
        String label=view.texture().getLabel();
        if(SEEN.size()<40 && SEEN.add(label)){
            var sampler=setup.comp_5053();
            System.out.println("EVATEX_QA FONT "+label+" owned="+com.videomenu.FontSmoothing.owns(view.texture())+" min="+sampler.method_75288()+" mag="+sampler.method_75289());
            if(com.videomenu.FontSmoothing.owns(view.texture()) && sampler.method_75288()!=com.mojang.blaze3d.textures.FilterMode.LINEAR)throw new AssertionError("Owned font sampler was not linear");
        }
    }
}
