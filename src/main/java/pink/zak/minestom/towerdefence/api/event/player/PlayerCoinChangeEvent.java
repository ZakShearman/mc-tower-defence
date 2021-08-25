package pink.zak.minestom.towerdefence.api.event.player;

import net.minestom.server.event.Event;
import pink.zak.minestom.towerdefence.model.GameUser;

public record PlayerCoinChangeEvent(GameUser gameUser, int coins) implements Event {
}
