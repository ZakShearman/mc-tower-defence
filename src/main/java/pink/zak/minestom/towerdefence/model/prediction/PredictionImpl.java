package pink.zak.minestom.towerdefence.model.prediction;

import org.jetbrains.annotations.NotNull;

final class PredictionImpl implements DamagePrediction {

    private final @NotNull DamagePredictable mob;
    private final float damage;

    PredictionImpl(@NotNull DamagePredictable mob, float damage) {
        this.damage = damage;
        this.mob = mob;
    }

    public void complete() {
        this.mob.completeDamagePrediction(this);
    }

    @Override
    public float damage() {
        return this.damage;
    }

}
