package pink.zak.minestom.towerdefence;

import dev.emortal.minestom.core.module.Module;
import dev.emortal.minestom.core.module.ModuleData;
import dev.emortal.minestom.core.module.ModuleEnvironment;
import dev.emortal.minestom.core.module.kubernetes.KubernetesModule;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.agones.AgonesManager;
import pink.zak.minestom.towerdefence.cache.TDUserLoader;
import pink.zak.minestom.towerdefence.command.towerdefence.TowerDefenceCommand;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.lobby.LobbyManager;
import pink.zak.minestom.towerdefence.scoreboard.ScoreboardManager;
import pink.zak.minestom.towerdefence.storage.MobStorage;
import pink.zak.minestom.towerdefence.storage.TowerStorage;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;
import pink.zak.minestom.towerdefence.world.WorldLoader;

@ModuleData(name = "towerdefence", required = false, softDependencies = {KubernetesModule.class})
public class TowerDefenceModule extends Module {
    private final KubernetesModule kubernetesModule;
    private final AgonesManager agonesManager;

    private TowerDefenceInstance instance;

    private GameState gameState = GameState.LOBBY;

    private TDUserLoader userCache;

    private MobStorage mobStorage;
    private TowerStorage towerStorage;
    private ScoreboardManager scoreboardManager;

    private GameHandler gameHandler;

    protected TowerDefenceModule(@NotNull ModuleEnvironment environment) {
        super(environment);

        this.kubernetesModule = environment.moduleManager().getModule(KubernetesModule.class);
        this.agonesManager = new AgonesManager(this.kubernetesModule);
    }

    @Override
    public boolean onLoad() {
        WorldLoader worldLoader = new WorldLoader();
        this.instance = worldLoader.load();

        this.getEventNode().addListener(PlayerLoginEvent.class, event -> {
            event.getPlayer().setRespawnPoint(new Pos(-1, 67, 4));
            event.setSpawningInstance(this.instance);
        });

        this.userCache = new TDUserLoader(this);

        this.mobStorage = new MobStorage();
        this.towerStorage = new TowerStorage();

        LobbyManager lobbyManager = new LobbyManager(this, this.agonesManager);
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
