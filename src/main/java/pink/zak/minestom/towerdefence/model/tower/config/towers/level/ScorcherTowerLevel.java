package pink.zak.minestom.towerdefence.model.tower.config.towers.level;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;
import pink.zak.minestom.towerdefence.statdiff.StatDiffCollection;
import pink.zak.minestom.towerdefence.statdiff.types.DurationStatDiff;

import java.time.Duration;

public class ScorcherTowerLevel extends AttackingTowerLevel {
    private final int tickDuration;

    public ScorcherTowerLevel(@NotNull JsonObject jsonObject) {
        super("Scorcher", jsonObject);
        this.tickDuration = jsonObject.get("tickDuration").getAsInt();
    }

    @Override
    public @NotNull StatDiffCollection generateDiff(@NotNull TowerLevel uncastOther) {
        if (!(uncastOther instanceof ScorcherTowerLevel other))
            throw new IllegalArgumentException("Cannot compare BlizzardTowerLevel to " + uncastOther.getClass().getSimpleName());

        return super.generateDiff(uncastOther)
                .addDiff("Burn Duration", new DurationStatDiff(
                        Duration.ofMillis(this.getTickDuration() * 50L), Duration.ofMillis(other.getTickDuration() * 50L)
                ));
    }

    public int getTickDuration() {
        return this.tickDuration;
    }
}
