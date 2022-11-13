package pink.zak.minestom.towerdefence.model;

import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.TDDamageType;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;

public interface DamageSource {

    @NotNull GameUser getOwningUser();

    @NotNull PlacedAttackingTower<?> getSourceTower();

    @NotNull TDDamageType getDamageType();
}
