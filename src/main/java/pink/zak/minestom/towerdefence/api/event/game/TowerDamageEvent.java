package pink.zak.minestom.towerdefence.api.event.game;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.enums.Team;

public record TowerDamageEvent(@NotNull Team team, int damage, int health) implements Event {
}
