package pink.zak.minestom.towerdefence.api.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.enums.Team;

public class PlayerTeamSwitchEvent implements Event {
    private final Team joinedTeam;
    private final Team oldTeam;
    private final Player player;

    public PlayerTeamSwitchEvent(@Nullable Team joinedTeam, @Nullable Team oldTeam, @NotNull Player player) {
        this.joinedTeam = joinedTeam;
        this.oldTeam = oldTeam;
        this.player = player;
    }

    @Nullable
    public Team getJoinedTeam() {
        return this.joinedTeam;
    }

    @Nullable
    public Team getOldTeam() {
        return this.oldTeam;
    }

    @NotNull
    public Player getPlayer() {
        return this.player;
    }
}
