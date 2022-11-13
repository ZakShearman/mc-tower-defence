package pink.zak.minestom.towerdefence;

import cc.towerdefence.minestom.module.Module;
import cc.towerdefence.minestom.module.ModuleData;
import cc.towerdefence.minestom.module.ModuleEnvironment;
import cc.towerdefence.minestom.module.kubernetes.KubernetesModule;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.cache.TDUserLoader;
import pink.zak.minestom.towerdefence.command.towerdefence.TowerDefenceCommand;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.listener.ProtectionHandler;
import pink.zak.minestom.towerdefence.lobby.LobbyManager;
import pink.zak.minestom.towerdefence.scoreboard.ScoreboardManager;
import pink.zak.minestom.towerdefence.storage.MapStorage;
import pink.zak.minestom.towerdefence.storage.MobStorage;
import pink.zak.minestom.towerdefence.storage.TowerStorage;
import pink.zak.minestom.towerdefence.utils.mechanic.CustomExplosion;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

@ModuleData(name = "towerdefence", required = false)
public class TowerDefenceModule extends Module {
    private static EventNode<Event> EVENT_NODE; // todo is this still necessary?

    private final KubernetesModule kubernetesModule;

    private GameState gameState = GameState.LOBBY;

    private TDUserLoader userCache;

    private MobStorage mobStorage;
    private MapStorage mapStorage;
    private TowerStorage towerStorage;
    private ScoreboardManager scoreboardManager;

    private GameHandler gameHandler;

    protected TowerDefenceModule(@NotNull ModuleEnvironment environment) {
        super(environment);

        this.kubernetesModule = environment.moduleManager().getModule(KubernetesModule.class);
    }

    @NotNull
    public static EventNode<Event> getCallingEventNode() {
        return EVENT_NODE;
    }

    @Override
    public boolean onLoad() {
        EVENT_NODE = this.getEventNode();
        this.startBenchmark();

        DimensionType dimensionType = DimensionType.builder(NamespaceID.from("towerdefence:main"))
                .fixedTime(1000L)
                .skylightEnabled(true)
                .build();
        MinecraftServer.getDimensionTypeManager().addDimension(dimensionType);
        Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer(dimensionType);

        instance.setExplosionSupplier((centerX, centerY, centerZ, strength, additionalData) -> new CustomExplosion(centerX, centerY, centerZ, strength));
        this.getEventNode().addListener(PlayerLoginEvent.class, event -> {
            event.getPlayer().setRespawnPoint(new Pos(-1, 67, 4));
            event.setSpawningInstance(instance);
        });

        this.userCache = new TDUserLoader(this);

        LobbyManager lobbyManager = new LobbyManager(this);

        this.mobStorage = new MobStorage(this);
        this.mapStorage = new MapStorage(this);
        this.towerStorage = new TowerStorage(this);
        this.scoreboardManager = new ScoreboardManager(this, lobbyManager);

        this.gameHandler = new GameHandler(this, lobbyManager);

        new ProtectionHandler(this);

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new TowerDefenceCommand(this));

        MinecraftServer.getConnectionManager().setPlayerProvider(this.userCache);

        return true;
    }

    @Override
    public void onUnload() {
        this.userCache.saveAll();
    }

    public KubernetesModule getKubernetesModule() {
        return this.kubernetesModule;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
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
        MinecraftServer.getGlobalEventHandler().addListener(ServerTickMonitorEvent.class, event -> lastTick.set(event.getTickMonitor()));

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
            if (players.isEmpty())
                return;

            long ramUsage = benchmarkManager.getUsedMemory();
            ramUsage /= 1e6; // bytes to MB

            TickMonitor tickMonitor = lastTick.get();
            final Component header = Component.text("RAM USAGE: " + ramUsage + " MB")
                    .append(Component.newline())
                    .append(Component.text("TICK TIME: " + MathUtils.round(tickMonitor.getTickTime(), 2) + "ms"));
            final Component footer = benchmarkManager.getCpuMonitoringMessage();
            Audiences.players().sendPlayerListHeaderAndFooter(header, footer);
        }).repeat(10, TimeUnit.SERVER_TICK).schedule();
    }
}
