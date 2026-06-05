package net.khofo.server;

import net.khofo.encumbered.Config;
import net.khofo.encumbered.Encumbered;
import net.khofo.server.packets.EncumberedPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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

        // Creative/spectator should have no encumbrance effects
        if (player.isCreative() || player.isSpectator()) {
            allowSprinting(player);
            sendEncumbranceLevel(player, 0);
            return;
        }

        float th1 = Config.THRESHOLD_1.get().floatValue();
        float th2 = Config.THRESHOLD_2.get().floatValue();
        float playerWeight = CalculateWeight.getInventoryWeight(player);

        int level;

        // Check what weight "level" the player is at. Level is loaded into a EncumberedPayload and shipped off to the client
        if (playerWeight >= th2) {
            level = 2;
        } else if (playerWeight >= th1) {
            level = 1;
        } else {
            level = 0;
        }

        // If you're at either threshold don't allow sprinting.
        if (level >= 1) {
            disallowSprinting(player);
        } else {
            allowSprinting(player);
        }

        // Send the "level" variable to the client so they can see how encumbered the player is.
        sendEncumbranceLevel(player, level);
    }

    // Helper method to disable sprinting. Works by adding an AttributeModifier (stopSprintingModifier) to the player
    public static void disallowSprinting(Player player) {
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speed == null) {
            return;
        }

        // Only apply when the player is sprinting so that we don't get slowed down when walking.
        // If player is walking remove it.
        if (player.isSprinting()) {
            // need this if to make sure the modifier isn't already applied.
            if (speed.getModifier(AttributeModifiers.stopSprintingModifierID) == null) {
                speed.addTransientModifier(AttributeModifiers.stopSprintingModifier);
            }
        } else {
            if (speed.getModifier(AttributeModifiers.stopSprintingModifierID) != null) {
                speed.removeModifier(AttributeModifiers.stopSprintingModifierID);
            }
        }
    }

    // Helper method that allows sprinting by removing the modifier.
    public static void allowSprinting(Player player) {
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speed == null) {
            return;
        }

        if (speed.getModifier(AttributeModifiers.stopSprintingModifierID) != null) {
            speed.removeModifier(AttributeModifiers.stopSprintingModifierID);
        }
    }

    // This is a helper method to send the EncumberedPayload to the client.
    private static void sendEncumbranceLevel(Player player, int level) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(
                    serverPlayer,
                    new EncumberedPayload(level)
            );
        }
    }
}
