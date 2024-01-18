package pink.zak.minestom.towerdefence.queue;

import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMobLevel;

public record QueuedEnemyMob(@NotNull EnemyMob mob, @NotNull EnemyMobLevel level) {

}
