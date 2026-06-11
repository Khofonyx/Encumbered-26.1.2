package net.khofo.encumbered.client;

import net.khofo.encumbered.Encumbered;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;

/*
This method saves data about the client related to if they are encumbered or not. Has some useful helpers to access the encumbered level.
This encumbered level matches the encumbered thresholds. so level 1 is Threshold 1, etc.
 */
@EventBusSubscriber(modid = Encumbered.MOD_ID, value = Dist.CLIENT)
public class ClientEncumberedData {
    private static float weight = 0.0F;
    private static int level = 0;

    private static boolean cannotSprint = false;
    private static boolean cannotJump = false;

    public static void set(int newLevel, float newWeight, boolean newCannotSprint, boolean newCannotJump) {
        level = newLevel;
        weight = newWeight;
        cannotSprint = newCannotSprint;
        cannotJump = newCannotJump;
    }

    public static boolean cannotSprint() {
        return cannotSprint;
    }

    public static boolean cannotJump() {
        return cannotJump;
    }

    public static float getWeight() {
        return weight;
    }

    public static int getLevel() {
        return level;
    }
}