package net.khofo.encumbered.server;

import net.khofo.encumbered.ServerConfig;
import net.khofo.encumbered.Encumbered;
import net.khofo.encumbered.server.packets.EncumberedPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.equine.*;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Encumbered.MOD_ID)
public class ApplyEffects {
    // Subscribe to the onPlayerTick event to handle all effect applications.
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Server only
        if (player.level().isClientSide()) {
            return;
        }

        // Calculate the players weight.
        float playerWeight = CalculateWeight.getInventoryWeight(player);

        // Creative/spectator should have no encumbrance effects
        if (shouldIgnoreEncumbrance(player)) {
            sendEncumbranceLevel(player, 0, playerWeight);
            togglePlayerSlowdown(player, false);
            return;
        }

        // Get the player thresholds from the configs
        float th1 = ServerConfig.THRESHOLD_1.get().floatValue();
        float th2 = ServerConfig.THRESHOLD_2.get().floatValue();

        // If you are on a vehicle, get it's boost amount from the configs and add it to the players thresholds.
        float mount_boost = getMountThresholdBoost(player.getVehicle());
        th1 += mount_boost;
        th2 += mount_boost;

        int level;

        // Check what weight "level" the player is at. Level is loaded into a EncumberedPayload and shipped off to the client
        if (playerWeight >= th2) {
            level = 2;
        } else if (playerWeight >= th1) {
            level = 1;
        } else {
            level = 0;
        }

        // Handles some server side things like slowing down the player and stoping them from riding.
        // Some other things are handled on the client by disabling the keys.
        switch (level){
            case 2:
                togglePlayerSlowdown(player, true);
                player.stopRiding();
                break;
            case 1:
                togglePlayerSlowdown(player, false);
                break;
            case 0:
                togglePlayerSlowdown(player, false);
                break;
            default:
                togglePlayerSlowdown(player, false);
                break;
        }

        // Send the "level" variable to the client so they can see how encumbered the player is.
        sendEncumbranceLevel(player, level, playerWeight);
    }

    // Entity interact event used to stop a player from mounting an entity if they are over that entities carry weight.
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }
        float mount_boost_amount = getMountThresholdBoost(event.getTarget());
        if (shouldIgnoreEncumbrance(player)){
            return;
        }
        System.out.println(mount_boost_amount);
        float playerWeight = CalculateWeight.getInventoryWeight(player);
        float th2 = ServerConfig.THRESHOLD_2.get().floatValue();
        if (playerWeight >= (mount_boost_amount + th2)) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("You weight too much!"));
        }
    }

    // helper method to get the amount a mount increases or decreases the weight carrying capacity.
    public static float getMountThresholdBoost(Entity entity){
        if(entity instanceof Horse){
            return ServerConfig.HORSE_THRESHOLD_BOOST.get().floatValue();
        }
        if(entity instanceof Pig){
            return ServerConfig.PIG_THRESHOLD_BOOST.get().floatValue();
        }
        if(entity instanceof Mule){
            return ServerConfig.MULE_THRESHOLD_BOOST.get().floatValue();
        }
        if(entity instanceof Donkey){
            return ServerConfig.DONKEY_THRESHOLD_BOOST.get().floatValue();
        }
        if(entity instanceof Llama){
            return ServerConfig.LLAMA_THRESHOLD_BOOST.get().floatValue();
        }
        if(entity instanceof Camel){
            return ServerConfig.CAMEL_THRESHOLD_BOOST.get().floatValue();
        }
        return 0f;
    }

    // apples or removes the player slowdown AttributeModifier.
    public static void togglePlayerSlowdown(Player player, boolean toggle){
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        boolean hasModifier = speed.getModifier(AttributeModifiers.playerSpeedID) != null;
        if (toggle){
            if (!hasModifier) {
                speed.addTransientModifier(AttributeModifiers.playerSpeedModifier);
            }
        }else{
            if (hasModifier) {
                speed.removeModifier(AttributeModifiers.playerSpeedModifier);
            }
        }

    }

    // This is a helper method to send the EncumberedPayload to the client.
    private static void sendEncumbranceLevel(Player player, int level, float weight) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(
                    serverPlayer,
                    new EncumberedPayload(level,weight)
            );
        }
    }

    public static boolean shouldIgnoreEncumbrance(Player player){
        return player.isCreative() || player.isSpectator();
    }

}
