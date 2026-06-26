package net.khofo.encumbered.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BoostItem(
        BoostType boostType,
        double amount,
        boolean activeInInventory,
        boolean activeInArmor,
        boolean activeInOffhand,
        boolean activeInCurios,
        int maxStacks
) {
    public BoostItem {
        if (amount < 0.0) {
            amount = 0.0;
        }

        maxStacks = Math.max(0, maxStacks);
    }

    public enum BoostType {
        FLAT,
        MULTIPLIER;

        public static final Codec<BoostType> CODEC =
                Codec.STRING.xmap(
                        value -> switch (value.toLowerCase()) {
                            case "flat" -> FLAT;
                            case "multiplier", "percent", "percentage" -> MULTIPLIER;
                            default -> FLAT;
                        },
                        value -> switch (value) {
                            case FLAT -> "flat";
                            case MULTIPLIER -> "multiplier";
                        }
                );
    }

    public static final Codec<BoostItem> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BoostType.CODEC.optionalFieldOf("boost_type", BoostType.FLAT)
                            .forGetter(BoostItem::boostType),

                    Codec.DOUBLE.optionalFieldOf("amount", 0.0)
                            .forGetter(BoostItem::amount),

                    Codec.BOOL.optionalFieldOf("active_in_inventory", true)
                            .forGetter(BoostItem::activeInInventory),

                    Codec.BOOL.optionalFieldOf("active_in_armor", true)
                            .forGetter(BoostItem::activeInArmor),

                    Codec.BOOL.optionalFieldOf("active_in_offhand", true)
                            .forGetter(BoostItem::activeInOffhand),

                    Codec.BOOL.optionalFieldOf("active_in_curios", true)
                            .forGetter(BoostItem::activeInCurios),

                    Codec.INT.optionalFieldOf("max_stacks", 1)
                            .forGetter(BoostItem::maxStacks)
            ).apply(instance, BoostItem::new)
    );
}
