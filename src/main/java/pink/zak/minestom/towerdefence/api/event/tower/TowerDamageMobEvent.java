package pink.zak.minestom.towerdefence.api.event.tower;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.DamageSource;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;

public record TowerDamageMobEvent(@NotNull DamageSource source,
                                  @NotNull LivingEnemyMob victim,
                                  float damage) implements Event {
}
