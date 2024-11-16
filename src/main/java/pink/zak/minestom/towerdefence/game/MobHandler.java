package pink.zak.minestom.towerdefence.game;

import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.cache.DamageIndicatorCache;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.queue.QueuedEnemyMob;
import pink.zak.minestom.towerdefence.utils.TDEnvUtils;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MobHandler {
    public static DamageIndicatorCache DAMAGE_INDICATOR_CACHE; // todo spend time to not use static here.

    private final @NotNull Set<LivingTDEnemyMob> redSideMobs = ConcurrentHashMap.newKeySet();
    private final @NotNull Set<LivingTDEnemyMob> blueSideMobs = ConcurrentHashMap.newKeySet();

    private final @NotNull GameHandler gameHandler;

    private final @NotNull TowerDefenceInstance instance;
    private final @NotNull TowerMap map;

    public MobHandler(@NotNull TowerDefenceModule module, @NotNull GameHandler gameHandler) {
        this.gameHandler = gameHandler;

        this.instance = module.getInstance();
        this.map = this.instance.getTowerMap();

        DAMAGE_INDICATOR_CACHE = new DamageIndicatorCache(module);
    }

    public void spawnMob(@NotNull QueuedEnemyMob queuedMob, @NotNull GameUser user) {
        // create mob
        LivingTDEnemyMob mob = LivingTDEnemyMob.create(
                this.gameHandler, queuedMob.mob(), queuedMob.level().asInteger(),
                this.instance, this.map, user
        );

        // assign mob to a team
        Team team;
        if (!TDEnvUtils.SEND_AGAINST_SELF) team = user.getTeam() == Team.RED ? Team.BLUE : Team.RED;
        else team = user.getTeam();
        this.getMobs(team).add(mob);

        // increase income rate for the spawning user
        user.updateIncomeRate(current -> current + queuedMob.level().getSendIncomeIncrease());
    }

    public @NotNull Set<LivingTDEnemyMob> getMobs(@NotNull Team team) {
        return team == Team.RED ? this.redSideMobs : this.blueSideMobs;
    }

}
