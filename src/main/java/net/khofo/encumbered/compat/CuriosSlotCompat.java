package net.khofo.encumbered.compat;

import net.khofo.encumbered.server.CalculateWeight;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.function.Consumer;

public class CuriosSlotCompat {
    private CuriosSlotCompat(){

    }

    public static float getCuriosWeight(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(CuriosSlotCompat::getCuriosInventoryWeight)
                .orElse(0.0F);
    }

    private static float getCuriosInventoryWeight(ICuriosItemHandler curiosInventory) {
        float total = 0.0F;

        for (ICurioStacksHandler slotInventory : curiosInventory.getCurios().values()) {
            var stacks = slotInventory.getStacks();

            for (int slot = 0; slot < stacks.getSlots(); slot++) {
                ItemStack stack = stacks.getStackInSlot(slot);

                if (!stack.isEmpty()) {
                    total += CalculateWeight.getStackWeight(stack, 0);
                }
            }
        }

        return total;
    }

    public static void forEachCuriosStack(Player player, Consumer<ItemStack> consumer) {
        CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
            for (ICurioStacksHandler slotInventory : curiosInventory.getCurios().values()) {
                var stacks = slotInventory.getStacks();

                for (int slot = 0; slot < stacks.getSlots(); slot++) {
                    ItemStack stack = stacks.getStackInSlot(slot);

                    if (!stack.isEmpty()) {
                        consumer.accept(stack);
                    }
                }
            }
        });
    }

}
