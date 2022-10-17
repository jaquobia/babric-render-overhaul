package io.github.jaquobia.mixin;

import io.github.jaquobia.ExampleMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements Runnable {


//	@Inject(at = @At("HEAD"), method = "init()V")
//	private void init(CallbackInfo info) {
//		ExampleMod.LOGGER.info("BABRIC2");
//	}


}
