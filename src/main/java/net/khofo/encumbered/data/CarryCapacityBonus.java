package net.khofo.encumbered.data;

public record CarryCapacityBonus(double flatBonus, double multiplierBonus) {
    public static final CarryCapacityBonus NONE = new CarryCapacityBonus(0.0, 0.0);

    public float apply(float baseThreshold) {
        double result = (baseThreshold + flatBonus) * (1.0 + multiplierBonus);
        return Math.max(0.0F, (float) result);
    }
}
