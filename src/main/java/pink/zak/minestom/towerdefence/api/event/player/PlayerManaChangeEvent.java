package pink.zak.minestom.towerdefence.api.event.player;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.user.GameUser;

public record PlayerManaChangeEvent(@NotNull GameUser gameUser, int mana) implements Event {
}
