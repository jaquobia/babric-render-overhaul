package io.github.jaquobia;

import net.minecraft.class_596;

public class MyMouseHelper extends class_596 {
    public MyMouseHelper() {
        super(null);
    }

    public void resetMouse() {
        int targetX = GlfwMinecraft.INSTANCE.displayWidth/2, targetY = GlfwMinecraft.INSTANCE.displayHeight/2;
        GlfwMinecraft.INSTANCE.mouseLX = targetX;
        GlfwMinecraft.INSTANCE.mouseLY = targetY;
        GlfwMinecraft.INSTANCE.mouseX = targetX;
        GlfwMinecraft.INSTANCE.mouseY = targetY;
        GlfwMinecraft.INSTANCE.getMouseDX();
        GlfwMinecraft.INSTANCE.getMouseDY();
        Glfw.glfwSetCursorPos(GlfwMinecraft.INSTANCE.window, targetX, targetY);
        this.field_2586 = 0;
        this.field_2587 = 0;
    }

    public void method_1970() {
        resetMouse();
        Glfw.glfwSetInputMode(GlfwMinecraft.INSTANCE.window, Glfw.GLFW_CURSOR, Glfw.GLFW_CURSOR_DISABLED);
    }

    public void method_1971() {
        resetMouse();
        Glfw.glfwSetInputMode(GlfwMinecraft.INSTANCE.window, Glfw.GLFW_CURSOR, Glfw.GLFW_CURSOR_NORMAL);
    }

    public void method_1972() {
        this.field_2586 = GlfwMinecraft.INSTANCE.getMouseDX();
        this.field_2587 = GlfwMinecraft.INSTANCE.getMouseDY();
    }

}
