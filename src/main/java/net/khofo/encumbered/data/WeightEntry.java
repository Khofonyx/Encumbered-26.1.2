package net.khofo.encumbered.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// See: https://docs.neoforged.net/docs/resources/server/datamaps/

// Weight map represents an entry in the data map. i.e: "weight": 1.0
public record WeightEntry(float weight) {
    public static final Codec<WeightEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("weight").forGetter(WeightEntry::weight)).apply(instance, WeightEntry::new));
}