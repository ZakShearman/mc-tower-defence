package pink.zak.minestom.towerdefence.model;

import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.TDDamageType;

public interface DamageSource {

    @NotNull GameUser getOwningUser();

    @NotNull TDDamageType getDamageType();
}
