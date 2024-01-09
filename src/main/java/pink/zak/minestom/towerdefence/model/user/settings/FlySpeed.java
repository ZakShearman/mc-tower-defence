package pink.zak.minestom.towerdefence.model.user.settings;

import org.jetbrains.annotations.NotNull;

public enum FlySpeed {
    SLOW(0.05f),
    NORMAL(0.075f),
    FAST(0.1f),
    SUPER_FAST(0.15f);

    private final float speed;

    FlySpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return this.speed;
    }

    public FlySpeed next() {
        int nextIndex = this.ordinal() + 1;
        if (nextIndex >= FlySpeed.values().length) // wrap-around support
            nextIndex = 0;
        return FlySpeed.values()[nextIndex];
    }

    @Override
    public @NotNull String toString() {
        return this.name().toLowerCase().replace('_', ' ') + " (" + this.getSpeed() + ")";
    }
}
