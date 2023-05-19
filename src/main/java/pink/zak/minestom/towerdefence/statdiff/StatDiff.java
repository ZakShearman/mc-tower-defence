package pink.zak.minestom.towerdefence.statdiff;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class StatDiff<T> {
    protected final @NotNull T originalValue;
    protected final @NotNull T newValue;

    private final @NotNull Function<T, String> stringifier;

    protected final @Nullable String prefix;
    protected final @Nullable String suffix;

    protected StatDiff(@NotNull T originalValue, @NotNull T newValue, @NotNull Function<T, String> stringifier,
                       @Nullable String prefix, @Nullable String suffix) {
        this.originalValue = originalValue;
        this.newValue = newValue;
        this.stringifier = stringifier;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    private @NotNull String getOriginal() {
        return this.stringifier.apply(this.originalValue);
    }

    public @NotNull String getFormattedOriginal() {
        StringBuilder builder = new StringBuilder();

        if (this.prefix != null) builder.append(this.prefix);
        builder.append(this.getOriginal());
        if (this.suffix != null) builder.append(this.suffix);

        return builder.toString();
    }

    private @NotNull String getNew() {
        return this.stringifier.apply(this.newValue);
    }

    public @NotNull String getFormattedNew() {
        StringBuilder builder = new StringBuilder();

        if (this.prefix != null) builder.append(this.prefix);
        builder.append(this.getNew());
        if (this.suffix != null) builder.append(this.suffix);

        return builder.toString();
    }

    public @NotNull String getFormattedDiff() {
        StringBuilder builder = new StringBuilder();

        if (this.prefix != null) builder.append(this.prefix);
        builder.append(this.getDiff());
        if (this.suffix != null) builder.append(this.suffix);

        return builder.toString();
    }

    public abstract @NotNull String getDiff();
}
