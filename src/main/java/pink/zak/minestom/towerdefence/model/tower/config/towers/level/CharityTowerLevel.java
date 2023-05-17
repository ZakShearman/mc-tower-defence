package pink.zak.minestom.towerdefence.model.tower.config.towers.level;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;
import pink.zak.minestom.towerdefence.statdiff.StatDiffCollection;
import pink.zak.minestom.towerdefence.statdiff.types.DoubleStatDiff;

public class CharityTowerLevel extends TowerLevel {
    private final double multiplier;

    public CharityTowerLevel(@NotNull JsonObject jsonObject) {
        super("Charity", jsonObject);
        this.multiplier = jsonObject.get("multiplier").getAsDouble();
    }

    public double getMultiplier() {
        return this.multiplier;
    }

    @Override
    public @NotNull StatDiffCollection generateDiff(@NotNull TowerLevel uncastOther) {
        if (!(uncastOther instanceof CharityTowerLevel other))
            throw new IllegalArgumentException("Cannot compare CharityTowerLevel to " + uncastOther.getClass().getSimpleName());

        return super.generateDiff(other)
                .addDiff("Coin Multiplier", new DoubleStatDiff(this.getMultiplier(), other.getMultiplier()));
    }
}
