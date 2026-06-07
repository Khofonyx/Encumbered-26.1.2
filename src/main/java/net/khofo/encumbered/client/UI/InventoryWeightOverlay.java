package net.khofo.encumbered.client.UI;

import net.khofo.encumbered.ClientConfig;
import net.khofo.encumbered.Encumbered;
import net.khofo.encumbered.client.ClientEncumberedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import static java.lang.Math.clamp;

// This class renders the Weight Overlay in the player's menu screen.
@EventBusSubscriber(modid = Encumbered.MOD_ID, value = Dist.CLIENT)
public class InventoryWeightOverlay {
    private static Integer x = null;
    private static Integer y = null;

    private static boolean dragging = false;
    private static int dragOffsetX = 0;
    private static int dragOffsetY = 0;

    private static Integer anvilX = null;
    private static Integer anvilY = null;

    private static boolean draggingAnvil = false;
    private static int anvilDragOffsetX = 0;
    private static int anvilDragOffsetY = 0;

    private static final int BOX_WIDTH = 56;
    private static final int BOX_HEIGHT = 18;

    private static final int ANVIL_WIDTH = 12;
    private static final int ANVIL_HEIGHT = 12;

    private static final Identifier KG_BOX = Identifier.fromNamespaceAndPath(Encumbered.MOD_ID, "weight/kg_box");
    private static final Identifier LB_BOX = Identifier.fromNamespaceAndPath(Encumbered.MOD_ID, "weight/lb_box");
    private static final Identifier ANVIL_GREY = Identifier.fromNamespaceAndPath(Encumbered.MOD_ID, "weight/anvil_grey");
    private static final Identifier ANVIL_YELLOW = Identifier.fromNamespaceAndPath(Encumbered.MOD_ID, "weight/anvil_yellow");
    private static final Identifier ANVIL_RED = Identifier.fromNamespaceAndPath(Encumbered.MOD_ID, "weight/anvil_red");

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.player.isCreative() || minecraft.player.isSpectator()) {
            return;
        }

        // This is needed to place the weight indicator UI in the middle of the screen upon initial load.
        if (x == null && y == null) {
            x = (event.getScreen().width / 2) - 28 + ClientConfig.INVENTORY_WEIGHT_INDICATOR_X.get();
            y = 60 + ClientConfig.INVENTORY_WEIGHT_INDICATOR_Y.get();
        }

        float weight = ClientEncumberedData.getWeight();

        // build the text to be displayed.
        Component text = Component.literal(formatWeight(weight));

        // Make sure the x and y values are not outside of the screen bounds before rendering
        clampToScreen(event.getScreen().width, event.getScreen().height);
        var graphics = event.getGuiGraphics();

        // Render the black box behind the text
        graphics.fill(RenderPipelines.GUI,x+4,y+4,x+ 38,y+14,0xFF000000);

        // render the text containing the weight
        int color = 0;
        switch(ClientEncumberedData.getLevel()){
            case 1:
                color = 0xFFFFFF00;
                break;
            case 2:
                color = 0xFFFF0000;
                break;
            default:
                color = 0xFFFFFFFF;
                break;
        }
        graphics.text(minecraft.font, text, x + 5,y + 5,color, true);

        // Render the lbs or kgs png.
        Identifier boxTexture = ClientConfig.USE_KGS.get() ? KG_BOX : LB_BOX;
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                boxTexture,
                x,
                y,
                BOX_WIDTH,
                BOX_HEIGHT
        );

        // Render the anvil icon
        setInitialAnvilPositionIfNeeded(
                event.getScreen().width,
                event.getScreen().height
        );
        renderAnvilIcon(graphics, event.getScreen().width, event.getScreen().height);
    }

    @SubscribeEvent
    public static void onHudRender(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.player.isCreative() || minecraft.player.isSpectator()) {
            return;
        }

        setInitialAnvilPositionIfNeeded(
                minecraft.getWindow().getGuiScaledWidth(),
                minecraft.getWindow().getGuiScaledHeight()
        );

        var graphics = event.getGuiGraphics();

        renderAnvilIcon(
                graphics,
                minecraft.getWindow().getGuiScaledWidth(),
                minecraft.getWindow().getGuiScaledHeight()
        );
    }

    private static void renderAnvilIcon(GuiGraphicsExtractor graphics, int screenWidth, int screenHeight) {
        setInitialAnvilPositionIfNeeded(screenWidth, screenHeight);
        clampAnvilToScreen(screenWidth, screenHeight);

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

    private static Identifier getAnvilIcon() {
        return switch (ClientEncumberedData.getLevel()) {
            case 1 -> ANVIL_YELLOW;
            case 2 -> ANVIL_RED;
            default -> ANVIL_GREY;
        };
    }

    private static boolean isMouseOverAnvil(double mouseX, double mouseY) {
        if (anvilX == null || anvilY == null) {
            return false;
        }

        return mouseX >= anvilX
                && mouseX <= anvilX + ANVIL_WIDTH
                && mouseY >= anvilY
                && mouseY <= anvilY + ANVIL_HEIGHT;
    }

    // Helper method to make sure only 5 digits gets displayed at a time.
    // prevents the text from extending past the box dimensions.
    private static String formatWeight(float weight) {
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
        if (!(event.getScreen() instanceof InventoryScreen)) {
            return;
        }

        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();

        // Check anvil first
        if (isMouseOverAnvil(mouseX, mouseY)) {
            draggingAnvil = true;

            anvilDragOffsetX = (int) mouseX - anvilX;
            anvilDragOffsetY = (int) mouseY - anvilY;

            event.setCanceled(true);
            return;
        }

        // Then check weight box
        if (isMouseOverBox(mouseX, mouseY)) {
            dragging = true;

            dragOffsetX = (int) mouseX - x;
            dragOffsetY = (int) mouseY - y;

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        dragging = false;
        draggingAnvil = false;
    }

    @SubscribeEvent
    public static void onMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        if (!(event.getScreen() instanceof InventoryScreen)) {
            return;
        }

        if (draggingAnvil) {
            anvilX = (int) event.getMouseX() - anvilDragOffsetX;
            anvilY = (int) event.getMouseY() - anvilDragOffsetY;

            clampAnvilToScreen(event.getScreen().width, event.getScreen().height);

            event.setCanceled(true);
            return;
        }

        if (dragging) {
            x = (int) event.getMouseX() - dragOffsetX;
            y = (int) event.getMouseY() - dragOffsetY;

            clampToScreen(event.getScreen().width, event.getScreen().height);

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

    private static void setInitialAnvilPositionIfNeeded(int screenWidth, int screenHeight) {
        if (anvilX != null && anvilY != null) {
            return;
        }

        anvilX = (screenWidth / 2) - (ANVIL_WIDTH / 2) + ClientConfig.ANVIL_WEIGHT_INDICATOR_X.get();
        anvilY = screenHeight - 50 + ClientConfig.ANVIL_WEIGHT_INDICATOR_Y.get();
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

        // Do not animate while dragging.
        if (draggingAnvil) {
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
}