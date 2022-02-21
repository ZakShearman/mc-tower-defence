package pink.zak.minestom.towerdefence.model.settings;

public enum HealthDisplayMode {
    RAW,
    PERCENTAGE;

    public HealthDisplayMode next() {
        int nextIndex = this.ordinal() + 1;
        if (nextIndex >= HealthDisplayMode.values().length) // wrap-around support
            nextIndex = 0;
        return HealthDisplayMode.values()[nextIndex];
    }
}
