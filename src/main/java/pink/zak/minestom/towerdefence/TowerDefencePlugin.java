package pink.zak.minestom.towerdefence;

import com.google.common.collect.Sets;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.extensions.Extension;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.time.TimeUnit;
import org.slf4j.Logger;
import pink.zak.minestom.towerdefence.command.towerdefence.TowerDefenceCommand;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.game.listeners.TowerInteractionHandler;
import pink.zak.minestom.towerdefence.listener.ProtectionHandler;
import pink.zak.minestom.towerdefence.listener.SpawnItemHandler;
import pink.zak.minestom.towerdefence.scoreboard.ScoreboardManager;
import pink.zak.minestom.towerdefence.storage.MapStorage;
import pink.zak.minestom.towerdefence.storage.MobStorage;
import pink.zak.minestom.towerdefence.storage.TowerStorage;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class TowerDefencePlugin extends Extension {
    public static Logger LOGGER;

    private final Set<Player> redPlayers = Sets.newConcurrentHashSet();
    private final Set<Player> bluePlayers = Sets.newConcurrentHashSet();
    private GameState gameState = GameState.LOBBY;

    private MobStorage mobStorage;
    private MapStorage mapStorage;
    private TowerStorage towerStorage;
    private ScoreboardManager scoreboardManager;

    private GameHandler gameHandler;

    @Override
    public void initialize() {
        LOGGER = getLogger();
        this.startBenchmark();

        this.getEventNode().addListener(PlayerLoginEvent.class, event -> {
            event.getPlayer().setRespawnPoint(new Pos(0, 100, 0));
            event.setSpawningInstance(MinecraftServer.getInstanceManager().createInstanceContainer());

            Audiences.all().sendMessage(Component.text(event.getPlayer().getUsername() + " logged in"));
        });

        this.mobStorage = new MobStorage(this);
        this.mapStorage = new MapStorage(this);
        this.towerStorage = new TowerStorage(this);
        this.scoreboardManager = new ScoreboardManager(this);

        this.gameHandler = new GameHandler(this);

        new TowerInteractionHandler(this);

        new ProtectionHandler(this);
        new SpawnItemHandler(this);

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new TowerDefenceCommand(this));
    }

    @Override
    public void terminate() {
    }

    public Set<Player> getRedPlayers() {
        return this.redPlayers;
    }

    public Set<Player> getBluePlayers() {
        return this.bluePlayers;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public MobStorage getMobStorage() {
        return this.mobStorage;
    }

    public MapStorage getMapStorage() {
        return this.mapStorage;
    }

    public TowerStorage getTowerStorage() {
        return this.towerStorage;
    }

    public ScoreboardManager getScoreboardManager() {
        return this.scoreboardManager;
    }

    public GameHandler getGameHandler() {
        return this.gameHandler;
    }

    private void startBenchmark() {
        BenchmarkManager benchmarkManager = MinecraftServer.getBenchmarkManager();
        benchmarkManager.enable(Duration.ofMillis(Long.MAX_VALUE));

        AtomicReference<TickMonitor> lastTick = new AtomicReference<>();
        MinecraftServer.getUpdateManager().addTickMonitor(lastTick::set);

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
            if (players.isEmpty())
                return;

            long ramUsage = benchmarkManager.getUsedMemory();
            ramUsage /= 1e6; // bytes to MB

            TickMonitor tickMonitor = lastTick.get();
            final Component header = Component.text("RAM USAGE: " + ramUsage + " MB")
                .append(Component.newline())
                .append(Component.text("TICK TIME: " + MathUtils.round(tickMonitor.getTickTime(), 2) + "ms"))
                .append(Component.newline())
                .append(Component.text("ACQ TIME: " + MathUtils.round(tickMonitor.getAcquisitionTime(), 2) + "ms"));
            final Component footer = benchmarkManager.getCpuMonitoringMessage();
            Audiences.players().sendPlayerListHeaderAndFooter(header, footer);
        }).repeat(10, TimeUnit.SERVER_TICK).schedule();
    }
}
