package pink.zak.minestom.towerdefence.model;

import org.jetbrains.annotations.NotNull;

public interface OwnedEntity {

    @NotNull GameUser getOwningUser();
}
