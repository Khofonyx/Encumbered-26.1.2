package net.khofo.encumbered.client.UI;

import net.khofo.encumbered.ClientConfig;
import net.khofo.encumbered.Encumbered;
import net.khofo.encumbered.client.ClientEncumberedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.awt.*;

// This class renders the Weight Overlay in the player's menu screen.
@EventBusSubscriber(modid = Encumbered.MOD_ID)
public class InventoryWeightOverlay {
    private static Integer x = null;
    private static int y = 60;

    private static boolean dragging = false;
    private static int dragOffsetX = 0;
    private static int dragOffsetY = 0;

    private static final int BOX_WIDTH = 56;
    private static final int BOX_HEIGHT = 18;

    private static final Identifier KG_BOX = Identifier.fromNamespaceAndPath(Encumbered.MOD_ID, "weight/kg_box");
    private static final Identifier LB_BOX = Identifier.fromNamespaceAndPath(Encumbered.MOD_ID, "weight/lb_box");

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        // This is needed to place the weight indicator UI in the middle of the screen upon initial load.
        if (x == null) {
            x = (event.getScreen().width / 2) - 28;
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
    }

    // Helper method to ensure the weight indicator box does not leave the bounds of the player's screen.
    private static void clampToScreen(int screenWidth, int screenHeight) {
        x = Math.clamp(x, 0, screenWidth - BOX_WIDTH);
        y = Math.clamp(y, 0, screenHeight - BOX_HEIGHT);
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

        // If the user is overtop of the indicator box, then cancel the click event
        // so that  in onMouseReleased you can set dragging to false.
        // Then in OnMouseDragged, you can move the box to that position
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
    }

    @SubscribeEvent
    public static void onMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        if (!(event.getScreen() instanceof InventoryScreen)) {
            return;
        }

        if (!dragging) {
            return;
        }

        x = (int) event.getMouseX() - dragOffsetX;
        y = (int) event.getMouseY() - dragOffsetY;

        event.setCanceled(true);
    }

    // helper method to determine if the user is over top of the weight indicator box
    private static boolean isMouseOverBox(double mouseX, double mouseY) {
        return mouseX >= x
                && mouseX <= x + BOX_WIDTH
                && mouseY >= y
                && mouseY <= y + BOX_HEIGHT;
    }
}