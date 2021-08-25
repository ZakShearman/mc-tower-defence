package pink.zak.minestom.towerdefence.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.mob.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.QueuedEnemyMob;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class GameUser {
    private final Player player;
    private final Team team;

    private final Map<EnemyMob, Integer> mobLevels = Maps.newConcurrentMap();
    private final Queue<QueuedEnemyMob> queuedMobs = Queues.newConcurrentLinkedQueue();

    private final AtomicInteger coins = new AtomicInteger(1000000);
    private final AtomicInteger mana = new AtomicInteger(10000);
    private final AtomicInteger maxQueueSize = new AtomicInteger(2);

    private Point lastClickedTowerBlock;

    public GameUser(Player player, Team team) {
        this.player = player;
        this.team = team;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Team getTeam() {
        return this.team;
    }

    public Map<EnemyMob, Integer> getMobLevels() {
        return this.mobLevels;
    }

    public int getMobLevel(EnemyMob enemyMob) {
        return this.mobLevels.getOrDefault(enemyMob, 0);
    }

    public Queue<QueuedEnemyMob> getQueuedMobs() {
        return this.queuedMobs;
    }

    public AtomicInteger getCoins() {
        return this.coins;
    }

    public AtomicInteger getMana() {
        return this.mana;
    }

    public AtomicInteger getMaxQueueSize() {
        return this.maxQueueSize;
    }

    public Point getLastClickedTowerBlock() {
        return this.lastClickedTowerBlock;
    }

    public void setLastClickedTowerBlock(Point lastClickedTowerBlock) {
        this.lastClickedTowerBlock = lastClickedTowerBlock;
    }
}
