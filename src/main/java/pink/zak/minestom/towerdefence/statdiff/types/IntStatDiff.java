package pink.zak.minestom.towerdefence.statdiff.types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.statdiff.StatDiff;

public class IntStatDiff extends StatDiff<Integer> {

    public IntStatDiff(int originalValue, int newValue, @Nullable String prefix, @Nullable String suffix) {
        super(originalValue, newValue, String::valueOf, prefix, suffix);
    }

    public IntStatDiff(int originalValue, int newValue) {
        this(originalValue, newValue, null, null);
    }

    @Override
    public @NotNull String getDiff() {
        int diff = this.newValue - this.originalValue;

        if (diff == 0) {
            return "+0";
        }
        if (diff > 0) {
            return "+" + diff;
        }
        return String.valueOf(diff);
    }
}
