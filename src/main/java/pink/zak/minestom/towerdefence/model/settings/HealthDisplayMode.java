package pink.zak.minestom.towerdefence.model.settings;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public enum HealthDisplayMode {
    RAW((health, maxHealth) -> "♥ " + health),
    PERCENTAGE((health, maxHealth) -> ((int) Math.ceil(health / maxHealth * 100)) + "%");

    private final @NotNull BiFunction<Float, Integer, String> healthResolver;

    HealthDisplayMode(@NotNull BiFunction<Float, Integer, String> healthResolver) {
        this.healthResolver = healthResolver;
    }

    public @NotNull BiFunction<Float, Integer, String> getHealthResolver() {
        return this.healthResolver;
    }

    public HealthDisplayMode next() {
        int nextIndex = this.ordinal() + 1;
        if (nextIndex >= HealthDisplayMode.values().length) // wrap-around support
            nextIndex = 0;
        return HealthDisplayMode.values()[nextIndex];
    }
}
