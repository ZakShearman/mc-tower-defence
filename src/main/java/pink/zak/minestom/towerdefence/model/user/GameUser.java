package pink.zak.minestom.towerdefence.model.user;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.api.event.player.PlayerCoinChangeEvent;
import pink.zak.minestom.towerdefence.api.event.player.PlayerIncomeChangeEvent;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.queue.MobQueue;
import pink.zak.minestom.towerdefence.upgrade.UpgradeHandler;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

public final class GameUser {
    public static final int DEFAULT_COINS = 2_000;
    public static final int DEFAULT_INCOME_RATE = 50;

    private final @NotNull TDPlayer player; // todo in the future we should allow re-joining a game so this will not be final.
    private final @NotNull Team team;
    private final @NotNull MobQueue queue;
    private final @NotNull UpgradeHandler upgradeHandler;

    private final @NotNull AtomicInteger coins = new AtomicInteger(DEFAULT_COINS);

    // incomeRate is the amount of coins the player gets per 10 seconds.
    private final @NotNull AtomicInteger incomeRate = new AtomicInteger(DEFAULT_INCOME_RATE);

    public GameUser(@NotNull TDPlayer player, @NotNull Set<EnemyMob> defaultUnlocks, @NotNull Team team, @NotNull MobHandler mobHandler) {
        this.player = player;
        this.team = team;

        this.queue = new MobQueue(mobHandler, this); // todo: unregister when the game ends/when the player leaves
        this.upgradeHandler = new UpgradeHandler(this, defaultUnlocks);
    }

    public @NotNull TDPlayer getPlayer() {
        return this.player;
    }

    public @NotNull Team getTeam() {
        return this.team;
    }

    public int getCoins() {
        return this.coins.get();
    }

    public @NotNull UpgradeHandler getUpgradeHandler() {
        return this.upgradeHandler;
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

    public @NotNull MobQueue getQueue() {
        return this.queue;
    }

}
