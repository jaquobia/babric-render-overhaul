package io.github.jaquobia.mixin;

import io.github.jaquobia.ExampleMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ButtonWidget.class)
public class MixinButtonWidget {
    @Shadow
    public int x;
    @Shadow
    public int y;
    @Shadow
    protected int width;
    @Shadow
    protected int height;

    @Inject(method="isMouseOver", at = @At("HEAD"), cancellable = true, remap = false)
    void injectIsMouseOver(Minecraft mc, int x, int y, CallbackInfoReturnable<Boolean> cir) {

//        ExampleMod.LOGGER.info("" + (x - this.x) + " " + (y - this.y) + " " + (x - (this.x + this.width)) + " " + (y - (this.y + this.height)));
    }
}
