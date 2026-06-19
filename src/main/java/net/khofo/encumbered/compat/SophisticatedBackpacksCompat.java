package net.khofo.encumbered.compat;

import net.khofo.encumbered.server.CalculateWeight;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;

public class SophisticatedBackpacksCompat {
    private SophisticatedBackpacksCompat() {
    }

    public static float getUpgradeWeight(ItemStack stack, int depth) {
        if (stack.isEmpty()) {
            return 0.0F;
        }

        if (!isSophisticatedBackpack(stack)) {
            return 0.0F;
        }

        IBackpackWrapper wrapper = BackpackWrapper.fromStack(stack);
        UpgradeHandler upgradeHandler = wrapper.getUpgradeHandler();

        float total = 0.0F;

        for (int slot = 0; slot < upgradeHandler.size(); slot++) {
            ItemStack upgradeStack = upgradeHandler.getStackInSlot(slot);

            if (!upgradeStack.isEmpty()) {
                total += CalculateWeight.getStackWeight(upgradeStack, depth + 1);
            }
        }

        return total;
    }

    private static boolean isSophisticatedBackpack(ItemStack stack) {
        return BuiltInRegistries.ITEM
                .getKey(stack.getItem())
                .getNamespace()
                .equals("sophisticatedbackpacks");
    }
}