package net.khofo.server.packets;

import io.netty.buffer.ByteBuf;
import net.khofo.encumbered.Encumbered;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/*
This class creates a packed of "encumberance" data. Essentially just sends a integer over to the client.
If the int is 1 then the player is at encumberance threshold 1
if 2 then they are at encumberance threshold 2
etc.
 */
public record EncumberedPayload(int level) implements CustomPacketPayload {
    public static final Type<EncumberedPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(
                    Encumbered.MOD_ID,
                    "encumbrance_level"
            ));

    public static final StreamCodec<ByteBuf, EncumberedPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    EncumberedPayload::level,
                    EncumberedPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
