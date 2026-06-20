package net.khofo.encumbered.client;

import net.khofo.encumbered.ClientConfig;
import net.khofo.encumbered.Encumbered;
import net.khofo.encumbered.server.CalculateWeight;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import static net.khofo.encumbered.client.UI.InventoryWeightOverlay.formatWeight;

@EventBusSubscriber(modid = Encumbered.MOD_ID, value = Dist.CLIENT)
public class WeightTooltip {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        if (stack.isEmpty()) {
            return;
        }

        float stackWeight = CalculateWeight.getStackWeight(
                stack,
                0,
                ClientEncumberedData.getNestedInventoryDepth()
        );

        String unit = ClientConfig.USE_KGS.get() ? "kg" : "lb";

        event.getToolTip().add(
                Component.literal(formatWeight(stackWeight) + " " + unit)
                        .withStyle(ChatFormatting.GRAY)
        );
    }
}
