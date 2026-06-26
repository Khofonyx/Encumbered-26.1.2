package net.khofo.encumbered.client.UI;

import com.mojang.blaze3d.platform.cursor.CursorType;
import net.khofo.encumbered.ClientConfig;
import net.khofo.encumbered.Encumbered;
import net.khofo.encumbered.client.ClientEncumberedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

import static java.lang.Math.clamp;

// This class renders the Weight Overlay in the player's menu screen.
@EventBusSubscriber(modid = Encumbered.MOD_ID, value = Dist.CLIENT)
public class InventoryWeightOverlay {

    private static final CursorType POINTING_HAND =
            CursorType.createStandardCursor(
                    GLFW.GLFW_HAND_CURSOR,
                    "pointing_hand",
                    CursorType.DEFAULT
            );
    private static Integer x = null;
    private static Integer y = null;

    private static int displayMode = 0;
    private static boolean draggedSinceClick = false;

    private static int lastWeightConfigX = Integer.MIN_VALUE;
    private static int lastWeightConfigY = Integer.MIN_VALUE;

    private static boolean dragging = false;
    private static int dragOffsetX = 0;
    private static int dragOffsetY = 0;

    private static float xPercent = -1.0F;
    private static float yPercent = -1.0F;

    private static int lastScreenWidth = -1;
    private static int lastScreenHeight = -1;

    private static Integer anvilX = null;
    private static Integer anvilY = null;

    private static final int BOX_WIDTH = 56;
    private static final int BOX_HEIGHT = 18;

    private static final int ANVIL_WIDTH = 12;
    private static final int ANVIL_HEIGHT = 12;

    private static final Identifier KG_BOX = Identifier.fromNamespaceAndPath(Encumbered.MOD_ID, "weight/kg_box");
    private static final Identifier LB_BOX = Identifier.fromNamespaceAndPath(Encumbered.MOD_ID, "weight/lb_box");

