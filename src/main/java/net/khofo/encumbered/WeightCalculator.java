package net.khofo.encumbered;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class WeightCalculator {
    private static final double DEFAULT_ITEM_WEIGHT = 1.0;

    private WeightCalculator() {
    }

    public static double getPlayerWeight(Player player) {
        double total = 0.0;

        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            total += getStackWeight(stack);
        }

        return total;
    }

    public static double getStackWeight(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0;
        }

        ItemWeight itemWeight = stack.getItem().builtInRegistryHolder().getData(EncumberedDataMaps.ITEM_WEIGHTS);

        double singleItemWeight = itemWeight != null
                ? itemWeight.weight()
                : DEFAULT_ITEM_WEIGHT;

        return singleItemWeight * stack.getCount();
    }
}
