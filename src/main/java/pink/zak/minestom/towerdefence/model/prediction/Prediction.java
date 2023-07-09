package pink.zak.minestom.towerdefence.model.prediction;

import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Prediction(double damage, @Nullable Task task, @NotNull DamagePredictionHandler damagePredictable) {

    public void destroy() {
        this.damagePredictable.removePrediction(this);
    }
}
