package pink.zak.minestom.towerdefence.game;

import com.google.common.collect.Sets;
import net.minestom.server.instance.Instance;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.QueuedEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;

import java.util.Set;

public class MobHandler {
    private final Set<LivingEnemyMob> redSideMobs = Sets.newConcurrentHashSet();
    private final Set<LivingEnemyMob> blueSideMobs = Sets.newConcurrentHashSet();

    private final GameHandler gameHandler;
    private final TowerMap map;
    private Instance instance;

    public MobHandler(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
        this.map = gameHandler.getMap();
    }

    public LivingEnemyMob spawnMob(QueuedEnemyMob queuedEnemyMob, GameUser spawner) {
        LivingEnemyMob mob = LivingEnemyMob.createMob(queuedEnemyMob.mob(), queuedEnemyMob.level().level(), this.instance, this.map, spawner);
        if (spawner.getTeam() == Team.RED)
            this.blueSideMobs.add(mob);
        else
            this.redSideMobs.add(mob);
        return mob;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }
}
