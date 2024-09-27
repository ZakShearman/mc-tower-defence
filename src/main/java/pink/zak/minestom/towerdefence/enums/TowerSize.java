package pink.zak.minestom.towerdefence.enums;

import org.jetbrains.annotations.NotNull;

public enum TowerSize {

    THREE(3, 1),
    FIVE(5, 2);

    private final int numericalValue;
    private final int checkDistance;

    TowerSize(int numericalValue, int checkDistance) {
        this.numericalValue = numericalValue;
        this.checkDistance = checkDistance;
    }

    public int getNumericalValue() {
        return numericalValue;
    }

    public int getCheckDistance() {
        return this.checkDistance;
    }

    public @NotNull String getFormattedName() {
        return this.numericalValue + "x" + this.numericalValue;
    }

}