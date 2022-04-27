package pink.zak.minestom.towerdefence.api.event.game;

import net.minestom.server.event.Event;
import pink.zak.minestom.towerdefence.model.user.GameUser;

import java.util.Collection;

public record GameStartEvent(Collection<GameUser> users) implements Event {
}
