package io.github.jaquobia.mixin;

import io.github.jaquobia.ExampleMod;
import io.github.jaquobia.GlfwMinecraft;
import net.fabricmc.loader.impl.game.minecraft.applet.AppletMain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AppletMain.class)
public class AppletMainMixin {

    @Inject(method = "main", at = @At("HEAD"), remap = false)
    private static void injectMain(String[] args, CallbackInfo ci) {
        ExampleMod.LOGGER.info("BABRIC 3");
    }
}
