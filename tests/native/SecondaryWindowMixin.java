package evatexqa;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(Window.class)
public abstract class SecondaryWindowMixin {
 @Inject(method="<init>",at=@At("TAIL"))
 private void evatexqa$hide(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci){
  long window=((Window)(Object)this).handle();
  org.lwjgl.glfw.GLFW.glfwSetWindowPos(window,2020,100);
  org.lwjgl.glfw.GLFW.glfwHideWindow(window);
 }
 @Redirect(method="<init>",at=@At(value="INVOKE",target="Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"),require=0)
 private long evatexqa$create(int w,int h,CharSequence title,long monitor,long share){
  org.lwjgl.glfw.GLFW.glfwWindowHint(org.lwjgl.glfw.GLFW.GLFW_VISIBLE,org.lwjgl.glfw.GLFW.GLFW_FALSE);
  long window=org.lwjgl.glfw.GLFW.glfwCreateWindow(w,h,title,monitor,share);
  org.lwjgl.glfw.GLFW.glfwSetWindowPos(window,2020,100);
  return window;
 }
}
