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

        if (player != Minecraft.getInstance().player) {
            return;
        }

        if (ClientEncumberedData.cannotSprint()) {
            Minecraft.getInstance().options.keySprint.setDown(false);
            player.setSprinting(false);
        }
    }
}
