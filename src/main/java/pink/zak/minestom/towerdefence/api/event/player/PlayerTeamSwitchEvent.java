package pink.zak.minestom.towerdefence.api.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.enums.Team;

public record PlayerTeamSwitchEvent(Team joinedTeam,
                                    Team oldTeam,
                                    Player player) implements Event {
    public PlayerTeamSwitchEvent(@Nullable Team joinedTeam, @Nullable Team oldTeam, @NotNull Player player) {
        this.joinedTeam = joinedTeam;
        this.oldTeam = oldTeam;
        this.player = player;
    }
}
