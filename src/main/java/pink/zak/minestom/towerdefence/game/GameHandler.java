package pink.zak.minestom.towerdefence.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.hologram.Hologram;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.api.event.game.CastleDamageEvent;
import pink.zak.minestom.towerdefence.api.event.game.GameStartEvent;
import pink.zak.minestom.towerdefence.cache.TDUserCache;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.game.listeners.MobMenuHandler;
import pink.zak.minestom.towerdefence.game.listeners.TowerPlaceHandler;
import pink.zak.minestom.towerdefence.game.listeners.TowerUpgradeHandler;
import pink.zak.minestom.towerdefence.game.listeners.UserSettingsMenuHandler;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.user.GameUser;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GameHandler {
    private final @NotNull TowerDefencePlugin plugin;
    private final @NotNull TDUserCache userCache;
    private final @NotNull TowerMap map;

    private final @NotNull MobHandler mobHandler;
    private final @NotNull TowerHandler towerHandler;
    private final @NotNull MobMenuHandler mobMenuHandler;
    private final @NotNull UserSettingsMenuHandler userSettingsMenuHandler;

    private final Set<EnemyMob> defaultEnemyMobs;

    private @NotNull Map<Player, GameUser> users = new HashMap<>();
    private Instance instance;

    private final @NotNull AtomicInteger redTowerHealth = new AtomicInteger(1000);
    private final @NotNull AtomicInteger blueTowerHealth = new AtomicInteger(1000);

    private Hologram redTowerHologram;
    private Hologram blueTowerHologram;

    public GameHandler(@NotNull TowerDefencePlugin plugin) {
        this.plugin = plugin;
        this.userCache = plugin.getUserCache();
        this.map = plugin.getMapStorage().getMap();

        this.towerHandler = new TowerHandler(plugin, this);
        this.mobHandler = new MobHandler(plugin, this);
        this.mobMenuHandler = new MobMenuHandler(plugin, this);
        this.userSettingsMenuHandler = new UserSettingsMenuHandler(plugin);

        this.defaultEnemyMobs = plugin.getMobStorage().getEnemyMobs().values()
            .stream()
            .filter(mob -> mob.getLevel(1).getManaCost() <= 0)
            .collect(Collectors.toUnmodifiableSet());

        new TowerPlaceHandler(plugin, this);
        new TowerUpgradeHandler(plugin, this);

        plugin.getEventNode().addListener(PlayerDisconnectEvent.class, event -> this.users.remove(event.getPlayer()));
    }

    public void start(@NotNull Instance instance) {
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

        MinecraftServer.getGlobalEventHandler().call(new GameStartEvent(Collections.unmodifiableCollection(this.users.values())));

        /*EnemyMob enemyMob = this.plugin.getMobStorage().getEnemyMob(EntityType.LLAMA);
        EnemyMobLevel enemyMobLevel = enemyMob.level(1);
        QueuedEnemyMob queuedEnemyMob = new QueuedEnemyMob(enemyMob,enemyMobLevel);
        for (int i = 0; i<100; i++) { // desired game count
            for (int z = 0; z<5000; z++) {// estimated max mobs in a game
                this.mobHandler.spawnMob(queuedEnemyMob, new GameUser(null, Team.RED));
            }
        }*/
    }

    private void configureTeam(@NotNull Team team, @NotNull Set<Player> players) {
        Pos spawnPoint = this.map.getSpawn(team);

        if (team == Team.RED) {
            this.redTowerHologram = new Hologram(this.instance, this.map.getRedTowerHologram(), this.createTowerHologram(Team.RED));
        } else {
            this.blueTowerHologram = new Hologram(this.instance, this.map.getBlueTowerHologram(), this.createTowerHologram(Team.BLUE));
        }

        for (Player player : players) {
            GameUser gameUser = new GameUser(player, this.userCache.getUser(player.getUuid()), this.defaultEnemyMobs, team);
            this.users.put(player, gameUser);

            player.setAllowFlying(true);
            player.setFlying(true);
            player.setFlyingSpeed(gameUser.getUser().getFlySpeed().getSpeed());
            player.teleport(spawnPoint);
        }
    }

    private @NotNull Component createTowerHologram(Team team) {
        int health = (team == Team.RED ? this.redTowerHealth : this.blueTowerHealth).get();
        int barAmount = (int) Math.ceil(health / 25.0); // for 40 bars, 40 * 25 = 1000

        char[] presentBars = new char[barAmount];
        char[] lostBars = new char[40 - barAmount];
        Arrays.fill(presentBars, '|');
        Arrays.fill(lostBars, '|');

        return Component.text(String.valueOf(presentBars), NamedTextColor.GREEN).append(Component.text(String.valueOf(lostBars), NamedTextColor.RED));
    }

    public void damageTower(@NotNull Team team, int damage) {
        int newHealth;
        if (team == Team.RED) {
            newHealth = this.redTowerHealth.updateAndGet(current -> current - damage);
            this.redTowerHologram.setText(this.createTowerHologram(Team.RED));
        } else {
            newHealth = this.blueTowerHealth.updateAndGet(current -> current - damage);
            this.blueTowerHologram.setText(this.createTowerHologram(Team.BLUE));
        }
        MinecraftServer.getGlobalEventHandler().call(new CastleDamageEvent(team, damage, newHealth));
    }

    public void end() {
        this.users = new HashMap<>();
        // todo properly clean up
    }

    public @NotNull TowerMap getMap() {
        return this.map;
    }

    public @NotNull MobHandler getMobHandler() {
        return this.mobHandler;
    }

    public @NotNull TowerHandler getTowerHandler() {
        return this.towerHandler;
    }

    public boolean isInGame(Player player) {
        return this.users.containsKey(player);
    }

    public @Nullable GameUser getGameUser(Player player) {
        return this.users.get(player);
    }

    public @NotNull Map<Player, GameUser> getUsers() {
        return this.users;
    }

    public Instance getInstance() {
        return this.instance;
    }
}
