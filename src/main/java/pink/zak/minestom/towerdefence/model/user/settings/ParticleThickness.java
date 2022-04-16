package pink.zak.minestom.towerdefence.model.user.settings;

public enum ParticleThickness {
    THIN(0.4),
    STANDARD(0.25),
    THICK(0.175);

    private final double spacing;

    ParticleThickness(double spacing) {
        this.spacing = spacing;
    }

    public double getSpacing() {
        return this.spacing;
    }

    public ParticleThickness next() {
        int nextIndex = this.ordinal() + 1;
        if (nextIndex >= ParticleThickness.values().length) // wrap-around support
            nextIndex = 0;
        return ParticleThickness.values()[nextIndex];
    }
}
