package pink.zak.minestom.towerdefence.model.user.settings;

import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

// todo names aren't updated when the player changes their setting, only when the mob spawns or takes damage.
public enum HealthDisplayMode {
    RAW((health, maxHealth) -> "\u2665 " + Math.round(health)),
    PERCENTAGE((health, maxHealth) -> ((int) Math.ceil((health / maxHealth) * 100)) + "%");

    private final @NotNull BiFunction<Float, Float, String> healthResolver;

    HealthDisplayMode(@NotNull BiFunction<Float, Float, String> healthResolver) {
        this.healthResolver = healthResolver;
    }

    public String resolve(LivingEntity entity) {
        return this.healthResolver.apply(entity.getHealth(), entity.getMaxHealth());
    }

    public HealthDisplayMode next() {
        int nextIndex = this.ordinal() + 1;
        if (nextIndex >= HealthDisplayMode.values().length) // wrap-around support
            nextIndex = 0;
        return HealthDisplayMode.values()[nextIndex];
    }
}
