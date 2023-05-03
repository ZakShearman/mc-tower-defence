package pink.zak.minestom.towerdefence.model.user;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.api.event.player.PlayerCoinChangeEvent;
import pink.zak.minestom.towerdefence.api.event.player.PlayerManaChangeEvent;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.mob.QueuedEnemyMob;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

public class GameUser {
    private final @NotNull TDPlayer player; // todo in the future we should allow re-joining a game so this will not be final.
    private final @NotNull Team team;

    private final @NotNull Map<EnemyMob, Integer> mobLevels = new ConcurrentHashMap<>();
    private final @NotNull Queue<QueuedEnemyMob> queuedMobs = new ConcurrentLinkedQueue<>();

    private final @NotNull AtomicInteger coins = new AtomicInteger(1000000);
    private final @NotNull AtomicInteger mana = new AtomicInteger(10000);
    private final @NotNull AtomicInteger maxQueueSize = new AtomicInteger(2);

    private @Nullable Point lastClickedTowerBlock;

    public GameUser(@NotNull TDPlayer player, @NotNull Set<EnemyMob> defaultUnlocks, @NotNull Team team) {
        this.player = player;
        this.team = team;

        for (EnemyMob mob : defaultUnlocks)
            this.mobLevels.put(mob, 1);
    }

    public @NotNull TDPlayer getPlayer() {
        return this.player;
    }

    public @NotNull Team getTeam() {
        return this.team;
    }

    public @NotNull Map<EnemyMob, Integer> getMobLevels() {
        return this.mobLevels;
    }

    public int getMobLevel(EnemyMob enemyMob) {
        return this.mobLevels.getOrDefault(enemyMob, 0);
    }

    public @NotNull Queue<QueuedEnemyMob> getQueuedMobs() {
        return this.queuedMobs;
    }

    public int getCoins() {
        return this.coins.get();
    }

    public int updateAndGetCoins(@NotNull IntUnaryOperator intOperator) {
        int newCoins = this.coins.updateAndGet(intOperator);
        MinecraftServer.getGlobalEventHandler().call(new PlayerCoinChangeEvent(this, newCoins));
        return newCoins;
    }

    public int updateAndGetMana(@NotNull IntUnaryOperator intOperator) {
        int newMana = this.mana.updateAndGet(intOperator);
        MinecraftServer.getGlobalEventHandler().call(new PlayerManaChangeEvent(this, newMana));
        return newMana;
    }

    public int getMana() {
        return this.mana.get();
    }

    public @NotNull AtomicInteger getMaxQueueSize() {
        return this.maxQueueSize;
    }

    public @Nullable Point getLastClickedTowerBlock() {
        return this.lastClickedTowerBlock;
    }

    public void setLastClickedTowerBlock(@Nullable Point lastClickedTowerBlock) {
        this.lastClickedTowerBlock = lastClickedTowerBlock;
    }
}
