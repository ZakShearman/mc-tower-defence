package pink.zak.minestom.towerdefence.statdiff.types;

import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.statdiff.StatDiff;

import java.time.Duration;

public class DurationStatDiff extends StatDiff<Duration> {

    public DurationStatDiff(@NotNull Duration originalValue, @NotNull Duration newValue) {
        super(originalValue, newValue, DurationStatDiff::format);
    }

    /**
     * Formats a duration as x.xxs
     *
     * @param duration the duration to format
     * @return the formatted duration
     */
    private static @NotNull String format(@NotNull Duration duration) {
        return String.format("%.2fs", duration.toMillis() / 1000.0);
    }

    @Override
    public @NotNull String getDiffText() {
        Duration diff = this.newValue.minus(this.originalValue);

        if (diff.isZero()) {
            return "+0s";
        }

        if (diff.isNegative()) {
            return format(diff);
        }

        return "+" + format(diff);
    }
}
