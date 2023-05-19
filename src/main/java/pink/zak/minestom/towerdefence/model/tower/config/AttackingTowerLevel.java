package pink.zak.minestom.towerdefence.model.tower.config;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.statdiff.StatDiffCollection;
import pink.zak.minestom.towerdefence.statdiff.types.DurationStatDiff;
import pink.zak.minestom.towerdefence.statdiff.types.FloatStatDiff;

import java.time.Duration;

public class AttackingTowerLevel extends TowerLevel {
    private final int fireDelay;
    private final float damage;

    public AttackingTowerLevel(@NotNull String towerName, @NotNull JsonObject jsonObject) {
        super(towerName, jsonObject);
        this.fireDelay = jsonObject.get("fireDelay").getAsInt();
        this.damage = jsonObject.get("damage").getAsFloat();
    }

    public int getFireDelay() {
        return this.fireDelay;
    }

    public float getDamage() {
        return this.damage;
    }

    @Override
    public @NotNull StatDiffCollection generateDiff(@NotNull TowerLevel uncastOther) {
        if (!(uncastOther instanceof AttackingTowerLevel other))
            throw new IllegalArgumentException("Cannot compare AttackingTowerLevel to " + uncastOther.getClass().getSimpleName());

        return super.generateDiff(other)
                .addDiff("Rate", new DurationStatDiff(
                        Duration.ofMillis(this.getFireDelay() * 50L), Duration.ofMillis(other.getFireDelay() * 50L)
                ))
                .addDiff("Damage", new FloatStatDiff(this.getDamage(), other.getDamage()));
    }
}
