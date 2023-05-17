package pink.zak.minestom.towerdefence.statdiff;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public abstract class StatDiff<T> {
    protected final @NotNull T originalValue;
    protected final @NotNull T newValue;

    private final @NotNull Function<T, String> stringifier;

    protected StatDiff(@NotNull T originalValue, @NotNull T newValue, @NotNull Function<T, String> stringifier) {
        this.originalValue = originalValue;
        this.newValue = newValue;
        this.stringifier = stringifier;
    }

    public @NotNull String getOriginal() {
        return this.stringifier.apply(this.originalValue);
    }

    public @NotNull String getNew() {
        return this.stringifier.apply(this.newValue);
    }

    public abstract @NotNull String getDiffText();
}
