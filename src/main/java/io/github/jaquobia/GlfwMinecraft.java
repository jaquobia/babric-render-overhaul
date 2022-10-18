package io.github.jaquobia;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.*;
import net.minecraft.achievement.Achievements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Option;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.resource.ResourceDownloadThread;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Session;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.slf4j.Logger;

import java.io.File;

import static io.github.jaquobia.LwjglToGlfwHelper.translateKeyToGlfw;

@Environment(EnvType.CLIENT)
public class GlfwMinecraft extends Minecraft implements GlfwCallback {

    static final Logger LOGGER = ExampleMod.LOGGER;
    static boolean glfwInit = Glfw.glfwInit(); // A nice way to initialize glfw and report it at the same time
    public static GlfwMinecraft INSTANCE = null; // A static instance for … whenever it is needed?

    public static void runWindow(int width, int height, String username, boolean fullscreen, String host, String port) {
        INSTANCE = new GlfwMinecraft(width, height, username, fullscreen, host, port);
        try {
            INSTANCE.mcThread.start();
            INSTANCE.mcThread.join(); // Pause this thread until the window is done
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Glfw.glfwDestroyWindow(INSTANCE.window);
        Glfw.glfwTerminate();
        LOGGER.info("Terminating GLFW window");
        System.exit(1); // Our window closed, don't continue and open normal mc's window
    }

    public long window;
    Thread mcThread;
    public int lastFpsLimit;
    public int x, y;
    public int old_x, old_y, old_width, old_height; // used for fullscreen
    public int mouseX = 0, mouseY = 0;
    public int mouseLX = 0, mouseLY = 0; // used for Mouse.DX() mouse.DY()
    public int mouseDX = 0, mouseDY = 0;

    public boolean currentMouseButtonState = false;
    public int currentMouseButton = 0;
    public boolean currentKeyboardButtonState = false;
    public int currentKeyboardButton = 0;
    public int currentKeyboardButtonModifiers = 0;

    public boolean delegateToCharCallback = false;
    public char currentKeyboardButtonCharacter = '\0';

    GlfwMinecraft(int width, int height, String username, boolean fullscreen, String host, String port) {
        super(null, null, null, width, height, fullscreen);
        this.displayWidth = width;
        this.displayHeight = height;

        mcThread = new Thread(this, "Minecraft main thread");

        this.session = new Session(username, "");
        this.applet = null;
        this.isApplet = false;

        if (host != null) {
            this.field_2810 = host;
            if (!port.equals("-1"))
                this.field_2810 += (":" + port);
        }
    }

    void createWindow() {
        if (window != 0) {
            return; // we already created a window
        }
        Glfw.glfwWindowHint(Glfw.GLFW_OPENGL_PROFILE, Glfw.GLFW_OPENGL_COMPAT_PROFILE);
        Glfw.glfwWindowHint(Glfw.GLFW_CONTEXT_VERSION_MAJOR, 3);
        Glfw.glfwWindowHint(Glfw.GLFW_CONTEXT_VERSION_MINOR, 3);
        long monitor = fullscreen ? Glfw.glfwGetPrimaryMonitor() : 0;
        this.window = Glfw.glfwCreateWindow(displayWidth, displayHeight, "Minecraft b1.7.3", monitor, 0);
        Glfw.glfwSetCallback(this);
        LOGGER.info("Created Glfw Window!");
    }

    @Override
    public void toggleFullscreen() {
        this.fullscreen = !this.fullscreen;
        if (fullscreen) {
            old_x = x;
            old_y = y;
            old_width = displayWidth;
            old_height = displayHeight;
            Glfw.glfwSetCurrentWindowMonitor(window);
        } else {
            this.x = old_x;
            this.y = old_y;
            this.displayWidth = old_width;
            this.displayHeight = old_height;
            Glfw.glfwSetWindowMonitor(window, 0, old_x, old_y, old_width, old_height, Glfw.GLFW_DONT_CARE);
        }
    }

    /// Start Glfw Callbacks
    @Override
    public void error(int error, String description) {
        LOGGER.error(String.format("GlfwError(%d): %s", error, description));
    }

    @Override
    public void monitor(long monitor, boolean connected) {
    }

    @Override
    public void joystick(int i, int i1) {

    }

    @Override
    public void windowPos(long window, int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void windowSize(long window, int width, int height) {
    }

    @Override
    public void windowClose(long window) {
        if (window == this.window) {
            this.scheduleStop();
        }
    }

    @Override
    public void windowRefresh(long window) {

    }

    @Override
    public void windowFocus(long window, boolean focused) {

    }

    @Override
    public void windowIconify(long window, boolean iconified) {

    }

    @Override
    public void windowMaximize(long window, boolean maximized) {

    }

    @Override
    public void windowFramebufferSize(long window, int width, int height) {
        this.displayWidth = width;
        this.displayHeight = height;
        GL11.glViewport(0, 0, width, height);
        setScreen(currentScreen);
    }

    @Override
    public void windowContentScale(long l, float v, float v1) {

    }

    @Override
    public void key(long window, int key, int scancode, int action, int mods) {
        if (key == Glfw.GLFW_KEY_LEFT_SHIFT)
            return;
        // Set up dummy lwjgl states
        currentKeyboardButtonState = action >= Glfw.GLFW_PRESS;
        currentKeyboardButton = key;
        currentKeyboardButtonModifiers = mods;

        delegateToCharCallback = key >= Glfw.GLFW_KEY_APOSTROPHE && key <= Glfw.GLFW_KEY_GRAVE_ACCENT;
        if (delegateToCharCallback) {
            return;
        }
        currentKeyboardButtonCharacter = getCharacter();
        handleKeyInput(key, action);
    }

    public char getCharacter() {
        int keyboardButton = currentKeyboardButton;
        int modifier = currentKeyboardButtonModifiers;
        String keyName = Glfw.glfwGetKeyName(keyboardButton, Glfw.glfwGetScancode(keyboardButton));
        char keyChar = (keyName != null ? keyName.charAt(0) : (keyboardButton == Glfw.GLFW_KEY_SPACE) ? ' ' : '\0');
//        if ((modifier & Glfw.GLFW_MOD_CONTROL) > 0 && keyboardButton == Glfw.GLFW_KEY_V) {
//            return 22;
//        }
//        if ((modifier & Glfw.GLFW_MOD_SHIFT) > 0) {
//            if (true)
//                keyChar = LwjglToGlfwHelper.translateKeyWithShift(keyboardButton);
//        }

        return keyChar;
    }

    @Override
    public void character(long window, int codepoint) {
        if (delegateToCharCallback) {
            int modifier = currentKeyboardButtonModifiers;
            if ((modifier & Glfw.GLFW_MOD_CONTROL) > 0 && currentKeyboardButton == Glfw.GLFW_KEY_V) {
                currentKeyboardButtonCharacter = 22;
            } else
                currentKeyboardButtonCharacter = (char) codepoint;
            handleKeyInput(currentKeyboardButton, currentKeyboardButtonState ? 1 : 0);
        }
    }

    @Override
    public void characterMods(long l, int i, int i1) {
    }

    @Override
    public void drop(long l, String[] strings) {

    }

    @Override
    public void mouseButton(long window, int button, boolean pressed, int mods) {
        currentMouseButtonState = pressed;
        currentMouseButton = button;

        handleMouseButton();
    }

    public int getMouseDX() {
        int temp = this.mouseDX;
        this.mouseDX = 0;
        return temp;
    }

    public int getMouseDY() {
        int temp = this.mouseDY;
        this.mouseDY = 0;
        return temp;
    }

    @Override
    public void cursorPos(long window, double x, double y) {
        this.mouseLX = this.mouseX;
        this.mouseLY = this.mouseY;
        this.mouseX = (int) x;
        this.mouseY = this.displayHeight - (int) y;
        this.mouseDX += this.mouseX - this.mouseLX;
        this.mouseDY += this.mouseY - this.mouseLY;
    }

    @Override
    public void cursorEnter(long window, boolean entered) {

    }

    @Override
    public void scroll(long window, double scrollX, double scrollY) {
        handleMouseScroll((int)scrollY);
    }
    /// END GLFW CALLBACKS

    /// MINECRAFT OVERRIDES
    public void method_2102(class_447 arg) {

    }

    @Override
    public void run() {
        this.running = true;

        this.myTrimmedInit();
        LOGGER.info("Initialized!");

        try {
            long time = System.currentTimeMillis();
            int var3 = 0;
            while (this.running && !Glfw.glfwWindowShouldClose(window)) {
                try {
                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

                    Box.resetCacheCount();
                    Vec3d.resetCacheCount();

                    if (this.paused && this.world != null) {
                        float var4 = timer.field_2370;
                        timer.method_1853();
                        timer.field_2370 = var4;
                    } else {
                        timer.method_1853();
                    }

                    long nanoTime = System.nanoTime();

                    for (int var6 = 0; var6 < timer.field_2369; ++var6) {
                        ++this.ticksPlayed;

                        try {
                            this.tick();
                        } catch (class_468 var16) {
                            this.world = null;
                            this.setWorld(null);
                            this.setScreen(new class_562());
                        }
                    }

                    long time_delta = System.nanoTime() - nanoTime;
                    this.logGlError("Pre render");
                    class_13.field_67 = this.options.fancyGraphics;
                    this.soundManager.method_2013(this.player, timer.field_2370);
                    GL11.glEnable(3553);
                    if (this.world != null) {
                        this.world.method_232();
                    }

                    if (this.player != null && this.player.method_1387()) {
                        this.options.thirdPerson = false;
                    }

                    if (!this.field_2821) {
                        if (this.interactionManager != null) {
                            this.interactionManager.method_1706(timer.field_2370);
                        }

                        this.field_2818.method_1844(timer.field_2370);
                    }

                    if (this.options.debugHud) {
                        this.method_2111(time_delta);
                    } else {
                        this.field_2777 = System.nanoTime();
                    }

                    this.field_2819.method_1963();
                    Thread.yield();

                    this.method_2152();
                    if (this.canvas != null && !this.fullscreen && (this.canvas.getWidth() != this.displayWidth || this.canvas.getHeight() != this.displayHeight)) {
                        this.method_2108(this.canvas.getWidth(), this.canvas.getHeight());
                    }

                    this.logGlError("Post render");
                    ++var3;

                    for (this.paused = !this.isWorldRemote() && this.currentScreen != null && this.currentScreen.shouldPause(); System.currentTimeMillis() >= time + 1000L; var3 = 0) {
                        this.debugText = var3 + " fps, " + class_66.field_230 + " chunk updates";
                        class_66.field_230 = 0;
                        time += 1000L;
                    }
                    Glfw.glfwSwapBuffers(window);
                    Glfw.glfwPollEvents();
                } catch (class_468 var18) {
                    this.world = null;
                    this.setWorld(null);
                    this.setScreen(new class_562());
                } catch (OutOfMemoryError var19) {
                    this.method_2131();
                    this.setScreen(new class_603());
                    System.gc();
                }
            } // END WHILE
        } catch (class_611 ignored) {
        } catch (Throwable var21) {
            this.method_2131();
            var21.printStackTrace();
            this.method_2126(new class_447("Unexpected error", var21));
        } finally {
            this.stop();
        }

    }

    // Is this the mojang logo?
    void myMethod_2150() {
        class_564 var1 = new class_564(this.options, this.displayWidth, this.displayHeight);
        GL11.glClear(16640);
        GL11.glMatrixMode(5889);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0, var1.field_2389, var1.field_2390, 0.0, 1000.0, 3000.0);
        GL11.glMatrixMode(5888);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
        GL11.glViewport(0, 0, this.displayWidth, this.displayHeight);
        GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        Tessellator var2 = Tessellator.INSTANCE;
        GL11.glDisable(2896);
        GL11.glEnable(3553);
        GL11.glDisable(2912);
        GL11.glBindTexture(3553, this.textureManager.getTextureId("/title/mojang.png"));
        var2.startQuads();
        var2.color(16777215);
        var2.vertex(0.0, this.displayHeight, 0.0, 0.0, 0.0);
        var2.vertex(this.displayWidth, this.displayHeight, 0.0, 0.0, 0.0);
        var2.vertex(this.displayWidth, 0.0, 0.0, 0.0, 0.0);
        var2.vertex(0.0, 0.0, 0.0, 0.0, 0.0);
        var2.draw();
        short var3 = 256;
        short var4 = 256;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        var2.color(16777215);
        this.method_2109((var1.method_1857() - var3) / 2, (var1.method_1858() - var4) / 2, 0, 0, var3, var4);
        GL11.glDisable(2896);
        GL11.glDisable(2912);
        GL11.glEnable(3008);
        GL11.glAlphaFunc(516, 0.1F);
    }

    void myTrimmedInit() {
        createWindow();

        Glfw.glfwMakeContextCurrent(window);
        Glfw.glfwShowWindow(window);

        // Pass in a fake context, so we can just use opengl.
        try {
            GLContext.useContext(new ContextHack());
        } catch (LWJGLException e) {
            throw new RuntimeException(e);
        }

        this.runDirectory = getRunDirectory();
        this.field_2792 = new class_157(new File(this.runDirectory, "saves"));
        this.options = new GameOptions(this, this.runDirectory);
        this.field_2768 = new class_303(this, this.runDirectory);
        this.textureManager = new TextureManager(this.field_2768, this.options);
        this.textRenderer = new TextRenderer(this.options, "/font/default.png", this.textureManager);
        class_279.method_972(this.textureManager.method_1092("/misc/watercolor.png"));
        class_287.method_982(this.textureManager.method_1092("/misc/grasscolor.png"));
        class_334.method_1081(this.textureManager.method_1092("/misc/foliagecolor.png"));
        this.field_2818 = new class_555(this);
        EntityRenderDispatcher.field_2489.field_2494 = new class_556(this);
        this.field_2773 = new class_96(this.session, this.runDirectory);
        Achievements.OPEN_INVENTORY.method_1042(new class_637());
        this.field_2767 = new MyMouseHelper();

        this.logGlError("Pre startup");
        GL11.glEnable(3553);
        GL11.glShadeModel(7425);
        GL11.glClearDepth(1.0);
        GL11.glEnable(2929);
        GL11.glDepthFunc(515);
        GL11.glEnable(3008);
        GL11.glAlphaFunc(516, 0.1F);
        GL11.glCullFace(1029);
        GL11.glMatrixMode(5889);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(5888);
        this.logGlError("Startup");
        this.field_2783 = new class_22();
        this.soundManager.method_2012(this.options);
        this.textureManager.method_1087(this.field_2796);
        this.textureManager.method_1087(this.field_2795);
        this.textureManager.method_1087(new class_269());
        this.textureManager.method_1087(new class_356(this));
        this.textureManager.method_1087(new class_384(this));
        this.textureManager.method_1087(new class_509());
        this.textureManager.method_1087(new class_296());
        this.textureManager.method_1087(new class_572(0));
        this.textureManager.method_1087(new class_572(1));
        this.worldRenderer = new WorldRenderer(this, this.textureManager);
        this.field_2808 = new class_75(this.world, this.textureManager);

        // Commented out try/catch, seemed unnecessary?
//            try {
        this.resourceDownloadThread = new ResourceDownloadThread(this.runDirectory, this);
        this.resourceDownloadThread.start();
//            } catch (Exception ignored) {
//            }

        this.logGlError("Post startup");
        this.inGameHud = new InGameHud(this);
        this.setScreen(this.field_2793 != null ? new ConnectScreen(this, this.field_2793, this.field_2794) : new TitleScreen());

        this.lastFpsLimit = this.options.fpsLimit;
    }

    public void handleKeyInput(int key, int action) {
        boolean pressed = action == Glfw.GLFW_PRESS;
        // Keybinds that should only respond on press
        if (pressed) {
            if (key == Glfw.GLFW_KEY_F11) {
                this.toggleFullscreen();
                return;
            }
            // Keybinds only usable in the world
            if (world != null && this.player != null && this.player.inventory != null) {
                if (currentScreen == null) {
                    if (key >= Glfw.GLFW_KEY_1 && key <= Glfw.GLFW_KEY_9) {
                        this.player.inventory.selectedSlot = key - Glfw.GLFW_KEY_1;
                        return;
                    }

                    if (key == translateKeyToGlfw(this.options.fogKey.code)) {
                        this.options.setInt(Option.RENDER_DISTANCE, !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 1 : -1);
                        return;
                    }

                    if (key == Glfw.GLFW_KEY_ESC) {
                        this.method_2135();
                        return;
                    }

                    if (key == Glfw.GLFW_KEY_T && Glfw.glfwGetKey(window, Glfw.GLFW_KEY_F3) == Glfw.GLFW_PRESS) {
                        this.forceResourceReload();
                        return;
                    }

                    if (key == Glfw.GLFW_KEY_F1) {
                        this.options.hideHud = !this.options.hideHud;
                        return;
                    }

                    if (key == Glfw.GLFW_KEY_F3) {
                        this.options.debugHud = !this.options.debugHud;
                        return;
                    }

                    if (key == Glfw.GLFW_KEY_F5) {
                        this.options.thirdPerson = !this.options.thirdPerson;
                        return;
                    }
                    if (key == Glfw.GLFW_KEY_F8) {
                        this.options.cinematicMode = !this.options.cinematicMode;
                        return;
                    }

                    if (key == translateKeyToGlfw(this.options.inventoryKey.code)) {
                        this.setScreen(new class_585(this.player));
                        return;
                    }

                    if (key == translateKeyToGlfw(this.options.dropKey.code)) {
                        this.player.dropSelectedItem();
                        return;
                    }

                    if (this.isWorldRemote() && key == translateKeyToGlfw(this.options.chatKey.code)) {
                        this.setScreen(new ChatScreen());
                        return;
                    }
                } else { // There is a screen

                }
            }
        }
        // Update gui or player movement
        if (this.currentScreen != null) {
            this.currentScreen.onKeyboardEvent();
        } else if (this.player != null) {
            this.player.method_136(Keyboard.getEventKey(), Keyboard.getEventKeyState());
        }
    }

    public void handleMouseButton() {
        if (this.currentScreen == null || this.currentScreen.field_155) {
            this.method_2110(0, this.currentScreen == null && Mouse.isButtonDown(0) && this.field_2778);
        }
        if (this.currentScreen == null) {

            if (!this.field_2778 && Mouse.getEventButtonState()) {
                this.method_2133();
            } else {
                if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
                    this.method_2107(0);
                    this.field_2798 = this.ticksPlayed;
                }

                if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState()) {
                    this.method_2107(1);
                    this.field_2798 = this.ticksPlayed;
                }

                if (Mouse.getEventButton() == 2 && Mouse.getEventButtonState()) {
                    this.method_2103();
                }
            }
        } else {
            this.currentScreen.onMouseEvent();
        }
    }

    void handleMouseScroll(int delta) {
        if (this.world != null && this.currentScreen == null && this.player != null && this.player.inventory != null)
            this.player.inventory.method_692(delta);
        if (this.options.field_1445) {
            this.options.field_1448 += (float) (delta > 0 ? 1 : -1) * 0.25F;
        }
        FabricLoader.getInstance().isModLoaded("");
    }

    @Override
    public void tick() {
        // Disable session check?
//        if (this.ticksPlayed == 6000) {
//            this.startSessionCheck();
//        }
        // Save States?
        this.field_2773.method_1996();
        this.inGameHud.tick();
        // Client rendering stuff (fog, weather)
        this.field_2818.method_1838(1.0F);

        // Chunk Things
        if (this.player != null) {
            class_51 var1 = this.world.method_259();
            if (var1 instanceof class_390) {
                class_390 var2 = (class_390) var1;
                int var3 = MathHelper.floor((float) ((int) this.player.x)) >> 4;
                int var4 = MathHelper.floor((float) ((int) this.player.z)) >> 4;
                var2.method_1242(var3, var4);
            }
        }

        // Does something with sounds
        if (!this.paused && this.world != null) {
            this.interactionManager.method_1720();
        }

        GL11.glBindTexture(3553, this.textureManager.getTextureId("/terrain.png"));
        if (!this.paused) {
            this.textureManager.method_1084();
        }

        // Player Sleep and Death Screens
        if (this.currentScreen == null && this.player != null) {
            if (this.player.health <= 0) {
                this.setScreen(null);
            } else if (this.player.method_943() && this.world != null && this.world.isRemote) {
                this.setScreen(new SleepingChatScreen());
            }
        } else if (this.currentScreen != null && this.currentScreen instanceof SleepingChatScreen && !this.player.method_943()) {
            this.setScreen(null);
        }

        // Update Screen Inputs
        if (this.currentScreen != null) {
            this.field_2787 = 10000;
            this.field_2798 = this.ticksPlayed + 10000;
            this.currentScreen.field_157.method_351();
            this.currentScreen.tick();
        }

        // TODO: fix continuous mouse input here when not looking at a block/entity
        int mouseButton = (Mouse.isButtonDown(0) ? 0 : Mouse.isButtonDown(1) ? 1 : -1);
        if (this.currentScreen == null && mouseButton >= 0 && (float) (this.ticksPlayed - this.field_2798) >= this.timer.field_2368 / 4.0F && this.field_2778) {
            this.method_2107(mouseButton);
            this.field_2798 = this.ticksPlayed;
        }
        // Break Blocks
        if (this.currentScreen == null || this.currentScreen.field_155) {
            this.method_2110(0, this.currentScreen == null && Mouse.isButtonDown(0) && this.field_2778);
        }

        // Remove mouse panning
        this.mouseLX = this.mouseX;
        this.mouseLY = this.mouseY;

        if (this.world != null) {
            if (this.player != null) {
                ++this.field_2799;
                if (this.field_2799 == 30) {
                    this.field_2799 = 0;
                    this.world.method_287(this.player);
                }
            }

            this.world.field_213 = this.options.difficulty;
            if (this.world.isRemote) {
                this.world.field_213 = 3;
            }

            if (!this.paused) {
                this.field_2818.method_1837();
            }

            if (!this.paused) {
                this.worldRenderer.method_1557();
            }

            if (!this.paused) {
                if (this.world.field_210 > 0) {
                    --this.world.field_210;
                }

                this.world.method_227();
            }

            if (!this.paused || this.isWorldRemote()) {
                this.world.method_196(this.options.difficulty > 0, true);
                this.world.method_242();
            }

            if (!this.paused && this.world != null) {
                this.world.method_294(MathHelper.floot(this.player.x), MathHelper.floot(this.player.y), MathHelper.floot(this.player.z));
            }

            if (!this.paused) {
                this.field_2808.method_320();
            }
        }

        this.lastTickTime = System.currentTimeMillis();
    }

    /// END MINECRAFT OVERRIDES
}
