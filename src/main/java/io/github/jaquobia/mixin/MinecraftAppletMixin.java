package io.github.jaquobia.mixin;

import io.github.jaquobia.ExampleMod;
import io.github.jaquobia.GlfwMinecraft;
import net.minecraft.client.MinecraftApplet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.applet.Applet;

/**
 * Steal the applet and its information
 */
@Mixin(MinecraftApplet.class)
public abstract class MinecraftAppletMixin extends Applet {
    @Inject(method = "init", at = @At("HEAD"), remap = false, cancellable = true)
    public void injectInit(CallbackInfo ci) {
        ExampleMod.LOGGER.info("Stand-alone: " + this.getParameter("stand-alone"));

        String s_width = this.getParameter("width");
        String s_height = this.getParameter("height");
        String s_username = this.getParameter("username");
        String s_fullscreen = this.getParameter("fullscreen");

        int width = s_width == null ? 854 : Integer.parseInt(s_width);
        int height = s_height == null ? 615 : Integer.parseInt(s_height);
        String username = s_username == null ? "Player" : s_username;
        boolean fullscreen = Boolean.parseBoolean(s_fullscreen);

        String host = this.getDocumentBase().getHost();
        String port = String.valueOf(this.getDocumentBase().getPort());
        //
        GlfwMinecraft.runWindow(width, height, username, fullscreen, host, port);
        // Force a system exit, prevents applet from starting
        ci.cancel();
    }
}
