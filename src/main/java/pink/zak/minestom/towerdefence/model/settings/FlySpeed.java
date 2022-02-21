package pink.zak.minestom.towerdefence.model.settings;

public enum FlySpeed {
    SLOW(0.025f),
    NORMAL(0.05f),
    FAST(0.075f),
    SUPER_FAST(0.1f);

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
}
