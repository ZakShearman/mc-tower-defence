package pink.zak.minestom.towerdefence.game;

import com.google.common.collect.Maps;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.instance.Instance;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.game.listeners.MobMenuHandler;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.map.TowerMap;

import java.util.Map;
import java.util.Set;

public class GameHandler {
    private final TowerDefencePlugin plugin;
    private final MobMenuHandler mobMenuHandler;
    private final TowerMap map;
    private Map<Player, GameUser> users = Maps.newHashMap();
    private Instance instance;

    public GameHandler(TowerDefencePlugin plugin) {
        this.plugin = plugin;
        this.mobMenuHandler = new MobMenuHandler(plugin, this);
        this.map = plugin.getMapStorage().getMap();
        plugin.getEventNode().addListener(PlayerDisconnectEvent.class, event -> this.users.remove(event.getPlayer()));

    }

    public void start(Instance instance) {
        this.plugin.setGameState(GameState.IN_PROGRESS);
        this.instance = instance;
        this.configureTeam(Team.RED, this.plugin.getRedPlayers());
        this.configureTeam(Team.BLUE, this.plugin.getBluePlayers());
        this.plugin.getScoreboardManager().startGame();
        this.mobMenuHandler.startGame();
    }

    private void configureTeam(Team team, Set<Player> players) {
        Pos spawnPoint = this.map.getSpawn(team);
        for (Player player : players) {
            this.users.put(player, new GameUser(player, team));

            player.setAllowFlying(true);
            player.setFlying(true);
            player.teleport(spawnPoint);
        }
    }

    public void end() {
        this.users = Maps.newHashMap();
    }

    public boolean isInGame(Player player) {
        return this.users.containsKey(player);
    }

    public TowerMap getMap() {
        return this.map;
    }

    public GameUser getGameUser(Player player) {
        return this.users.get(player);
    }

    public Map<Player, GameUser> getUsers() {
        return this.users;
    }

    public Instance getInstance() {
        return this.instance;
    }
}
