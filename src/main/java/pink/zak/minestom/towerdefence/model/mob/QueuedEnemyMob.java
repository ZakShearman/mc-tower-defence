package pink.zak.minestom.towerdefence.model.mob;

import org.jetbrains.annotations.NotNull;

public record QueuedEnemyMob(@NotNull EnemyMob mob,
                             @NotNull EnemyMobLevel level) {
}
