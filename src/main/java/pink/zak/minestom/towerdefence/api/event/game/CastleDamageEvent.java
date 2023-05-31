package pink.zak.minestom.towerdefence.api.event.game;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import pink.zak.minestom.towerdefence.enums.Team;

public record CastleDamageEvent(@NotNull Team team, int damage,
                                @Range(from = 0, to = Integer.MAX_VALUE) int health) implements Event {
}
