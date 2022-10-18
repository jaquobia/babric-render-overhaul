package io.github.jaquobia.mixin;

import io.github.jaquobia.Glfw;
import io.github.jaquobia.GlfwMinecraft;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Redirect to use glfw inputs
 */
@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(method = "getX",at = @At("HEAD"), cancellable = true, remap = false)
    private static void injectMouseX(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(GlfwMinecraft.INSTANCE.mouseX);
    }
    @Inject(method = "getY",at = @At("HEAD"), cancellable = true, remap = false)
    private static void injectMouseY(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(GlfwMinecraft.INSTANCE.mouseY);
    }
    @Inject(method = "getEventX",at = @At("HEAD"), cancellable = true, remap = false)
    private static void injectMouseEventX(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(GlfwMinecraft.INSTANCE.mouseX);
    }
    @Inject(method = "getEventY",at = @At("HEAD"), cancellable = true, remap = false)
    private static void injectMouseEventY(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(GlfwMinecraft.INSTANCE.mouseY);
    }
    @Inject(method = "isButtonDown",at = @At("HEAD"), cancellable = true, remap = false)
    private static void injectIsButtonDown(int button, CallbackInfoReturnable<Boolean> cir) {
        boolean isButtonDown = Glfw.glfwGetMouseButton(GlfwMinecraft.INSTANCE.window, button) >= Glfw.GLFW_PRESS;
        cir.setReturnValue(isButtonDown);
    }
    @Inject(method = "next",at = @At("HEAD"), cancellable = true, remap = false)
    private static void injectNext(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
    @Inject(method = "getEventButtonState", at = @At("HEAD"), remap = false, cancellable = true)
    private static void injectGetEventButtonState(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(GlfwMinecraft.INSTANCE.currentMouseButtonState);
    }
    @Inject(method = "getEventButton", at = @At("HEAD"), remap = false, cancellable = true)
    private static void injectGetEventButton(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(GlfwMinecraft.INSTANCE.currentMouseButton);
    }
    @Inject(method = "isCreated", at = @At("HEAD"), remap = false, cancellable = true)
    private static void injectIsCreated(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
