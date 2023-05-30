package pink.zak.minestom.towerdefence.model.user;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.api.event.player.PlayerCoinChangeEvent;
import pink.zak.minestom.towerdefence.api.event.player.PlayerIncomeChangeEvent;
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
    public static final int DEFAULT_COINS = 1000;
    public static final int DEFAULT_INCOME_RATE = 50;
    public static final int DEFAULT_MAX_QUEUE_TIME = 45_000; // 45 seconds

    private final @NotNull TDPlayer player; // todo in the future we should allow re-joining a game so this will not be final.
    private final @NotNull Team team;

    private final @NotNull SendQueue queue = new SendQueue();

    private final @NotNull Map<EnemyMob, Integer> mobLevels = new ConcurrentHashMap<>();

    private final @NotNull AtomicInteger coins = new AtomicInteger(DEFAULT_COINS);

    // incomeRate is the amount of coins the player gets per 10 seconds.
    private final @NotNull AtomicInteger incomeRate = new AtomicInteger(DEFAULT_INCOME_RATE);

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

    public int getCoins() {
        return this.coins.get();
    }

    public int updateCoins(@NotNull IntUnaryOperator intOperator) {
        int newCoins = this.coins.updateAndGet(intOperator);
        MinecraftServer.getGlobalEventHandler().call(new PlayerCoinChangeEvent(this, newCoins));
        return newCoins;
    }

    public int updateIncomeRate(@NotNull IntUnaryOperator intOperator) {
        int newRate = this.incomeRate.updateAndGet(intOperator);
        MinecraftServer.getGlobalEventHandler().call(new PlayerIncomeChangeEvent(this, newRate));
        return newRate;
    }

    public boolean canAffordWithIncome(int cost) {
        return this.getIncomeRate() - DEFAULT_INCOME_RATE >= cost;
    }

    public int getIncomeRate() {
        return this.incomeRate.get();
    }

    public @Nullable Point getLastClickedTowerBlock() {
        return this.lastClickedTowerBlock;
    }

    public void setLastClickedTowerBlock(@Nullable Point lastClickedTowerBlock) {
        this.lastClickedTowerBlock = lastClickedTowerBlock;
    }

    public @NotNull SendQueue getQueue() {
        return this.queue;
    }

    public static class SendQueue {
        private static final int TIME_DECREMENT = MinecraftServer.TICK_MS;

        private final @NotNull AtomicInteger maxQueueTime = new AtomicInteger(DEFAULT_MAX_QUEUE_TIME);
        private final @NotNull Queue<QueuedEnemyMob> queuedMobs = new ConcurrentLinkedQueue<>();

        private final @NotNull AtomicInteger currentQueueTime = new AtomicInteger(0);
        private final @NotNull AtomicInteger timeToCurrentSend = new AtomicInteger(0);

        public SendQueue() {
            MinecraftServer.getSchedulerManager().buildTask(this::tick)
                    .repeat(TaskSchedule.nextTick())
                    .schedule();
        }

        private void tick() {
        }

        public QueuedEnemyMob poll() {
            QueuedEnemyMob mob = this.queuedMobs.poll();

            QueuedEnemyMob newMob = this.queuedMobs.peek();
            if (newMob != null) {
                this.timeToCurrentSend.set(newMob.mob().getSendTime());
            }

            return mob;
        }

        public boolean canQueue(@NotNull EnemyMob enemyMob) {
            int sendTime = enemyMob.getSendTime();

            return this.currentQueueTime.get() + sendTime <= this.maxQueueTime.get();
        }

        /**
         * Reduces the time of all necessary variables by {@link #TIME_DECREMENT}.
         *
         * @return The updated timeToCurrentSend.
         */
        public int tickTime() {
            int newTime = this.timeToCurrentSend.updateAndGet(time -> Math.max(time - TIME_DECREMENT, 0));
            this.currentQueueTime.updateAndGet(time -> Math.max(time - TIME_DECREMENT, 0));

            return newTime;
        }

        public void queue(@NotNull QueuedEnemyMob mob) {
            this.queuedMobs.add(mob);

            this.currentQueueTime.addAndGet(mob.mob().getSendTime());

            if (this.queuedMobs.size() == 1) {
                this.timeToCurrentSend.set(mob.mob().getSendTime());
            }
        }

        public @NotNull Queue<QueuedEnemyMob> getQueuedMobs() {
            return this.queuedMobs;
        }
    }
}
