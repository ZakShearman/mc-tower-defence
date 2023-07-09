package pink.zak.minestom.towerdefence.model.prediction;

import org.jetbrains.annotations.NotNull;

public interface DamagePredictable {

    @NotNull DamagePredictionHandler damagePredictionHandler();

    default @NotNull Prediction addDamagePrediction(float damage) {
        return this.damagePredictionHandler().addPrediction(damage);
    }

    default @NotNull Prediction addDamagePrediction(long duration, float damage) {
        return this.damagePredictionHandler().addPrediction(duration, damage);
    }

    /**
     * Whether it is predicted this mob will die soon due to a damage hold from a tower.
     *
     * @return whether this mob is predicted to die soon.
     */
    default boolean isPredictedDead() {
        return this.damagePredictionHandler().isPredictedDead();
    }
}
