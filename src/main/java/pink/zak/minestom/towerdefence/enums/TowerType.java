package pink.zak.minestom.towerdefence.enums;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.towers.CharityTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.towers.LightningTowerLevel;

import java.util.function.Function;

public enum TowerType {
    ARCHER(Size.THREE, 10, true, AttackingTower::new, AttackingTowerLevel::new),
    BOMBER(Size.THREE, 11, false, AttackingTower::new, AttackingTowerLevel::new),
    LIGHTNING(Size.THREE, 12, true, AttackingTower::new, LightningTowerLevel::new),
    CHARITY(Size.THREE, 13, false, Tower::new, CharityTowerLevel::new);

    private final @NotNull Size size;
    private final int guiSlot;
    private final boolean targetAir;
    private final @NotNull Function<JsonObject, ? extends Tower> towerFunction;
    private final @NotNull Function<JsonObject, ? extends TowerLevel> towerLevelFunction;

    TowerType(@NotNull Size size, int guiSlot, boolean targetAir,
              @NotNull Function<JsonObject, ? extends Tower> towerFunction, @NotNull Function<JsonObject, ? extends TowerLevel> towerLevelFunction) {
        this.size = size;
        this.guiSlot = guiSlot;
        this.targetAir = targetAir;
        this.towerFunction = towerFunction;
        this.towerLevelFunction = towerLevelFunction;
    }

    public static TowerType valueOf(int guiSlot) {
        for (TowerType towerType : TowerType.values())
            if (towerType.getGuiSlot() == guiSlot)
                return towerType;
        return null;
    }

    public @NotNull Size getSize() {
        return this.size;
    }

    public int getGuiSlot() {
        return this.guiSlot;
    }

    public boolean isTargetAir() {
        return this.targetAir;
    }

    public @NotNull Function<JsonObject, ? extends Tower> getTowerFunction() {
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
