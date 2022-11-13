package pink.zak.minestom.towerdefence.api.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.enums.Team;

public record PlayerTeamSwitchEvent(@Nullable Team newTeam,
                                    @Nullable Team oldTeam,
                                    @NotNull Player player) implements Event {
}
