package net.khofo.encumbered;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue THRESHOLD_1 = BUILDER
            .comment("First weight threshold where you cannot sprint or dive in water.")
            .defineInRange("THRESHOLD_1", 70, 0, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue THRESHOLD_2 = BUILDER
            .comment("Second weight threshold where you cannot jump, sprint, swim, and are slowed down.")
            .defineInRange("THRESHOLD_2", 100, 0, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue DEFAULT_ITEM_WEIGHT = BUILDER
            .comment("The default weight assigned to every item not defined in the item_weights Data Map")
            .defineInRange("DEFAULT_ITEM_WEIGHT", 1, 0, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue HORSE_THRESHOLD_BOOST = BUILDER
            .comment("The amount to add to your weight thresholds for riding horses")
            .defineInRange("HORSE_THRESHOLD_BOOST", 30, -Double.MAX_VALUE, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue PIG_THRESHOLD_BOOST = BUILDER
            .comment("The amount to add to your weight thresholds for riding pigs")
            .defineInRange("PIG_THRESHOLD_BOOST", 30, -Double.MAX_VALUE, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue MULE_THRESHOLD_BOOST = BUILDER
            .comment("The amount to add to your weight thresholds for riding a mule")
            .defineInRange("MULE_THRESHOLD_BOOST", 30, -Double.MAX_VALUE, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue DONKEY_THRESHOLD_BOOST = BUILDER
            .comment("The amount to add to your weight thresholds for riding donkeys")
            .defineInRange("DONKEY_THRESHOLD_BOOST", -10, -Double.MAX_VALUE, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue LLAMA_THRESHOLD_BOOST = BUILDER
            .comment("The amount to add to your weight thresholds for riding llamas")
            .defineInRange("LLAMA_THRESHOLD_BOOST", 30, -Double.MAX_VALUE, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue CAMEL_THRESHOLD_BOOST = BUILDER
            .comment("The amount to add to your weight thresholds for riding camels")
            .defineInRange("CAMEL_THRESHOLD_BOOST", 30, -Double.MAX_VALUE, Double.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();
}
