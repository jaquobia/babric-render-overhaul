package io.github.jaquobia.mixin;

import io.github.jaquobia.ExampleMod;
import io.github.jaquobia.Glfw;
import io.github.jaquobia.GlfwMinecraft;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.jaquobia.LwjglToGlfwHelper.translateKeyToGlfw;
import static io.github.jaquobia.LwjglToGlfwHelper.translateKeyToLWJGL;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "isKeyDown", at = @At("HEAD"), remap = false, cancellable = true)
    private static void injectIsKeyDown(int key, CallbackInfoReturnable<Boolean> cir) {
        int glfwKey = translateKeyToGlfw(key);
        cir.setReturnValue(Glfw.glfwGetKey(GlfwMinecraft.INSTANCE.window, glfwKey) == Glfw.GLFW_PRESS);
    }
    @Inject(method = "next", at = @At("HEAD"), remap = false, cancellable = true)
    private static void injectNext(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
    @Inject(method = "getNumKeyboardEvents", at = @At("HEAD"), remap = false, cancellable = true)
    private static void injectGetNumKeyboardEvents(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }
    @Inject(method = "getEventKey", at = @At("HEAD"), remap = false, cancellable = true)
    private static void injectGetEventKey(CallbackInfoReturnable<Integer> cir) {
        int key = translateKeyToLWJGL(GlfwMinecraft.INSTANCE.currentKeyboardButton);
        cir.setReturnValue(key);
    }
    @Inject(method = "getEventCharacter", at = @At("HEAD"), remap = false, cancellable = true)
    private static void injectGetCharacter(CallbackInfoReturnable<Character> cir) {
        int keyboardButton = GlfwMinecraft.INSTANCE.currentKeyboardButton;
        int scancode = Glfw.glfwGetScancode(keyboardButton);
        String keyName = Glfw.glfwGetKeyName(keyboardButton, scancode);
        cir.setReturnValue((keyName != null ? keyName.charAt(0) : (keyboardButton == Glfw.GLFW_KEY_SPACE) ? ' ' : '\0'));
    }
    @Inject(method = "getEventKeyState", at = @At("HEAD"), remap = false, cancellable = true)
    private static void injectGetEventKeyState(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(GlfwMinecraft.INSTANCE.currentKeyboardButtonState);
    }
}
