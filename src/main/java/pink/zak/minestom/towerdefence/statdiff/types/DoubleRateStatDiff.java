package pink.zak.minestom.towerdefence.statdiff.types;

import org.jetbrains.annotations.NotNull;

public class DoubleRateStatDiff extends DoubleStatDiff {

    public DoubleRateStatDiff(@NotNull Double originalValue, @NotNull Double newValue) {
        super(originalValue, newValue);
    }

    @Override
    public @NotNull String getOriginal() {
        return super.getOriginal() + "x";
    }

    @Override
    public @NotNull String getNew() {
        return super.getNew() + "x";
    }

    @Override
    public @NotNull String getDiffText() {
        return super.getDiffText() + "x";
    }
}
