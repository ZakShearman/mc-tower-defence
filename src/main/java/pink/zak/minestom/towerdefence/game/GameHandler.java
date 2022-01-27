package pink.zak.minestom.towerdefence.game;

import com.google.common.collect.Maps;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.instance.Instance;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.api.event.game.TowerDamageEvent;
import pink.zak.minestom.towerdefence.cache.TDUserCache;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.game.listeners.MobMenuHandler;
import pink.zak.minestom.towerdefence.game.listeners.TowerPlaceHandler;
import pink.zak.minestom.towerdefence.game.listeners.TowerUpgradeHandler;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.map.TowerMap;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class GameHandler {
    private final TowerDefencePlugin plugin;
    private final TDUserCache userCache;
    private final TowerMap map;

    private final MobHandler mobHandler;
    private final TowerHandler towerHandler;
    private final MobMenuHandler mobMenuHandler;

    private Map<Player, GameUser> users = Maps.newHashMap();
    private Instance instance;

    private final AtomicInteger redTowerHealth = new AtomicInteger(1000);
    private final AtomicInteger blueTowerHealth = new AtomicInteger(1000);

    public GameHandler(TowerDefencePlugin plugin) {
        this.plugin = plugin;
        this.userCache = plugin.getUserCache();
        this.map = plugin.getMapStorage().getMap();

        this.towerHandler = new TowerHandler(this);
        this.mobHandler = new MobHandler(this, plugin);
        this.mobMenuHandler = new MobMenuHandler(plugin, this);
        new TowerPlaceHandler(plugin, this);
        new TowerUpgradeHandler(plugin, this);

        plugin.eventNode().addListener(PlayerDisconnectEvent.class, event -> this.users.remove(event.getPlayer()));
    }

    public void start(Instance instance) {
        this.plugin.setGameState(GameState.IN_PROGRESS);

        this.instance = instance;
        this.mobHandler.setInstance(instance);
        this.towerHandler.setInstance(instance);

        this.configureTeam(Team.RED, this.plugin.getRedPlayers());
        this.configureTeam(Team.BLUE, this.plugin.getBluePlayers());
        this.plugin.getScoreboardManager().startGame();
        this.mobMenuHandler.startGame();

        /*EnemyMob enemyMob = this.plugin.getMobStorage().getTower(EntityType.LLAMA);
        EnemyMobLevel enemyMobLevel = enemyMob.level(1);
        QueuedEnemyMob queuedEnemyMob = new QueuedEnemyMob(enemyMob,enemyMobLevel);
        for (int i = 0; i<100; i++) { // desired game count
            for (int z = 0; z<5000; z++) {// estimated max mobs in a game
                this.mobHandler.spawnMob(queuedEnemyMob, new GameUser(null, Team.RED));
            }
        }*/
    }

    private void configureTeam(Team team, Set<Player> players) {
        Pos spawnPoint = this.map.getSpawn(team);
        for (Player player : players) {
            GameUser gameUser = new GameUser(player, this.userCache.getUser(player.getUuid()), team);
            this.users.put(player, gameUser);

            player.setAllowFlying(true);
            player.setFlying(true);
            player.setFlyingSpeed(gameUser.getUser().getFlySpeed());
            player.teleport(spawnPoint);
        }
    }

    public void end() {
        this.users = Maps.newHashMap();
        // todo properly clean up
    }

    public TowerMap getMap() {
        return this.map;
    }

    public MobHandler getMobHandler() {
        return this.mobHandler;
    }

    public TowerHandler getTowerHandler() {
        return this.towerHandler;
    }

    public boolean isInGame(Player player) {
        return this.users.containsKey(player);
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

    public void damageRedTower(int damage) {
        int newHealth = this.redTowerHealth.updateAndGet(current -> current - damage);
        MinecraftServer.getGlobalEventHandler().call(new TowerDamageEvent(Team.RED, damage, newHealth));
    }

    public void damageBlueTower(int damage) {
        int newHealth = this.blueTowerHealth.updateAndGet(current -> current - damage);
        MinecraftServer.getGlobalEventHandler().call(new TowerDamageEvent(Team.BLUE, damage, newHealth));
    }
}
