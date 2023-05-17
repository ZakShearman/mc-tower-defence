package pink.zak.minestom.towerdefence.model.tower.config.towers.level;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;
import pink.zak.minestom.towerdefence.statdiff.StatDiffCollection;
import pink.zak.minestom.towerdefence.statdiff.types.DoubleRateStatDiff;
import pink.zak.minestom.towerdefence.statdiff.types.DurationStatDiff;

import java.time.Duration;

public class BlizzardTowerLevel extends AttackingTowerLevel {
    private final double speedModifier;
    private final int tickDuration;

    public BlizzardTowerLevel(@NotNull JsonObject jsonObject) {
        super("Blizzard", jsonObject);

        this.speedModifier = jsonObject.get("speedModifier").getAsDouble();
        this.tickDuration = jsonObject.get("tickDuration").getAsInt();
    }

    public double getSpeedModifier() {
        return this.speedModifier;
    }

    public int getTickDuration() {
        return this.tickDuration;
    }

    @Override
    public @NotNull StatDiffCollection generateDiff(@NotNull TowerLevel uncastOther) {
        if (!(uncastOther instanceof BlizzardTowerLevel other))
            throw new IllegalArgumentException("Cannot compare BlizzardTowerLevel to " + uncastOther.getClass().getSimpleName());

        return super.generateDiff(uncastOther)
                .addDiff("Speed Modifier", new DoubleRateStatDiff(this.getSpeedModifier(), other.getSpeedModifier()))
                .addDiff("Slow Duration", new DurationStatDiff(
                        Duration.ofMillis(this.getTickDuration() * 50L), Duration.ofMillis(other.getTickDuration() * 50L)
                ));
    }
}