package net.khofo.encumbered;

/*
MAKE SURE TO DELETE THIS CLASS, THIS WAS JUST FOR TESTING.
 */
import net.khofo.encumbered.data.WeightsDataMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

@EventBusSubscriber(modid = Encumbered.MOD_ID)
public class Test {

    @SubscribeEvent
    public static void itemPickup(ItemEntityPickupEvent.Post event) {
        float weight = WeightsDataMap.getWeight(event.getOriginalStack());
        Player player = event.getPlayer();
        player.sendSystemMessage(Component.literal("Picked up weighted item: " + weight));
    }
}
