package net.khofo.encumbered.server;

import net.khofo.encumbered.Encumbered;
import net.khofo.encumbered.ServerConfig;
import net.khofo.encumbered.data.WeightsDataMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractChestBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.khofo.encumbered.compat.SophisticatedBackpacksCompat;
import net.neoforged.fml.ModList;

@EventBusSubscriber(modid = Encumbered.MOD_ID)
public class CalculateWeight {

    // Calculate the player's weight (inventory, hotbar, item in left hand, item on mouse cursor, armor slots, vehicle slots)
    public static float getInventoryWeight(Player player) {
        return getPlayerOnlyInventoryWeight(player) + getPlayerVehicleWeight(player);
    }

    private static float getCompatUpgradeWeight(ItemStack stack, int depth) {
        if (!ModList.get().isLoaded("sophisticatedbackpacks")) {
            return 0.0F;
        }

        return SophisticatedBackpacksCompat.getUpgradeWeight(stack, depth);
    }

    public static float getPlayerOnlyInventoryWeight(Player player) {
        Inventory inventory = player.getInventory();
        float total = 0.0F;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (!stack.isEmpty()) {
                total += getStackWeight(stack, 0);
            }
        }

        ItemStack carried = player.containerMenu.getCarried();

        if (!carried.isEmpty()) {
            total += getStackWeight(carried, 0);
        }

        return total;
    }

    //Helper method to get the weight of the horse, mule, donkey, llama that the player is riding.
    public static float getPlayerVehicleWeight(Player player){
        return getVehicleWeight(player.getVehicle());
    }

    public static float getVehicleWeight(Entity vehicle) {
        float weight = 0.0F;

        if (vehicle instanceof AbstractHorse horse) {
            weight += getHorseWeight(horse);
        }

        if (vehicle instanceof AbstractChestBoat chestBoat) {
            weight += getContainerWeight(chestBoat);
        }

        return weight;
    }

    private static float getContainerWeight(Container container) {
        float total = 0.0F;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);

            if (!stack.isEmpty()) {
                total += getStackWeight(stack, 0);
            }
        }

        return total;
    }

    public static float getStackWeight(ItemStack stack, int depth) {
        if (stack.isEmpty()) {
            return 0.0F;
        }

        // Weight of the item stack itself.
        float total = WeightsDataMap.getWeight(stack) * stack.getCount();

        // Stop here if nested inventory support is disabled or too deep.
        if (depth >= ServerConfig.NESTED_INVENTORY_DEPTH.get()) {
            return total;
        }

        // Add weight of items inside this item stack.
        total += getContainedItemsWeight(stack, depth);

        return total;
    }

    private static float getContainedItemsWeight(ItemStack stack, int depth) {
        float total = 0.0F;

        float vanillaContainerWeight = getVanillaContainerWeight(stack, depth);

        if (vanillaContainerWeight >= 0.0F) {
            total += vanillaContainerWeight;
        } else {
            total += getCapabilityInventoryWeight(stack, depth);
        }

        total += getCompatUpgradeWeight(stack, depth);

        return total;
    }

    private static float getVanillaContainerWeight(ItemStack stack, int depth) {
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);

        if (contents == null) {
            return -1.0F;
        }

        float total = 0.0F;

        for (ItemStackTemplate template : contents.nonEmptyItems()) {
            ItemStack containedStack = new ItemStack(
                    template.item(),
                    template.count(),
                    template.components()
            );

            total += getStackWeight(containedStack, depth + 1);
        }

        return total;
    }

    private static float getCapabilityInventoryWeight(ItemStack stack, int depth) {
        ResourceHandler<ItemResource> handler =
                ItemAccess.forStack(stack).getCapability(Capabilities.Item.ITEM);

        if (handler == null) {
            return 0.0F;
        }

        float total = 0.0F;

        for (int slot = 0; slot < handler.size(); slot++) {
            ItemResource resource = handler.getResource(slot);

            if (resource.isEmpty()) {
                continue;
            }

            int amount = handler.getAmountAsInt(slot);

            ItemStack containedStack = resource.toStack(amount);

            if (!containedStack.isEmpty()) {
                total += getStackWeight(containedStack, depth + 1);
            }
        }

        return total;
    }

    // Gets the weight of the items the horse is holding (saddles, armor)
    public static float getHorseWeight(AbstractHorse horse) {
        float total = 0.0F;

        // Check the saddle and armor slots to see if equipped.
        ItemStack bodyArmor = horse.getItemBySlot(EquipmentSlot.BODY);
        ItemStack saddle = horse.getItemBySlot(EquipmentSlot.SADDLE);

        // If horse armor equipped, add it to total
        if (!bodyArmor.isEmpty()) {
            total += getStackWeight(bodyArmor, 0);
        }

        // if saddle is equipped, add it to total
        if (!saddle.isEmpty()) {
            total += getStackWeight(saddle, 0);
        }

        // If the entity is an Abstract Chested Horse, then check it's chest inventory slots as well and add it to the total.
        if (horse instanceof AbstractChestedHorse chestedHorse){
            total += getContainerWeight(chestedHorse.getInventory());
        }

        return total;
    }
}
