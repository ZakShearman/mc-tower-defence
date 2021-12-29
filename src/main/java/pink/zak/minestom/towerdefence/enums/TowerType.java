package pink.zak.minestom.towerdefence.enums;

import com.google.gson.JsonObject;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.towers.CharityTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.towers.LightningTowerLevel;

import java.util.function.Function;

public enum TowerType {
    ARCHER(Size.THREE, 10, true, Tower::new, AttackingTowerLevel::new),
    BOMBER(Size.THREE, 11, false, Tower::new, AttackingTowerLevel::new),
    LIGHTNING(Size.THREE, 12, false, Tower::new, LightningTowerLevel::new),
    CHARITY(Size.THREE, 13, false, Tower::new, CharityTowerLevel::new);

    private final Size size;
    private final int guiSlot;
    private final boolean targetAir;
    private final Function<JsonObject, ? extends Tower> towerFunction;
    private final Function<JsonObject, ? extends TowerLevel> towerLevelFunction;

    TowerType(Size size, int guiSlot, boolean targetAir, Function<JsonObject, ? extends Tower> towerFunction, Function<JsonObject, ? extends TowerLevel> towerLevelFunction) {
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

    public Size getSize() {
        return this.size;
    }

    public int getGuiSlot() {
        return this.guiSlot;
    }

    public boolean isTargetAir() {
        return this.targetAir;
    }

    public Function<JsonObject, ? extends Tower> getTowerFunction() {
        return this.towerFunction;
    }

    public Function<JsonObject, ? extends TowerLevel> getTowerLevelFunction() {
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
