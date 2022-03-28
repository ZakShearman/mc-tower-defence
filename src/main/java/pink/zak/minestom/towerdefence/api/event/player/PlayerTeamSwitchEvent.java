package pink.zak.minestom.towerdefence.api.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.enums.Team;

public record PlayerTeamSwitchEvent(@NotNull Team newTeam,
                                    @NotNull Team oldTeam,
                                    @NotNull Player player) implements Event {
}
