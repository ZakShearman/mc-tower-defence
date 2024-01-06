package pink.zak.minestom.towerdefence.api.event.player;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;

public record PlayerTowerPlaceEvent(@NotNull Tower tower, PlacedTower<?> placedTower,
                                    @NotNull GameUser gameUser) implements Event {
}
