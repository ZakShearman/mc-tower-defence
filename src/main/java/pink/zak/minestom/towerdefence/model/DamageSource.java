package pink.zak.minestom.towerdefence.model;

import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.user.GameUser;

public interface DamageSource {

    @NotNull GameUser getOwner();

}
