package pink.zak.minestom.towerdefence.enums;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.towers.ArcherTowerConfig;
import pink.zak.minestom.towerdefence.model.tower.config.towers.level.*;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum TowerType {
    ARCHER(TowerSize.THREE, ArcherTowerConfig::new, json -> new AttackingTowerLevel("Archer", json)),
    BLIZZARD(TowerSize.THREE, AttackingTower::new, BlizzardTowerLevel::new),
    BOMBER(TowerSize.THREE, AttackingTower::new, BomberTowerLevel::new),
    CHARITY(TowerSize.FIVE, Tower::new, CharityTowerLevel::new),
    EARTHQUAKE(TowerSize.THREE, AttackingTower::new, EarthquakeTowerLevel::new),
    LIGHTNING(TowerSize.THREE, AttackingTower::new, LightningTowerLevel::new),
    NECROMANCER(TowerSize.FIVE, AttackingTower::new, NecromancerTowerLevel::new);

    private final @NotNull TowerSize size;
    private final @NotNull BiFunction<JsonObject, Map<Integer, JsonObject>, ? extends Tower> towerFunction;
    private final @NotNull Function<JsonObject, ? extends TowerLevel> towerLevelFunction;

    TowerType(@NotNull TowerSize size, @NotNull BiFunction<JsonObject, Map<Integer, JsonObject>, ? extends Tower> towerFunction, @NotNull Function<JsonObject, ? extends TowerLevel> towerLevelFunction) {
        this.size = size;
        this.towerFunction = towerFunction;
        this.towerLevelFunction = towerLevelFunction;
    }

    public @NotNull TowerSize getSize() {
        return this.size;
    }

    public @NotNull BiFunction<JsonObject, Map<Integer, JsonObject>, ? extends Tower> getTowerFunction() {
        return this.towerFunction;
    }

    public @NotNull Function<JsonObject, ? extends TowerLevel> getTowerLevelFunction() {
        return this.towerLevelFunction;
    }

}
