package pink.zak.minestom.towerdefence.statdiff.types;

import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.statdiff.StatDiff;

import java.text.DecimalFormat;

public class FloatStatDiff extends StatDiff<Float> {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    public FloatStatDiff(@NotNull Float originalValue, @NotNull Float newValue) {
        super(originalValue, newValue, DECIMAL_FORMAT::format);
    }

    @Override
    public @NotNull String getDiffText() {
        float diff = this.newValue - this.originalValue;

        if (diff == 0) {
            return "+0";
        }
        if (diff > 0) {
            return "+" + DECIMAL_FORMAT.format(diff);
        }

        return DECIMAL_FORMAT.format(diff); // -x
    }
}
