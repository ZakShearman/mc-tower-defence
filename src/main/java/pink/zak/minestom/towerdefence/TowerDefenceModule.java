package pink.zak.minestom.towerdefence;

import dev.emortal.api.modules.annotation.Dependency;
import dev.emortal.api.modules.annotation.ModuleData;
import dev.emortal.api.modules.env.ModuleEnvironment;
import dev.emortal.minestom.core.module.MinestomModule;
import dev.emortal.minestom.core.module.kubernetes.KubernetesModule;
import dev.emortal.minestom.core.module.messaging.MessagingModule;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.agones.GameStateManager;
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

@ModuleData(name = "towerdefence", dependencies = {@Dependency(name = "kubernetes"), @Dependency(name = "messaging", required = false)})
public class TowerDefenceModule extends MinestomModule {
    private final KubernetesModule kubernetesModule;
    private final MessagingModule messagingModule;

    private TowerDefenceInstance instance;

    private GameState gameState = GameState.LOBBY;

    private TDUserLoader userCache;

    private MobStorage mobStorage;
    private TowerStorage towerStorage;
    private ScoreboardManager scoreboardManager;

    private GameHandler gameHandler;

    protected TowerDefenceModule(@NotNull ModuleEnvironment environment) {
        super(environment);

        this.kubernetesModule = super.getModule(KubernetesModule.class);
        this.messagingModule = super.getModule(MessagingModule.class);
    }

    @Override
    public boolean onLoad() {
        WorldLoader worldLoader = new WorldLoader();
        this.instance = worldLoader.load();

        super.eventNode.addListener(PlayerLoginEvent.class, event -> {
            event.getPlayer().setRespawnPoint(new Pos(-1, 67, 4));
            event.setSpawningInstance(this.instance);
        });

        this.userCache = new TDUserLoader(this);

        this.mobStorage = new MobStorage();
        this.towerStorage = new TowerStorage();

        GameStateManager gameStateManager = new GameStateManager(this.messagingModule);

        LobbyManager lobbyManager = new LobbyManager(this, gameStateManager);
        this.scoreboardManager = new ScoreboardManager(this, lobbyManager);

        MessagingModule messagingModule = super.getOptionalModule(MessagingModule.class);
        this.gameHandler = new GameHandler(this, lobbyManager, gameStateManager, messagingModule);

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
