package pink.zak.minestom.towerdefence;

import dev.emortal.api.modules.annotation.Dependency;
import dev.emortal.api.modules.annotation.ModuleData;
import dev.emortal.api.modules.env.ModuleEnvironment;
import dev.emortal.minestom.core.Environment;
import dev.emortal.minestom.core.module.MinestomModule;
import dev.emortal.minestom.core.module.kubernetes.KubernetesModule;
import dev.emortal.minestom.core.module.messaging.MessagingModule;
import io.github.cdimascio.dotenv.Dotenv;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.agones.GameStateManager;
import pink.zak.minestom.towerdefence.cache.TDUserLoader;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.lobby.LobbyManager;
import pink.zak.minestom.towerdefence.scoreboard.ScoreboardManager;
import pink.zak.minestom.towerdefence.storage.MobStorage;
import pink.zak.minestom.towerdefence.storage.TowerStorage;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;
import pink.zak.minestom.towerdefence.world.WorldLoader;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@ModuleData(name = "towerdefence", dependencies = {@Dependency(name = "kubernetes"), @Dependency(name = "messaging", required = false)})
public class TowerDefenceModule extends MinestomModule {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    private final KubernetesModule kubernetesModule;
    private final @Nullable MessagingModule messagingModule;

    private TowerDefenceInstance instance;

    private GameState gameState = GameState.LOBBY;

    private TDUserLoader userCache;

    private MobStorage mobStorage;
    private TowerStorage towerStorage;
    private ScoreboardManager scoreboardManager;

    private GameHandler gameHandler;

    private final AtomicReference<TickMonitor> LAST_TICK = new AtomicReference<>();

    protected TowerDefenceModule(@NotNull ModuleEnvironment environment) {
        super(environment);

        this.kubernetesModule = super.getModule(KubernetesModule.class);
        this.messagingModule = super.getOptionalModule(MessagingModule.class);

        MinecraftServer.getBenchmarkManager().enable(Duration.of(10, TimeUnit.SECOND));

        MinecraftServer.getGlobalEventHandler().addListener(ServerTickMonitorEvent.class, event -> LAST_TICK.set(event.getTickMonitor()));

        BenchmarkManager benchmarkManager = MinecraftServer.getBenchmarkManager();
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (LAST_TICK.get() == null || MinecraftServer.getConnectionManager().getOnlinePlayerCount() == 0)
                return;

            long ramUsage = benchmarkManager.getUsedMemory();
            ramUsage /= 1e6; // bytes to MB

            TickMonitor tickMonitor = LAST_TICK.get();
            final Component header = Component.text("RAM USAGE: " + ramUsage + " MB")
                    .append(Component.newline())
                    .append(Component.text("TICK TIME: " + MathUtils.round(tickMonitor.getTickTime(), 2) + "ms"))
                    .append(Component.newline())
                    .append(Component.text("ACQ TIME: " + MathUtils.round(tickMonitor.getAcquisitionTime(), 2) + "ms"));
            final Component footer = benchmarkManager.getCpuMonitoringMessage();
            Audiences.players().sendPlayerListHeaderAndFooter(header, footer);
        }).repeat(10, TimeUnit.SERVER_TICK).schedule();
    }

    @Override
    public boolean onLoad() {
        WorldLoader worldLoader = new WorldLoader();
        this.instance = worldLoader.load();

        super.eventNode.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.getPlayer().setRespawnPoint(new Pos(-1, 67, 4));
            event.setSpawningInstance(this.instance);
        });

        this.userCache = new TDUserLoader(this);

        this.mobStorage = new MobStorage();
        this.towerStorage = new TowerStorage();

        GameStateManager gameStateManager = new GameStateManager(this.messagingModule);

        LobbyManager lobbyManager = new LobbyManager(this, gameStateManager);
        this.scoreboardManager = new ScoreboardManager(this, lobbyManager);

        this.gameHandler = new GameHandler(this, lobbyManager, gameStateManager, this.messagingModule);

        new ProtectionHandler(this);

        MinecraftServer.getConnectionManager().setPlayerProvider(this.userCache);

        if (!Environment.isProduction()) OpenToLAN.open();

        return true;
    }

    @Override
    public void onUnload() {
        this.userCache.saveAll();
    }

    /**
     * Get an env var from the .env file with the system environment as a backup
     *
     * @param key The key to get
     * @return The value of the key (or null if not found)
     */
    public static @Nullable String getEnv(@NotNull String key) {
        return dotenv.get(key, System.getenv(key));
    }

    public static @NotNull String getEnv(@NotNull String key, @NotNull String defaultValue) {
        String value = getEnv(key);
        return value == null ? defaultValue : value;
    }

    public static @NotNull <T> T getEnv(@NotNull String key, @NotNull T defaultValue, @NotNull Function<String, T> parser) {
        String value = getEnv(key);
        return value == null ? defaultValue : parser.apply(value);
    }

    public static boolean getFlag(@NotNull String key, boolean defaultValue) {
        return Boolean.parseBoolean(getEnv(key, String.valueOf(defaultValue)));
    }

    public @NotNull EventNode<Event> getEventNode() {
        return super.eventNode;
    }

    public KubernetesModule getKubernetesModule() {
        return this.kubernetesModule;
    }

    public TowerDefenceInstance getInstance() {
        return instance;
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

    public TowerStorage getTowerStorage() {
        return this.towerStorage;
    }

    public ScoreboardManager getScoreboardManager() {
        return this.scoreboardManager;
    }

    public GameHandler getGameHandler() {
        return this.gameHandler;
    }
}
