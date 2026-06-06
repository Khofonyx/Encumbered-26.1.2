package net.khofo.encumbered.server;

import net.khofo.encumbered.Encumbered;
import net.khofo.encumbered.data.WeightsDataMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.animal.nautilus.Nautilus;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Encumbered.MOD_ID)
public class CalculateWeight {

    /*
    This constantly calculates the players inventory weight and displays it.
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event){
        Player player = event.getEntity();
        // Checks if the code is running on the client, if it is, don't run it.
        if (player.level().isClientSide()) {
            return;
        }

        // If the player is not in creative or spectator mode, calculate the players weight and print it to the chat.
        if (!(player.isCreative() || player.isSpectator())){
            player.sendSystemMessage(Component.literal("Player Weight: " + getInventoryWeight(player)));
        }
    }

    // Calculate the player's weight (inventory, hotbar, item in left hand, item on mouse cursor, armor slots, vehicle slots)
    public static float getInventoryWeight(Player player){
        Inventory inventory = player.getInventory();
        float total = 0.0F;

        // Scan the inventory, armor slots, and left hand add all those items weights.
        for (int i = 0; i < inventory.getContainerSize(); i++){
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()){
                total += WeightsDataMap.getWeight(stack) * stack.getCount();
            }
        }

        // Add the vehicle the player is riding's weight (anything a donkey, horse, llama, mule is holding)
        total += getPlayerVehicleWeight(player);

        // Add the item attached to the mouse cursor
        ItemStack carried = player.containerMenu.getCarried();
        if (!carried.isEmpty()){
            total += WeightsDataMap.getWeight(carried) * carried.getCount();
        }

        // Return the total weight of the player.
        return total;
    }

    //Helper method to get the weight of the horse, mule, donkey, llama that the player is riding.
    public static float getPlayerVehicleWeight(Player player){
        float weight = 0f;
        if (player.getVehicle() instanceof AbstractHorse horse){
           weight = getHorseWeight(horse);
        }
        return weight;
    }

    // Gets the weight of the items the horse is holding (saddles, armor)
    public static float getHorseWeight(AbstractHorse horse) {
        float total = 0.0F;

        // Check the saddle and armor slots to see if equipped.
        ItemStack bodyArmor = horse.getItemBySlot(EquipmentSlot.BODY);
        ItemStack saddle = horse.getItemBySlot(EquipmentSlot.SADDLE);

        // If horse armor equipped, add it to total
        if (!bodyArmor.isEmpty()) {
            total += WeightsDataMap.getWeight(bodyArmor) * bodyArmor.getCount();
        }

        // if saddle is equipped, add it to total
        if (!saddle.isEmpty()) {
            total += WeightsDataMap.getWeight(saddle) * saddle.getCount();
        }

        // If the entity is an Abstract Chested Horse, then check it's chest inventory slots as well and add it to the total.
        if (horse instanceof AbstractChestedHorse chestedHorse){
            // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
            // If a mod makes an Abstract chested horse with more than 3 rows in its inventory, this will not calculate slots after the third row.
            // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
            for (int slot = 0; slot < chestedHorse.getInventoryColumns() * 3; slot++){
                ItemStack stack = chestedHorse.getInventory().getItem(slot);
                if (!stack.isEmpty()){
                    total += WeightsDataMap.getWeight(stack) * stack.getCount();
                }
            }
        }

        return total;
    }
}
