package pink.zak.minestom.towerdefence.model.prediction;

import org.jetbrains.annotations.NotNull;

public sealed interface DamagePrediction permits PredictionImpl {

    /**
     * Creates a new damage prediction.
     *
     * @param mob the mob to predict damage for
     * @param damage the damage predicted
     * @return the damage prediction
     */
    static @NotNull DamagePrediction create(@NotNull DamagePredictable mob, float damage) {
        return new PredictionImpl(mob, damage);
    }

    /**
     * Completes the prediction.
     */
    void complete();

    /**
     * Gets the damage predicted.
     *
     * @return the damage predicted
     */
    float damage();

}
