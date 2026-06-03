package net.khofo.encumbered;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/* This defines the following structure:
{
  "weight": 1.0
}
*/


public record ItemWeight(double weight) {
    public static final Codec<ItemWeight> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("weight").forGetter(ItemWeight::weight)
            ).apply(instance, ItemWeight::new)
    );
}
