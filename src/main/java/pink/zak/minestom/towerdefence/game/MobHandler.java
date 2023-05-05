package pink.zak.minestom.towerdefence.game;

import dev.emortal.minestom.core.Environment;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.cache.DamageIndicatorCache;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.QueuedEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.LivingTDEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MobHandler {
    public static DamageIndicatorCache DAMAGE_INDICATOR_CACHE; // todo spend time to not use static here.

    private final @NotNull Set<LivingTDEnemyMob> redSideMobs = ConcurrentHashMap.newKeySet();
    private final @NotNull Set<LivingTDEnemyMob> blueSideMobs = ConcurrentHashMap.newKeySet();

    private final @NotNull GameHandler gameHandler;

    private final @NotNull TowerHandler towerHandler;
    private final @NotNull TowerDefenceInstance instance;
    private final @NotNull TowerMap map;

    private Task attackUpdateTask;

    public MobHandler(@NotNull TowerDefenceModule module, @NotNull GameHandler gameHandler) {
        this.gameHandler = gameHandler;

        this.towerHandler = gameHandler.getTowerHandler();
        this.instance = module.getInstance();
        this.map = this.instance.getTowerMap();

        DAMAGE_INDICATOR_CACHE = new DamageIndicatorCache(module);

        this.startUpdatingAttackingTowers();
    }

    public void spawnMob(@NotNull QueuedEnemyMob queuedEnemyMob, @NotNull GameUser spawningUser) {
        LivingTDEnemyMob mob = LivingTDEnemyMob.create(
                this.gameHandler, queuedEnemyMob.mob(), queuedEnemyMob.level().getLevel(),
                this.instance, this.map, spawningUser
        );
        Team team;
        if (Environment.isProduction()) team = spawningUser.getTeam() == Team.RED ? Team.BLUE : Team.RED;
        else team = spawningUser.getTeam();

        this.getMobs(team).add(mob);
    }

    private void startUpdatingAttackingTowers() {
        this.attackUpdateTask = MinecraftServer.getSchedulerManager()
                .buildTask(this::updateAttackingTowers)
                .repeat(10, TimeUnit.SERVER_TICK)
                .schedule();
    }

    private void updateAttackingTowers() {
        this.updateAttackingTowers(Team.RED);
        this.updateAttackingTowers(Team.BLUE);
    }

    // todo is there a way other than recalculating every time? Sure this is easy, but not great on performance
    private void updateAttackingTowers(@NotNull Team team) {
        List<LivingTDEnemyMob> distanceSortedMobs = new ArrayList<>(this.getMobs(team));
        distanceSortedMobs.sort(Comparator.comparingDouble(LivingTDEnemyMob::getTotalDistanceMoved).reversed());

        for (PlacedAttackingTower<?> tower : this.towerHandler.getTowers(team)
                .stream()
                .filter(tower -> tower instanceof PlacedAttackingTower)
                .map(tower -> (PlacedAttackingTower<?>) tower)
                .collect(Collectors.toUnmodifiableSet())
        ) {
            List<LivingTDEnemyMob> newTargets = new ArrayList<>();
            int i = 0;
            while (newTargets.size() < tower.getMaxTargets() && i < distanceSortedMobs.size()) {
                LivingTDEnemyMob enemyMob = distanceSortedMobs.get(i++); // get mob and increment i

                if (enemyMob.getEnemyMob().getIgnoredDamageTypes().contains(tower.getDamageType())) continue;

                double distance = tower.getBasePoint().distance(enemyMob.getPosition());

                if (distance < tower.getLevel().getRange() && (tower.getTower().getType().isTargetAir() || !enemyMob.getEnemyMob().isFlying()))
                    newTargets.add(enemyMob);
            }
            tower.setTargets(newTargets);
        }
    }

    public @NotNull Set<LivingTDEnemyMob> getMobs(@NotNull Team team) {
        return team == Team.RED ? this.redSideMobs : this.blueSideMobs;
    }
}
