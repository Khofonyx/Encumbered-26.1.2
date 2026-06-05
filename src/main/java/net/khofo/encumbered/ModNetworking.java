package net.khofo.encumbered;

import net.khofo.encumbered.server.packets.EncumberedPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Encumbered.MOD_ID)
public class ModNetworking {
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                EncumberedPayload.TYPE,
                EncumberedPayload.STREAM_CODEC
        );
    }
}
