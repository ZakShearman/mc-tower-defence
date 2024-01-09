package pink.zak.minestom.towerdefence.upgrade;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMobLevel;
import pink.zak.minestom.towerdefence.model.user.GameUser;

public final class UpgradeHandler {

    private final @NotNull Map<EnemyMob, EnemyMobLevel> mobs = new HashMap<>();

    private final @NotNull GameUser user;

    public UpgradeHandler(@NotNull GameUser user, @NotNull Set<EnemyMob> defaultUnlocks) {
        this.user = user;
        for (EnemyMob mob : defaultUnlocks) this.mobs.put(mob, mob.getLevel(1));
    }

    public boolean has(@NotNull EnemyMob mob) {
        return this.mobs.containsKey(mob);
    }

    public boolean has(@NotNull EnemyMob mob, @NotNull EnemyMobLevel level) {
        return this.getLevel(mob).map(EnemyMobLevel::getLevel)
                .map(value -> value >= level.getLevel())
                .orElse(false);
    }

    public @NotNull Optional<EnemyMobLevel> getLevel(@NotNull EnemyMob mob) {
        return Optional.ofNullable(this.mobs.get(mob));
    }

    public boolean upgrade(@NotNull EnemyMob mob, @NotNull EnemyMobLevel level) {
        return this.upgrade(mob, level, false);
    }

    public boolean upgrade(@NotNull EnemyMob mob, @NotNull EnemyMobLevel level, boolean free) {
        if (!free) {
            // calculate cost of upgrade
            int cost = this.getCost(mob, level);

            // check if user can afford upgrade
            if (this.user.getCoins() < cost) return false;

            // charge user for upgrade
            this.user.updateCoins(balance -> balance - cost);
        }
        this.mobs.put(mob, level);
        return true;
    }

    public int getCost(@NotNull EnemyMob mob, @NotNull EnemyMobLevel level) {
        int currentLevel = this.getLevel(mob)
                .map(EnemyMobLevel::getLevel)
                .orElse(0);
        int cost = 0;
        for (int i = currentLevel + 1; i <= level.getLevel(); i++) {
            EnemyMobLevel l = mob.getLevel(i);
            if (l == null) throw new IllegalStateException("Mob " + mob.getCommonName() + " is missing level " + i);
            cost += l.getUnlockCost();
        }
        return cost;
    }

}
