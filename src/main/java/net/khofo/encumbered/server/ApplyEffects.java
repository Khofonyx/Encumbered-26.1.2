package net.khofo.encumbered.server;

import net.khofo.encumbered.ServerConfig;
import net.khofo.encumbered.Encumbered;
import net.khofo.encumbered.server.packets.EncumberedPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.equine.*;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.AbstractChestBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
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

        // Get the player thresholds from the configs
        float th1 = ServerConfig.THRESHOLD_1.get().floatValue();
        float th2 = ServerConfig.THRESHOLD_2.get().floatValue();

        // Creative/spectator should have no encumbrance effects
        if (shouldIgnoreEncumbrance(player)) {
            sendEncumbranceLevel(player, 0, playerWeight, false, false, false,th1,th2,ServerConfig.NESTED_INVENTORY_DEPTH.get());
            togglePlayerSlowdown(player, false);
            togglePlayerJumpStrength(player, false);
            return;
        }

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
        applyConfiguredEffects(player, level);
        boolean cannotSprint = shouldDisableSprint(level);
        boolean cannotJump = shouldDisableJump(level);
        boolean cannotUseElytra = shouldDisableElytra(level);
        // Send the "level" variable to the client so they can see how encumbered the player is.
        sendEncumbranceLevel(player, level, playerWeight, cannotSprint, cannotJump, cannotUseElytra,th1,th2,ServerConfig.NESTED_INVENTORY_DEPTH.get());
    }

    private static boolean shouldDisableElytra(int level){
        return switch(level){
            case 2 -> ServerConfig.LEVEL_2_DISABLE_ELYTRA.get();
            case 1 -> ServerConfig.LEVEL_1_DISABLE_ELYTRA.get();
            default -> false;
        };
    }

    // Entity interact event used to stop a player from mounting an entity if they are over that entities carry weight.
    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        // Only care about mounting, not dismounting.
        if (!event.isMounting()) {
            return;
        }

        // Only care when the thing mounting is a player.
        if (!(event.getEntityMounting() instanceof Player player)) {
            return;
        }

        // Server only.
        if (player.level().isClientSide()) {
            return;
        }

        if (shouldIgnoreEncumbrance(player)) {
            return;
        }

        Entity vehicle = event.getEntityBeingMounted();
        if (vehicle instanceof AbstractMinecart){
            return;
        }
        float mountBoost = getMountThresholdBoost(vehicle);

        float playerWeight = CalculateWeight.getPlayerOnlyInventoryWeight(player);
        float vehicleInventoryWeight = CalculateWeight.getVehicleWeight(vehicle);

        float totalWeightAfterMounting = playerWeight + vehicleInventoryWeight;

        float maxAllowedWeight = ServerConfig.THRESHOLD_2.get().floatValue() + mountBoost;

        if (totalWeightAfterMounting >= maxAllowedWeight) {
            event.setCanceled(true);
            player.sendSystemMessage(Component.literal("You weigh too much to ride this!"));
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
        if (entity instanceof AbstractChestBoat) {
            return ServerConfig.CHEST_BOAT_THRESHOLD_BOOST.get().floatValue();
        }
        if (entity instanceof AbstractBoat) {
            return ServerConfig.BOAT_THRESHOLD_BOOST.get().floatValue();
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
    private static void sendEncumbranceLevel(
            Player player,
            int level,
            float weight,
            boolean cannotSprint,
            boolean cannotJump,
            boolean cannotUseElytra,
            float th1,
            float th2,
            int nestedInventoryDepth
    ) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(
                    serverPlayer,
                    new EncumberedPayload(level, weight, cannotSprint, cannotJump, cannotUseElytra,th1,th2,nestedInventoryDepth)
            );
        }
    }

    public static boolean shouldIgnoreEncumbrance(Player player){
        return player.isCreative() || player.isSpectator();
    }

    // Helper method that detects if a players hitbox is touching a fluid
    private static boolean isTouchingAnyFluid(Player player) {
        var level = player.level();
        var box = player.getBoundingBox();

        int minX = (int) Math.floor(box.minX);
        int maxX = (int) Math.floor(box.maxX);
        int minY = (int) Math.floor(box.minY);
        int maxY = (int) Math.floor(box.maxY);
        int minZ = (int) Math.floor(box.minZ);
        int maxZ = (int) Math.floor(box.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    var fluidState = level.getFluidState(new BlockPos(x, y, z));

                    if (!fluidState.isEmpty()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // Helper method that applies a sinking effect to the player by adding a negative force on the y axis.
    private static void applySinkingEffect(Player player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        if (!isTouchingAnyFluid(player)) {
            return;
        }

        var motion = player.getDeltaMovement();

        double x = motion.x;
        double y = motion.y;
        double z = motion.z;

        if (y > 0.0D) {
            y = 0.0D;
        }

        y -= 0.03D;

        x *= 0.6D;
        z *= 0.6D;

        player.setDeltaMovement(x, y, z);
        player.hurtMarked = true;
    }

    private static void applyConfiguredEffects(Player player, int level) {
        boolean shouldSlowdown = false;
        boolean shouldDismount = false;
        boolean shouldSink = false;
        boolean shouldDisableJump = shouldDisableJump(level);

        switch (level) {
            case 2:
                shouldSlowdown = ServerConfig.LEVEL_2_SLOWDOWN.get();
                shouldDismount = ServerConfig.LEVEL_2_DISMOUNT.get();
                shouldSink = ServerConfig.LEVEL_2_SINKING.get();
                break;

            case 1:
                shouldSlowdown = ServerConfig.LEVEL_1_SLOWDOWN.get();
                shouldDismount = ServerConfig.LEVEL_1_DISMOUNT.get();
                shouldSink = ServerConfig.LEVEL_1_SINKING.get();
                break;

            case 0:
            default:
                break;
        }

        togglePlayerSlowdown(player, shouldSlowdown);
        togglePlayerJumpStrength(player, shouldDisableJump);

        if (shouldDismount && !isRidingMinecart(player)) {
            player.stopRiding();
        }

        if (shouldDisableElytra(level)){
            player.stopFallFlying();
        }

        if (shouldSink) {
            applySinkingEffect(player);
        }
    }

    private static boolean isRidingMinecart(Player player){
        return player.getVehicle() instanceof AbstractMinecart;
    }
    private static boolean shouldDisableSprint(int level) {
        return switch (level) {
            case 2 -> ServerConfig.LEVEL_2_DISABLE_SPRINT.get();
            case 1 -> ServerConfig.LEVEL_1_DISABLE_SPRINT.get();
            default -> false;
        };
    }

    public static void togglePlayerJumpStrength(Player player, boolean toggle){
        var jump = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (jump == null){
            return;
        }

        boolean hasModifier = jump.getModifier(AttributeModifiers.playerJumpID) != null;

        if(toggle){
            if (!hasModifier){
                jump.addTransientModifier(AttributeModifiers.playerJumpModifier);
            }
        }else{
            if(hasModifier){
                jump.removeModifier(AttributeModifiers.playerJumpModifier);
            }
        }
    }

    private static boolean shouldDisableJump(int level) {
        return switch (level) {
            case 2 -> ServerConfig.LEVEL_2_DISABLE_JUMP.get();
            case 1 -> ServerConfig.LEVEL_1_DISABLE_JUMP.get();
            default -> false;
        };
    }
}
