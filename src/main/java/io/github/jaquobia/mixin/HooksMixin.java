package io.github.jaquobia.mixin;

import io.github.jaquobia.ExampleMod;
import io.github.jaquobia.GlfwMinecraft;
import net.fabricmc.loader.impl.game.minecraft.Hooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(Hooks.class)
public class HooksMixin {

    @Inject(method = "startClient", at = @At("HEAD"), remap = false)
    private static void injectStartClient(File runDir, Object gameInstance, CallbackInfo ci) {
        ExampleMod.LOGGER.info("BABRIC 7");
//        GlfwMinecraft.run();
    }
}
