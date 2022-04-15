package pink.zak.minestom.towerdefence.game;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.cache.DamageIndicatorCache;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.QueuedEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedAttackingTower;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MobHandler {
    public static DamageIndicatorCache DAMAGE_INDICATOR_CACHE; // todo spend time to not use static here.

    private final @NotNull Set<LivingEnemyMob> redSideMobs = ConcurrentHashMap.newKeySet();
    private final @NotNull Set<LivingEnemyMob> blueSideMobs = ConcurrentHashMap.newKeySet();

    private final @NotNull TowerDefencePlugin plugin;
    private final @NotNull GameHandler gameHandler;

    private final @NotNull TowerHandler towerHandler;
    private final @NotNull TowerMap map;
    private Instance instance;

    private Task attackUpdateTask;

    public MobHandler(@NotNull TowerDefencePlugin plugin, @NotNull GameHandler gameHandler) {
        this.plugin = plugin;
        this.gameHandler = gameHandler;

        this.towerHandler = gameHandler.getTowerHandler();
        this.map = gameHandler.getMap();

        DAMAGE_INDICATOR_CACHE = new DamageIndicatorCache(plugin);

        this.startUpdatingAttackingTowers();
    }

    public void spawnMob(@NotNull QueuedEnemyMob queuedEnemyMob, @NotNull GameUser spawner) {
        LivingEnemyMob mob = LivingEnemyMob.create(this.plugin, this.gameHandler, queuedEnemyMob.mob(), queuedEnemyMob.level().getLevel(), this.instance, this.map, spawner);
        if (spawner.getTeam() == Team.RED)
            this.redSideMobs.add(mob); // todo change later
        else
            this.redSideMobs.add(mob);
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
        List<LivingEnemyMob> distanceSortedMobs = new ArrayList<>(team == Team.RED ? this.redSideMobs : this.blueSideMobs);
        distanceSortedMobs.sort(Comparator.comparingDouble(LivingEnemyMob::getTotalDistanceMoved).reversed());

        for (PlacedAttackingTower<?> tower : (team == Team.RED ? this.towerHandler.getRedTowers() : this.towerHandler.getBlueTowers())
            .stream()
            .filter(tower -> tower instanceof PlacedAttackingTower)
            .map(tower -> (PlacedAttackingTower<?>) tower)
            .collect(Collectors.toSet())
        ) {
            List<LivingEnemyMob> newTargets = new ArrayList<>();
            int i = 0;
            while (newTargets.size() < tower.getMaxTargets() && i < distanceSortedMobs.size()) {
                LivingEnemyMob enemyMob = distanceSortedMobs.get(i);
                double distance = tower.getBasePoint().distance(enemyMob.getPosition());

                if (distance < tower.getLevel().getRange() && (tower.getTower().getType().isTargetAir() || !enemyMob.getEnemyMob().isFlying()))
                    newTargets.add(enemyMob);

                i++;
            }
            tower.setTargets(newTargets);
        }
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public @NotNull Set<LivingEnemyMob> getRedSideMobs() {
        return this.redSideMobs;
    }

    public @NotNull Set<LivingEnemyMob> getBlueSideMobs() {
        return this.blueSideMobs;
    }
}
