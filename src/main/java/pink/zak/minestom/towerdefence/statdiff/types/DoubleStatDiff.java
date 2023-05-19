package pink.zak.minestom.towerdefence.statdiff.types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.statdiff.StatDiff;

import java.text.DecimalFormat;

public class DoubleStatDiff extends StatDiff<Double> {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    public DoubleStatDiff(double originalValue, double newValue, @Nullable String prefix, @Nullable String suffix) {
        super(originalValue, newValue, DECIMAL_FORMAT::format, prefix, suffix);
    }

    public DoubleStatDiff(double originalValue, double newValue) {
        this(originalValue, newValue, null, null);
    }

    @Override
    public @NotNull String getDiff() {
        double diff = this.newValue - this.originalValue;

        if (diff == 0) {
            return "+0";
        }
        if (diff > 0) {
            return "+" + DECIMAL_FORMAT.format(diff);
        }

        return DECIMAL_FORMAT.format(diff); // -x
    }
}
