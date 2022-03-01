package pink.zak.minestom.towerdefence.game;

import com.google.common.collect.Maps;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.hologram.Hologram;
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
import pink.zak.minestom.towerdefence.game.listeners.UserSettingsMenuHandler;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.map.TowerMap;

import java.util.Arrays;
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
    private final UserSettingsMenuHandler userSettingsMenuHandler;

    private Map<Player, GameUser> users = Maps.newHashMap();
    private Instance instance;

    private final AtomicInteger redTowerHealth = new AtomicInteger(1000);
    private final AtomicInteger blueTowerHealth = new AtomicInteger(1000);

    private Hologram redTowerHologram;
    private Hologram blueTowerHologram;

    public GameHandler(TowerDefencePlugin plugin) {
        this.plugin = plugin;
        this.userCache = plugin.getUserCache();
        this.map = plugin.getMapStorage().getMap();

        this.towerHandler = new TowerHandler(plugin, this);
        this.mobHandler = new MobHandler(plugin, this);
        this.mobMenuHandler = new MobMenuHandler(plugin, this);
        this.userSettingsMenuHandler = new UserSettingsMenuHandler(plugin);
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

        for (Player player : this.users.keySet()) {
            player.getInventory().clear();
            player.getInventory().setItemStack(4, this.mobMenuHandler.getChestItem());
            player.getInventory().setItemStack(8, UserSettingsMenuHandler.getMenuItem()); // todo standardise static/non-static usage
            this.mobMenuHandler.onGameStart();
            this.userSettingsMenuHandler.onGameStart();
        }

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

        if (team == Team.RED) {
            Audiences.all().sendMessage(Component.text("Creating red hologram at " + this.map.getRedTowerHologram()));
            this.redTowerHologram = new Hologram(this.instance, this.map.getRedTowerHologram(), this.createTowerHologram(Team.RED));
        } else {
            Audiences.all().sendMessage(Component.text("Creating blue hologram at " + this.map.getBlueTowerHologram()));
            this.blueTowerHologram = new Hologram(this.instance, this.map.getBlueTowerHologram(), this.createTowerHologram(Team.BLUE));
        }

        for (Player player : players) {
            GameUser gameUser = new GameUser(player, this.userCache.getUser(player.getUuid()), team);
            this.users.put(player, gameUser);

            player.setAllowFlying(true);
            player.setFlying(true);
            player.setFlyingSpeed(gameUser.getUser().getFlySpeed().getSpeed());
            player.teleport(spawnPoint);
        }
    }

    private Component createTowerHologram(Team team) {
        int health = (team == Team.RED ? this.redTowerHealth : this.blueTowerHealth).get();
        int barAmount = (int) Math.ceil(health / 25.0); // for 40 bars, 40 * 25 = 1000

        char[] presentBars = new char[barAmount];
        char[] lostBars = new char[40 - barAmount];
        Arrays.fill(presentBars, '|');
        Arrays.fill(lostBars, '|');

        return Component.text(String.valueOf(presentBars), NamedTextColor.GREEN).append(Component.text(String.valueOf(lostBars), NamedTextColor.RED));
    }

    public void damageTower(Team team, int damage) {
        int newHealth;
        if (team == Team.RED) {
            newHealth = this.redTowerHealth.updateAndGet(current -> current - damage);
            this.redTowerHologram.setText(this.createTowerHologram(Team.RED));
        } else {
            newHealth = this.blueTowerHealth.updateAndGet(current -> current - damage);
            this.redTowerHologram.setText(this.createTowerHologram(Team.BLUE));
        }
        MinecraftServer.getGlobalEventHandler().call(new TowerDamageEvent(team, damage, newHealth));
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
}
