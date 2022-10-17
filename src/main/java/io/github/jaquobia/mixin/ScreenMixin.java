package io.github.jaquobia.mixin;

import io.github.jaquobia.Glfw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.jaquobia.LwjglToGlfwHelper.translateKeyToLWJGL;

@Mixin(Screen.class)
public class ScreenMixin {

    @Shadow
    public int width;
    @Shadow
    public int height;
    @Shadow
    protected Minecraft minecraft;
    @Shadow
    protected void keyPressed(char c, int i) {
    }

    @Shadow
    protected void mouseClicked(int i, int j, int k) {
    }
    @Shadow
    protected void mouseReleased(int i, int j, int k) {
    }

    @Inject(method = "tickInput", at = @At("HEAD"), cancellable = true)
    void injectTickInput(CallbackInfo ci) {
//        ci.cancel();
    }

    @Inject(method = "onMouseEvent", at = @At("HEAD"), cancellable = true)
    void injectOnMouseEvent(CallbackInfo ci) {
        int var1 = Mouse.getEventX() * this.width / this.minecraft.displayWidth;
        int var2 = this.height - Mouse.getEventY() * this.height / this.minecraft.displayHeight - 1;
        if (Mouse.getEventButtonState()) {
            this.mouseClicked(var1, var2, Mouse.getEventButton());
        } else {
            this.mouseReleased(var1, var2, Mouse.getEventButton());
        }
        ci.cancel();
    }
    @Inject(method = "onKeyboardEvent", at = @At("HEAD"), cancellable = true)
    void injectOnKeyboardEvent(CallbackInfo ci) {
        if (Keyboard.getEventKeyState()) {
            this.keyPressed(Keyboard.getEventCharacter(), Keyboard.getEventKey());
        }
        ci.cancel();
    }
}
