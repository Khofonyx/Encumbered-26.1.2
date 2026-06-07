package net.khofo.encumbered;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue USE_KGS = BUILDER
            .comment("If true, the UI displays weight in kilograms (kg). If false, it displays pounds (lb).")
            .define("use_kgs", false);

    public static final ModConfigSpec.IntValue INVENTORY_WEIGHT_INDICATOR_X = BUILDER
            .comment("Allows you to nudge the default starting position of the weight indicator in your inventory screen. positive numbers move it right, negative values move it left.")
            .defineInRange("INVENTORY_WEIGHT_INDICATOR_X", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue INVENTORY_WEIGHT_INDICATOR_Y = BUILDER
            .comment("Allows you to nudge the default starting position of the weight indicator in your inventory screen. positive numbers move it down, negative values move it up.")
            .defineInRange("INVENTORY_WEIGHT_INDICATOR_Y", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ANVIL_WEIGHT_INDICATOR_X = BUILDER
            .comment("Allows you to nudge the default starting position of the anvil weight indicator. positive numbers move it right, negative values move it left.")
            .defineInRange("ANVIL_WEIGHT_INDICATOR_X", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ANVIL_WEIGHT_INDICATOR_Y = BUILDER
            .comment("Allows you to nudge the default starting position of the anvil weight indicator. positive numbers move it down, negative values move it up.")
            .defineInRange("ANVIL_WEIGHT_INDICATOR_Y", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);

    public static final ModConfigSpec SPEC = BUILDER.build();
}