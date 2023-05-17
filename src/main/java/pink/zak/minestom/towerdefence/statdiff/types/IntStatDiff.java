package pink.zak.minestom.towerdefence.statdiff.types;

import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.statdiff.StatDiff;

public class IntStatDiff extends StatDiff<Integer> {

    public IntStatDiff(int originalValue, int newValue) {
        super(originalValue, newValue, String::valueOf);
    }

    @Override
    public @NotNull String getDiffText() {
        int diff = this.newValue - this.originalValue;

        if (diff >= 0) {
            return "+" + diff;
        }

        return String.valueOf(diff); // -x
    }
}
