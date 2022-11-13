package pink.zak.minestom.towerdefence.model.user;

import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.enums.Team;

public class LobbyPlayer {
    private final @NotNull TDPlayer player;
    private @NotNull Team team;

    public LobbyPlayer(@NotNull TDPlayer player, @NotNull Team team) {
        this.player = player;
        this.team = team;
    }

    public @NotNull TDPlayer getPlayer() {
        return player;
    }

    public @NotNull Team getTeam() {
        return this.team;
    }

    public void setTeam(@NotNull Team team) {
        this.team = team;
    }
}
