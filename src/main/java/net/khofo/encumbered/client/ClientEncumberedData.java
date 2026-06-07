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
    public static int level = 0;

    public static void set(int newLevel, float newWeight) {
        level = newLevel;
        weight = newWeight;
    }

    public static boolean cannotSprint() {
        return level >= 1;
    }

    public static boolean cannotJump() {
        return level >= 2;
    }

    public static float getWeight(){
        return weight;
    }

    public static int getLevel(){return level;}
}
