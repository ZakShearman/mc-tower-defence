package pink.zak.minestom.towerdefence.model.prediction;

import org.jetbrains.annotations.NotNull;

public interface DamagePredictable {

    /**
     * Predicts the damage this mob will take in the future.
     * Callers of this method should also complete the prediction
     * once the damage has been applied.
     *
     * @param damage the damage predicted
     * @return the prediction
     */
    @NotNull DamagePrediction applyDamagePrediction(float damage);

    /**
     * Marks the prediction as complete.
     *
     * @param prediction the prediction to complete
     */
    void completeDamagePrediction(@NotNull DamagePrediction prediction);

    /**
     * Gets the damage prediction for this mob.
     *
     * @return the damage prediction
     */
    float getDamagePrediction();

    /**
     * Gets the predicted health of this mob.
     *
     * @return the predicted health
     */
    float getPredictedHealth();

    /**
     * Whether it is predicted this mob will die soon due to a damage hold from a tower.
     *
     * @return whether this mob is predicted to die soon.
     */
    default boolean isPredictedDead() {
        return this.getPredictedHealth() <= 0;
    }
}