    private static final Identifier KG_BOX_TH1 = Identifier.fromNamespaceAndPath(Encumbered.MOD_ID, "weight/kg_box_yellow");
    private static final Identifier KG_BOX_TH2 = Identifier.fromNamespaceAndPath(Encumbered.MOD_ID, "weight/kg_box_red");
    private static final Identifier LB_BOX_TH1 = Identifier.fromNamespaceAndPath(Encumbered.MOD_ID, "weight/lb_box_yellow");
    private static final Identifier LB_BOX_TH2 = Identifier.fromNamespaceAndPath(Encumbered.MOD_ID, "weight/lb_box_red");
    private static final Identifier ANVIL_GREY = Identifier.fromNamespaceAndPath(Encumbered.MOD_ID, "weight/anvil_grey");
    private static final Identifier ANVIL_YELLOW = Identifier.fromNamespaceAndPath(Encumbered.MOD_ID, "weight/anvil_yellow");
    private static final Identifier ANVIL_RED = Identifier.fromNamespaceAndPath(Encumbered.MOD_ID, "weight/anvil_red");

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!shouldShowWeightBox(minecraft)) {
            return;
        }
        if (minecraft.player == null || minecraft.player.isCreative() || minecraft.player.isSpectator() || ClientConfig.HIDE_WEIGHT_INDICATOR.get()) {
            return;
        }

        // This is needed to place the weight indicator UI in the middle of the screen upon initial load.
        int weightConfigX = ClientConfig.INVENTORY_WEIGHT_INDICATOR_X.get();
        int weightConfigY = ClientConfig.INVENTORY_WEIGHT_INDICATOR_Y.get();

        boolean weightConfigChanged =
                weightConfigX != lastWeightConfigX || weightConfigY != lastWeightConfigY;

        // This is needed to place the weight indicator UI in the middle of the screen
        // upon initial load, or when the config values are changed in-game.
        if (x == null || y == null || weightConfigChanged) {
            x = (event.getScreen().width / 2) - 28 + weightConfigX;
            y = 60 + weightConfigY;

            saveWeightPositionPercent(event.getScreen().width, event.getScreen().height);

            lastWeightConfigX = weightConfigX;
            lastWeightConfigY = weightConfigY;
        }

        float displayWeight = switch (displayMode) {
            case 1 -> ClientEncumberedData.getTh1();
            case 2 -> ClientEncumberedData.getTh2();
            default -> ClientEncumberedData.getWeight();
        };

        Component text = Component.literal(formatWeight(displayWeight));

        updatePositionsForScreenSize(event.getScreen().width, event.getScreen().height);

        // Make sure the x and y values are not outside of the screen bounds before rendering
        clampToScreen(event.getScreen().width, event.getScreen().height);
        var graphics = event.getGuiGraphics();

        boolean hoveringBox = isMouseOverBox(event.getMouseX(), event.getMouseY());

        if (hoveringBox || dragging) {
            graphics.requestCursor(POINTING_HAND);
            graphics.fill(RenderPipelines.GUI, x, y, x + BOX_WIDTH, y + BOX_HEIGHT, 0x33FFFFFF);
            graphics.fill(RenderPipelines.GUI, x, y, x + BOX_WIDTH, y + 1, 0xFFFFFFFF);
            graphics.fill(RenderPipelines.GUI, x, y + BOX_HEIGHT - 1, x + BOX_WIDTH, y + BOX_HEIGHT, 0xFFFFFFFF);
            graphics.fill(RenderPipelines.GUI, x, y, x + 1, y + BOX_HEIGHT, 0xFFFFFFFF);
            graphics.fill(RenderPipelines.GUI, x + BOX_WIDTH - 1, y, x + BOX_WIDTH, y + BOX_HEIGHT, 0xFFFFFFFF);
        }

        // Render the black box behind the text
        graphics.fill(RenderPipelines.GUI, x + 4, y + 4, x + 38, y + 14, 0xFF000000);

        // render the text containing the weight
        int color = switch (displayMode) {
            case 1 -> 0xFFFFFF00;
            case 2 -> 0xFFFF0000;
            default -> switch (ClientEncumberedData.getLevel()) {
                case 1 -> 0xFFFFFF00;
                case 2 -> 0xFFFF0000;
                default -> 0xFFFFFFFF;
            };
        };
        int textX = x + 5;
        if (displayMode == 1 || displayMode == 2) {
            textX = x + 21 - (minecraft.font.width(text) / 2);
        }

        graphics.text(minecraft.font, text, textX, y + 5, color, true);

        // Render the lbs or kgs png.
        Identifier boxTexture = getBoxTexture();

        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                boxTexture,
                x,
                y,
                BOX_WIDTH,
                BOX_HEIGHT
        );

    }


    @SubscribeEvent
    public static void onHudRender(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.player.isCreative() || minecraft.player.isSpectator() || ClientConfig.HIDE_ANVIL_ICON.get()) {
            return;
        }

        var graphics = event.getGuiGraphics();
        renderAnvilIcon(
                graphics,
                minecraft.getWindow().getGuiScaledWidth(),
                minecraft.getWindow().getGuiScaledHeight()
        );
    }

    private static void updatePositionsForScreenSize(int screenWidth, int screenHeight) {
        if (screenWidth <= 0 || screenHeight <= 0) {
            return;
        }

        if (lastScreenWidth == -1 || lastScreenHeight == -1) {
            lastScreenWidth = screenWidth;
            lastScreenHeight = screenHeight;
            return;
        }

        if (lastScreenWidth == screenWidth && lastScreenHeight == screenHeight) {
            return;
        }

        if (x != null && y != null && xPercent >= 0.0F && yPercent >= 0.0F) {
            x = Math.round(xPercent * screenWidth);
            y = Math.round(yPercent * screenHeight);
            clampToScreen(screenWidth, screenHeight);
        }

        lastScreenWidth = screenWidth;
        lastScreenHeight = screenHeight;
    }

    private static void saveWeightPositionPercent(int screenWidth, int screenHeight) {
        if (x == null || y == null || screenWidth <= 0 || screenHeight <= 0) {
            return;
        }

        xPercent = (float) x / (float) screenWidth;
        yPercent = (float) y / (float) screenHeight;
    }

    private static Identifier getBoxTexture() {
        boolean useKgs = ClientConfig.USE_KGS.get();

        return switch (displayMode) {
            case 1 -> useKgs ? KG_BOX_TH1 : LB_BOX_TH1;
            case 2 -> useKgs ? KG_BOX_TH2 : LB_BOX_TH2;
            default -> useKgs ? KG_BOX : LB_BOX;
        };
    }

    private static void renderAnvilIcon(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight) {
        updateAnvilPosition(screenWidth, screenHeight);

        int drawX = anvilX;
        int drawY = anvilY + getAnvilStompOffset();

        drawX = clamp(drawX, 0, screenWidth - ANVIL_WIDTH);
        drawY = clamp(drawY, 0, screenHeight - ANVIL_HEIGHT);

        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                getAnvilIcon(),
                drawX,
                drawY,
                ANVIL_WIDTH,
                ANVIL_HEIGHT
        );
    }

    private static boolean shouldShowWeightBox(Minecraft minecraft) {
        if (minecraft.screen == null) {
            return false;
        }

        return minecraft.screen instanceof AbstractContainerScreen<?>;
    }

    private static Identifier getAnvilIcon() {
        return switch (ClientEncumberedData.getLevel()) {
            case 1 -> ANVIL_YELLOW;
            case 2 -> ANVIL_RED;
            default -> ANVIL_GREY;
        };
    }

    // Helper method to make sure only 5 digits gets displayed at a time.
    // prevents the text from extending past the box dimensions.
    public static String formatWeight(float weight) {
        weight = Math.max(0.0F, weight);

        if (weight >= 100000.0F) {
            return "99999";
        }

        if (weight >= 1000.0F) {
            return trimZeros(String.format("%.1f", weight));
        }

        if (weight >= 100.0F) {
            return trimZeros(String.format("%.2f", weight));
        }

        if (weight >= 10.0F) {
            return trimZeros(String.format("%.3f", weight));
        }

        return trimZeros(String.format("%.4f", weight));
    }

    // Removes any trailing zeros from the weight.
    private static String trimZeros(String text) {
        while (text.contains(".") && text.endsWith("0")) {
            text = text.substring(0, text.length() - 1);
        }

        if (text.endsWith(".")) {
            text = text.substring(0, text.length() - 1);
        }

        return text;
    }

    // This event is waiting for a mount click on the screen. used to be able to drag the weight indicator box
    @SubscribeEvent
    public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (!shouldShowWeightBox(minecraft)) {
            return;
        }

        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();

        if (isMouseOverBox(mouseX, mouseY)) {
            dragging = true;
            draggedSinceClick = false;

            dragOffsetX = (int) mouseX - x;
            dragOffsetY = (int) mouseY - y;

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (dragging && !draggedSinceClick) {
            displayMode = (displayMode + 1) % 3;
            playButtonClickSound();
            event.setCanceled(true);
        }

        dragging = false;
        draggedSinceClick = false;
    }

    @SubscribeEvent
    public static void onMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (!shouldShowWeightBox(minecraft)) {
            return;
        }

        if (dragging) {
            draggedSinceClick = true;
            x = (int) event.getMouseX() - dragOffsetX;
            y = (int) event.getMouseY() - dragOffsetY;

            clampToScreen(event.getScreen().width, event.getScreen().height);
            saveWeightPositionPercent(event.getScreen().width, event.getScreen().height);

            event.setCanceled(true);
        }
    }

    // helper method to determine if the user is over top of the weight indicator box
    private static boolean isMouseOverBox(double mouseX, double mouseY) {
        if (x == null || y == null) {
            return false;
        }

        return mouseX >= x
                && mouseX <= x + BOX_WIDTH
                && mouseY >= y
                && mouseY <= y + BOX_HEIGHT;
    }

    private static void updateAnvilPosition(int screenWidth, int screenHeight) {
        anvilX = (screenWidth / 2) - (ANVIL_WIDTH / 2) + ClientConfig.ANVIL_WEIGHT_INDICATOR_X.get();
        anvilY = screenHeight - 50 + ClientConfig.ANVIL_WEIGHT_INDICATOR_Y.get();

        clampAnvilToScreen(screenWidth, screenHeight);
    }

    private static void clampAnvilToScreen(int screenWidth, int screenHeight) {
        if (anvilX == null || anvilY == null) {
            return;
        }

        anvilX = clamp(anvilX, 0, screenWidth - ANVIL_WIDTH);
        anvilY = clamp(anvilY, 0, screenHeight - ANVIL_HEIGHT);
    }

    private static void clampToScreen(int screenWidth, int screenHeight) {
        if (x == null || y == null) {
            return;
        }

        x = clamp(x, 0, screenWidth - BOX_WIDTH);
        y = clamp(y, 0, screenHeight - BOX_HEIGHT);
    }

    private static int getAnvilStompOffset() {
        // Only stomp when fully overweight.
        if (ClientEncumberedData.getLevel() != 2) {
            return 0;
        }

        long time = System.currentTimeMillis();

        // One full stomp cycle every 1200 ms.
        long cycleLength = 1200L;
        long cycleTime = time % cycleLength;

        // Pause for most of the cycle.
        if (cycleTime < 800L) {
            return 0;
        }

        // Jump up quickly: 800ms - 900ms
        if (cycleTime < 900L) {
            return -2;
        }

        // Slam down: 900ms - 970ms
        if (cycleTime < 970L) {
            return 1;
        }

        // Small rebound: 970ms - 1030ms
        if (cycleTime < 1030L) {
            return 0;
        }

        // Rest until next cycle.
        return 0;
    }

    private static void playButtonClickSound() {
       switch (displayMode) {
           case 1:
               Minecraft.getInstance().getSoundManager().play(
                       net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                               SoundEvents.ANVIL_PLACE,
                               .9F,
                               .65F
                       )
               );
               break;
           case 2:
               Minecraft.getInstance().getSoundManager().play(
                       net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                               SoundEvents.ANVIL_PLACE,
                               1F,
                               .65F
                       )
               );
               break;
           default:
               Minecraft.getInstance().getSoundManager().play(
                       net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                               SoundEvents.ANVIL_PLACE,
                               .8F,
                               .65F
                       )
               );
               break;
       };
    }
}