package net.khofo.encumbered;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class ServerConfig {
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

    public static final ModConfigSpec.DoubleValue CHEST_BOAT_THRESHOLD_BOOST = BUILDER
            .comment("The amount to add to your weight thresholds for riding chest boats.")
            .defineInRange("CHEST_BOAT_THRESHOLD_BOOST", 30, -Double.MAX_VALUE, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue BOAT_THRESHOLD_BOOST = BUILDER
            .comment("The amount to add to your weight thresholds for riding normal boats.")
            .defineInRange("BOAT_THRESHOLD_BOOST", 0, -Double.MAX_VALUE, Double.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue LEVEL_1_SLOWDOWN = BUILDER
            .comment("If true, level 1 encumbrance slows the player.")
            .define("level_1_slowdown", false);

    public static final ModConfigSpec.BooleanValue LEVEL_1_DISMOUNT = BUILDER
            .comment("If true, level 1 encumbrance forces the player to dismount.")
            .define("level_1_dismount", false);

    public static final ModConfigSpec.BooleanValue LEVEL_1_SINKING = BUILDER
            .comment("If true, level 1 encumbrance makes the player sink in fluids.")
            .define("level_1_sinking", false);

    public static final ModConfigSpec.BooleanValue LEVEL_1_DISABLE_SPRINT = BUILDER
            .comment("If true, level 1 encumbrance prevents the player from sprinting.")
            .define("level_1_disable_sprint", true);

    public static final ModConfigSpec.BooleanValue LEVEL_1_DISABLE_JUMP = BUILDER
            .comment("If true, level 1 encumbrance prevents the player from jumping.")
            .define("level_1_disable_jump", false);

    public static final ModConfigSpec.BooleanValue LEVEL_1_DISABLE_ELYTRA = BUILDER
            .comment("If true, level 1 encumbrance prevents the player from using elytra.")
            .define("level_1_disable_elytra", false);

    public static final ModConfigSpec.BooleanValue LEVEL_2_SLOWDOWN = BUILDER
            .comment("If true, level 2 encumbrance slows the player.")
            .define("level_2_slowdown", true);

    public static final ModConfigSpec.BooleanValue LEVEL_2_DISMOUNT = BUILDER
            .comment("If true, level 2 encumbrance forces the player to dismount.")
            .define("level_2_dismount", true);

    public static final ModConfigSpec.BooleanValue LEVEL_2_SINKING = BUILDER
            .comment("If true, level 2 encumbrance makes the player sink in fluids.")
            .define("level_2_sinking", true);

    public static final ModConfigSpec.BooleanValue LEVEL_2_DISABLE_SPRINT = BUILDER
            .comment("If true, level 2 encumbrance prevents the player from sprinting.")
            .define("level_2_disable_sprint", true);

    public static final ModConfigSpec.BooleanValue LEVEL_2_DISABLE_JUMP = BUILDER
            .comment("If true, level 2 encumbrance prevents the player from jumping.")
            .define("level_2_disable_jump", true);

    public static final ModConfigSpec.BooleanValue LEVEL_2_DISABLE_ELYTRA = BUILDER
            .comment("If true, level 2 encumbrance prevents the player from using elytra.")
            .define("level_2_disable_elytra", true);

    public static final ModConfigSpec.IntValue NESTED_INVENTORY_DEPTH = BUILDER
            .comment("How many layers of inventories inside inventories should be counted. 0 = disabled.")
            .defineInRange("nested_inventory_depth", 3, 0, 10);

    static final ModConfigSpec SPEC = BUILDER.build();
}
