package io.github.jaquobia.mixin;

import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * We no longer use this class, just pretend a display always exists and "updates"
 */
@Mixin(Display.class)
public class DisplayMixin {
    @Inject(method = "update()V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void injectUpdate(CallbackInfo ci) {
        ci.cancel();
    }
    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true, remap = false)
    private static void injectIsActive(CallbackInfoReturnable<Boolean> cir) {

        cir.setReturnValue(true);
    }
}
