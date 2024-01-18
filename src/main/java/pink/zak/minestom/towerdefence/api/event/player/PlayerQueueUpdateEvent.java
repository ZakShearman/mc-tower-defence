package pink.zak.minestom.towerdefence.api.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.queue.MobQueue;

public record PlayerQueueUpdateEvent(@NotNull GameUser user, @NotNull MobQueue queue) implements PlayerEvent {

    @Override
    public @NotNull Player getPlayer() {
        return this.user.getPlayer();
    }

}
