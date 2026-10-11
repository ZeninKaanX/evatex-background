package evatexqa;
import net.minecraft.class_1041;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
@Mixin(class_1041.class)
public abstract class HiddenWindowMixin {
    @Redirect(method="<init>",at=@At(value="INVOKE",target="Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"),require=0)
    private long evatexqa$create(int width,int height,CharSequence title,long monitor,long share) {
        if(Boolean.getBoolean("evatex.qa.hiddenWindow")) org.lwjgl.glfw.GLFW.glfwWindowHint(org.lwjgl.glfw.GLFW.GLFW_VISIBLE,org.lwjgl.glfw.GLFW.GLFW_FALSE);
        return org.lwjgl.glfw.GLFW.glfwCreateWindow(width,height,title,monitor,share);
    }
}
