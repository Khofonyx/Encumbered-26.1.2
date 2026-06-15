package net.khofo.encumbered.client;

import net.khofo.encumbered.Encumbered;
import net.khofo.encumbered.server.packets.EncumberedPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@EventBusSubscriber(modid = Encumbered.MOD_ID, value = Dist.CLIENT)
public class ClientNetworking {
    @SubscribeEvent
    public static void registerClientPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(
                EncumberedPayload.TYPE,
                (payload, context) -> {
                    ClientEncumberedData.set(
                            payload.level(),
                            payload.weight(),
                            payload.cannotSprint(),
                            payload.cannotJump(),
                            payload.cannotUseElytra()
                    );
                }
        );
    }
}