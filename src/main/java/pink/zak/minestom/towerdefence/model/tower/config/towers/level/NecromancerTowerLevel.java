package pink.zak.minestom.towerdefence.model.tower.config.towers.level;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;
import pink.zak.minestom.towerdefence.statdiff.StatDiffCollection;
import pink.zak.minestom.towerdefence.statdiff.types.IntStatDiff;

public class NecromancerTowerLevel extends TowerLevel {
    private final int maxNecromancedTroops;
    private final int necromancedHealth;
    private final int necromancedDamage;

    public NecromancerTowerLevel(@NotNull JsonObject jsonObject) {
        super("Necromancer", jsonObject);

        this.maxNecromancedTroops = jsonObject.get("maxNecromancedTroops").getAsInt();
        this.necromancedHealth = jsonObject.get("necromancedHealth").getAsInt();
        this.necromancedDamage = jsonObject.get("necromancedDamage").getAsInt();
    }

    public int getMaxNecromancedMobs() {
        return this.maxNecromancedTroops;
    }

    public int getNecromancedHealth() {
        return this.necromancedHealth;
    }

    public int getNecromancedDamage() {
        return this.necromancedDamage;
    }

    @Override
    public @NotNull StatDiffCollection generateDiff(@NotNull TowerLevel uncastOther) {
        if (!(uncastOther instanceof NecromancerTowerLevel other))
            throw new IllegalArgumentException("Cannot compare NecromancerTowerLevel to " + uncastOther.getClass().getSimpleName());

        return super.generateDiff(uncastOther)
                .addDiff("Necromanced Troops", new IntStatDiff(this.getMaxNecromancedMobs(), other.getMaxNecromancedMobs()))
                .addDiff("Troop Health", new IntStatDiff(this.getNecromancedHealth(), other.getNecromancedHealth()))
                .addDiff("Troop Damage", new IntStatDiff(this.getNecromancedDamage(), other.getNecromancedDamage()));
    }
}
