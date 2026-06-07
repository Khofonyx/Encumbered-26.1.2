package net.khofo.encumbered;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue USE_KGS = BUILDER
            .comment("If true, the UI displays weight in kilograms (kg). If false, it displays pounds (lb).")
            .define("use_kgs", false);

    public static final ModConfigSpec SPEC = BUILDER.build();
}