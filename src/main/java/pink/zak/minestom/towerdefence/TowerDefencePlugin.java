package pink.zak.minestom.towerdefence;

import com.google.common.collect.Sets;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;
import net.minestom.server.extensions.Extension;
import org.slf4j.Logger;
import pink.zak.minestom.towerdefence.command.towerdefence.TowerDefenceCommand;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.game.listeners.TowerInteractionHandler;
import pink.zak.minestom.towerdefence.listener.ProtectionHandler;
import pink.zak.minestom.towerdefence.listener.SpawnItemHandler;
import pink.zak.minestom.towerdefence.scoreboard.ScoreboardManager;
import pink.zak.minestom.towerdefence.storage.MapStorage;
import pink.zak.minestom.towerdefence.storage.TowerStorage;

import java.util.Set;

public class TowerDefencePlugin extends Extension {
    public static Logger LOGGER;

    private final Set<Player> redPlayers = Sets.newConcurrentHashSet();
    private final Set<Player> bluePlayers = Sets.newConcurrentHashSet();
    private GameState gameState = GameState.LOBBY;

    private MapStorage mapStorage;
    private TowerStorage towerStorage;
    private ScoreboardManager scoreboardManager;

    private GameHandler gameHandler;

    @Override
    public void initialize() {
        LOGGER = getLogger();

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
}
