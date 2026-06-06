package net.khofo.encumbered.client;

import net.khofo.encumbered.Encumbered;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/*
This class applies effects to the player. This needs to get it's data from the server using networking or else it will cause problems.
 */
@EventBusSubscriber(modid = Encumbered.MOD_ID, value = Dist.CLIENT)
public class ApplyEffects {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();

        // If the player is not you (i.e. another player in the world) then don't do anything.
        if (player != Minecraft.getInstance().player) {
            return;
        }

        // If player is in creative or spectator, don't apply effects
        if (Minecraft.getInstance().player.isCreative() || Minecraft.getInstance().player.isSpectator()){
            return;
        }

        // If the player is at their threshold 1 value, then disable sprinting
        if (ClientEncumberedData.cannotSprint()) {
            Minecraft.getInstance().options.keySprint.setDown(false);
            player.setSprinting(false);
        }

        // If the player is at their threshold 2 value, then
        if (ClientEncumberedData.cannotJump()) {
            Minecraft.getInstance().options.keyJump.setDown(false);
        }
    }
}
