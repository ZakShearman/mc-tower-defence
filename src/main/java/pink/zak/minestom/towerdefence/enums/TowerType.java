package pink.zak.minestom.towerdefence.enums;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.towers.BlizzardTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.towers.CharityTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.towers.LightningTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.towers.NecromancerTowerLevel;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public enum TowerType {
    ARCHER(Size.THREE, true, AttackingTower::new, AttackingTowerLevel::new),
    BOMBER(Size.THREE, false, AttackingTower::new, AttackingTowerLevel::new),
    BLIZZARD(Size.THREE, true, AttackingTower::new, BlizzardTowerLevel::new),
    LIGHTNING(Size.THREE, true, AttackingTower::new, LightningTowerLevel::new),
    NECROMANCER(Size.FIVE, true, AttackingTower::new, NecromancerTowerLevel::new),
    CHARITY(Size.THREE, false, Tower::new, CharityTowerLevel::new);

    private final @NotNull Size size;
    private final boolean targetAir;
    private final @NotNull BiFunction<JsonObject, Map<Integer, JsonObject>, ? extends Tower> towerFunction;
    private final @NotNull Function<JsonObject, ? extends TowerLevel> towerLevelFunction;

    TowerType(@NotNull Size size, boolean targetAir,
              @NotNull BiFunction<JsonObject, Map<Integer, JsonObject>, ? extends Tower> towerFunction, @NotNull Function<JsonObject, ? extends TowerLevel> towerLevelFunction) {
        this.size = size;
        this.targetAir = targetAir;
        this.towerFunction = towerFunction;
        this.towerLevelFunction = towerLevelFunction;
    }

    public @NotNull Size getSize() {
        return this.size;
    }

    public boolean isTargetAir() {
        return this.targetAir;
    }

    public @NotNull BiFunction<JsonObject, Map<Integer, JsonObject>, ? extends Tower> getTowerFunction() {
        return this.towerFunction;
    }

    public @NotNull Function<JsonObject, ? extends TowerLevel> getTowerLevelFunction() {
        return this.towerLevelFunction;
    }

    public enum Size {
        THREE(1),
        FIVE(2);

        private final int checkDistance;

        Size(int checkDistance) {
            this.checkDistance = checkDistance;
        }

        public int getCheckDistance() {
            return this.checkDistance;
        }
    }
}
