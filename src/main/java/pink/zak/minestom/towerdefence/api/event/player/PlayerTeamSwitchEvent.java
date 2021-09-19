package pink.zak.minestom.towerdefence.api.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import pink.zak.minestom.towerdefence.enums.Team;

public record PlayerTeamSwitchEvent(Team joinedTeam,
                                    Team oldTeam,
                                    Player player) implements Event {
}
