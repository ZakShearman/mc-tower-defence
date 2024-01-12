package pink.zak.minestom.towerdefence.api.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMobLevel;
import pink.zak.minestom.towerdefence.model.user.GameUser;

public record PlayerUpgradeMobEvent(@NotNull GameUser user, @NotNull EnemyMob mob, @Nullable EnemyMobLevel from, @NotNull EnemyMobLevel to) implements PlayerEvent {

    @Override
    public @NotNull Player getPlayer() {
        return this.user.getPlayer();
    }

}
