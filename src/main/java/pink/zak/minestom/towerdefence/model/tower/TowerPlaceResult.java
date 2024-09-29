package pink.zak.minestom.towerdefence.model.tower;

import org.jetbrains.annotations.UnknownNullability;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;

public record TowerPlaceResult(@UnknownNullability TowerPlaceFailureReason failureReason,
                               @UnknownNullability PlacedTower<? extends TowerLevel> tower) {

    public boolean isSuccessful() {
        return this.failureReason == null;
    }

}
