package pink.zak.minestom.towerdefence.game;

import com.google.common.collect.Maps;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.instance.Instance;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class GameHandler {
    private final TowerDefencePlugin plugin;
    private final TowerMap map;
    private Map<Player, GameUser> users = Maps.newHashMap();

    public GameHandler(TowerDefencePlugin plugin) {
        this.plugin = plugin;
        this.map = plugin.getMapStorage().getMap();
        plugin.getEventNode().addListener(PlayerDisconnectEvent.class, event -> this.users.remove(event.getPlayer()));
    }

    public void start(Instance instance) {
        this.plugin.setGameState(GameState.IN_PROGRESS);
        this.configureTeam(Team.RED, this.plugin.getRedPlayers());
        this.configureTeam(Team.BLUE, this.plugin.getBluePlayers());
        this.plugin.getScoreboardManager().startGame();

        Audiences.all().sendMessage(Component.text("Red mob spawn: " + this.map.getRedMobSpawn()));
        Map<EntityType, EnemyMob> mobs = this.plugin.getMobStorage().getEnemyMobs();
        Executors.newScheduledThreadPool(2).scheduleAtFixedRate(() -> {
            EnemyMob enemyMob = mobs.values().toArray(new EnemyMob[]{})[ThreadLocalRandom.current().nextInt(mobs.size())];
            LivingEnemyMob.createMob(enemyMob, instance, this.map, Team.RED, 1);
        }, 0, 400, TimeUnit.MILLISECONDS);
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

    public GameUser getGameUser(Player player) {
        return this.users.get(player);
    }

    public Map<Player, GameUser> getUsers() {
        return this.users;
    }
}
