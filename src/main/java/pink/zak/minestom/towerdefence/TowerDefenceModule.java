package pink.zak.minestom.towerdefence;

import cc.towerdefence.minestom.module.Module;
import cc.towerdefence.minestom.module.ModuleData;
import cc.towerdefence.minestom.module.ModuleEnvironment;
import cc.towerdefence.minestom.module.kubernetes.KubernetesModule;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.agones.TDAgonesManager;
import pink.zak.minestom.towerdefence.cache.TDUserLoader;
import pink.zak.minestom.towerdefence.command.towerdefence.TowerDefenceCommand;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.listener.ProtectionHandler;
import pink.zak.minestom.towerdefence.lobby.LobbyManager;
import pink.zak.minestom.towerdefence.scoreboard.ScoreboardManager;
import pink.zak.minestom.towerdefence.storage.MobStorage;
import pink.zak.minestom.towerdefence.storage.TowerStorage;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;
import pink.zak.minestom.towerdefence.world.WorldLoader;

@ModuleData(name = "towerdefence", required = false)
public class TowerDefenceModule extends Module {
    private static EventNode<Event> EVENT_NODE; // todo is this still necessary?

    private final KubernetesModule kubernetesModule;
    private final TDAgonesManager tdAgonesManager;

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
        this.tdAgonesManager = new TDAgonesManager(this, this.kubernetesModule);
    }

    @NotNull
    public static EventNode<Event> getCallingEventNode() {
        return EVENT_NODE;
    }

    @Override
    public boolean onLoad() {
        EVENT_NODE = this.getEventNode();

        WorldLoader worldLoader = new WorldLoader();
        this.instance = worldLoader.load();

        this.getEventNode().addListener(PlayerLoginEvent.class, event -> {
            event.getPlayer().setRespawnPoint(new Pos(-1, 67, 4));
            event.setSpawningInstance(this.instance);
        });

        this.userCache = new TDUserLoader(this);

        this.mobStorage = new MobStorage(this);
        this.towerStorage = new TowerStorage(this);


        LobbyManager lobbyManager = new LobbyManager(this);
        this.scoreboardManager = new ScoreboardManager(this, lobbyManager);

        this.gameHandler = new GameHandler(this, lobbyManager);

        new ProtectionHandler(this);

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new TowerDefenceCommand(this));

        MinecraftServer.getConnectionManager().setPlayerProvider(this.userCache);
        this.tdAgonesManager.setPhase("lobby");
        this.tdAgonesManager.setBackfill(true);

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

        this.tdAgonesManager.setPhase(gameState.name().toLowerCase());
        if (gameState == GameState.END) {
            this.tdAgonesManager.setBackfill(false);
        }
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
