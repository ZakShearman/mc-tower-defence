package pink.zak.minestom.towerdefence.upgrade;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.api.event.player.PlayerUpgradeMobEvent;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMobLevel;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.utils.Result;

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
        return this.getLevel(mob).map(EnemyMobLevel::asInteger)
                .map(value -> value >= level.asInteger())
                .orElse(false);
    }

    public @NotNull Optional<EnemyMobLevel> getLevel(@NotNull EnemyMob mob) {
        return Optional.ofNullable(this.mobs.get(mob));
    }

    public @NotNull Result<MobUpgradeFailureReason> unlock(@NotNull EnemyMob mob) {
        //noinspection DataFlowIssue there will always be a level 1
        return this.upgrade(mob, mob.getLevel(1));
    }

    public @NotNull Result<MobUpgradeFailureReason> upgrade(@NotNull EnemyMob mob, @NotNull EnemyMobLevel level) {
        return this.upgrade(mob, level, false);
    }

    public @NotNull Result<MobUpgradeFailureReason> upgrade(@NotNull EnemyMob mob, @NotNull EnemyMobLevel level, boolean free) {
        // get current level
        EnemyMobLevel currentLevel = this.getLevel(mob).orElse(null);

        // check if the mob is already at the level
        if (currentLevel != null && currentLevel.compareTo(level) >= 0) return Result.failure(MobUpgradeFailureReason.ALREADY_AT_LEVEL);

        if (!free) {
            // calculate cost of upgrade
            int cost = this.getCost(mob, level);

            // check if user can afford upgrade
            if (this.user.getCoins() < cost) return Result.failure(MobUpgradeFailureReason.CANNOT_AFFORD);

            // charge user for upgrade
            this.user.updateCoins(balance -> balance - cost);
        }
        // update to new level
        this.mobs.put(mob, level);

        // call update event
        MinecraftServer.getGlobalEventHandler().call(new PlayerUpgradeMobEvent(
                this.user,
                mob,
                currentLevel,
                level
        ));

        return Result.success();
    }

    public int getCost(@NotNull EnemyMob mob, @NotNull EnemyMobLevel level) {
        int currentLevel = this.getLevel(mob)
                .map(EnemyMobLevel::asInteger)
                .orElse(0);

        int cost = 0;
        for (int i = currentLevel + 1; i <= level.asInteger(); i++) {
            EnemyMobLevel l = mob.getLevel(i);
            if (l == null) throw new IllegalStateException("Mob " + mob.getCommonName() + " is missing level " + i);
            cost += l.getUnlockCost();
        }

        return cost;
    }

}
