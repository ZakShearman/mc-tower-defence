package pink.zak.minestom.towerdefence.api.event.game;

import net.minestom.server.event.Event;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.user.GameUser;

import java.util.Collection;

// todo call this event
public record GameEndEvent(Team winningTeam, Collection<GameUser> users) implements Event {
}
