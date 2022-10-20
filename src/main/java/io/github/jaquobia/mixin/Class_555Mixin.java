package io.github.jaquobia.mixin;

import io.github.jaquobia.Glfw;
import io.github.jaquobia.GlfwMinecraft;
import net.minecraft.class_12;
import net.minecraft.class_555;
import net.minecraft.class_564;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Handles Refresh rates and mouse deltas
 */
@Mixin(class_555.class)
public abstract class Class_555Mixin {
    @Shadow
    private long field_2334;
    @Shadow
    private class_12 field_2353;
    @Shadow
    private class_12 field_2354;
    @Shadow
    public static boolean field_2340;

    @Shadow
    public abstract void method_1841(float f, long l);
    @Shadow
    public abstract void method_1843();


    @Shadow
    private long field_2335;

    @Inject(method = "method_1844", at = @At("HEAD"), cancellable = true)
    void injectMethod_1844(float f, CallbackInfo ci) {
        GlfwMinecraft mc = GlfwMinecraft.INSTANCE;
        // Lost focus
        if (System.currentTimeMillis() - this.field_2334 > 500L) {
            mc.method_2135();
        }
        this.field_2334 = System.currentTimeMillis();

        int yMod;
        if (mc.field_2778) {
            mc.field_2767.method_1972();
            float sensitivityA = mc.options.mouseSensitivity * 0.6F + 0.2F;
            float sensitivityB = sensitivityA * sensitivityA * sensitivityA * 8.0F;
            float xSensitivity = (float)mc.field_2767.field_2586 * sensitivityB;
            float ySensitivity = (float)mc.field_2767.field_2587 * sensitivityB;
            yMod = 1;
            if (mc.options.invertYMouse) {
                yMod = -1;
            }

            if (mc.options.cinematicMode) {
                xSensitivity = this.field_2353.method_40(xSensitivity, 0.05F * sensitivityB);
                ySensitivity = this.field_2354.method_40(ySensitivity, 0.05F * sensitivityB);
            }

            mc.player.method_1362(xSensitivity, ySensitivity * (float)yMod);
        }

        if (mc.lastFpsLimit != mc.options.fpsLimit)
        {
            Glfw.glfwSwapInterval(mc.options.fpsLimit);
            mc.lastFpsLimit = mc.options.fpsLimit;
        }

        if (!mc.field_2821 && mc.displayWidth * mc.displayHeight > 0) {
            field_2340 = mc.options.anaglyph3d;
            class_564 RenderScaler = new class_564(mc.options, mc.displayWidth, mc.displayHeight);
            int scaleX = RenderScaler.method_1857();
            int scaleY = RenderScaler.method_1858();
            int var16 = mc.mouseX * scaleX / mc.displayWidth;
            yMod = scaleY - mc.mouseY * scaleY / mc.displayHeight - 1;

            if (mc.world != null) {
                if (mc.options.fpsLimit == 0) {
                    this.method_1841(f, 0L);
                } else {
                    short fps = (short) (80 * (2 - mc.options.fpsLimit) + 40); // (0, 1, 2) -> (200, 120, 40)
                    this.method_1841(f, this.field_2335 + (long)(1000000000 / fps));
                }

                this.field_2335 = System.nanoTime();
                if (!mc.options.hideHud || mc.currentScreen != null) {
                    mc.inGameHud.render(f, mc.currentScreen != null, var16, yMod);
                }
            } else {
                GL11.glMatrixMode(5889);
                GL11.glLoadIdentity();
                GL11.glMatrixMode(5888);
                GL11.glLoadIdentity();
                this.method_1843();

                this.field_2335 = System.nanoTime();
            }

            if (mc.currentScreen != null) {
                GL11.glClear(256);
                mc.currentScreen.render(var16, yMod, f);
                if (mc.currentScreen != null && mc.currentScreen.field_157 != null) {
                    mc.currentScreen.field_157.method_352(f);
                }
            }

        }
        ci.cancel();
    }
}
