package io.github.jaquobia.mixin;

import io.github.jaquobia.ExampleMod;
import io.github.jaquobia.GlfwMinecraft;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.launch.knot.Knot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Knot.class)
public class KnotMixin {

    @Inject(method = "launch", at = @At("HEAD"), remap = false)
    private static void injectLaunch(String[] args, EnvType type, CallbackInfo ci) {
        ExampleMod.LOGGER.info("BABRIC 6");
//        GlfwMinecraft.run();
    }
}
