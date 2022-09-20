package pink.zak.minestom.towerdefence.game;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.api.event.player.PlayerTowerPlaceEvent;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TowerHandler {
    private final @NotNull AtomicInteger towerIdCounter = new AtomicInteger(Integer.MIN_VALUE);
    private final @NotNull TowerDefencePlugin plugin;
    private final @NotNull GameHandler gameHandler;
    private final @NotNull Set<PlacedTower<?>> redTowers = ConcurrentHashMap.newKeySet();
    private final @NotNull Set<PlacedTower<?>> blueTowers = ConcurrentHashMap.newKeySet();

    private final TowerMap map;
    private Instance instance;

    public TowerHandler(@NotNull TowerDefencePlugin plugin, @NotNull GameHandler gameHandler) {
        this.plugin = plugin;
        this.gameHandler = gameHandler;
        this.map = gameHandler.getMap();
    }

    public void createTower(@NotNull Tower tower, @NotNull GameUser gameUser) {
        PlacedTower<?> placedTower = PlacedTower.create(this.plugin, this.gameHandler, this.instance, tower, this.map.getTowerBaseMaterial(), this.generateTowerId(), gameUser, gameUser.getLastClickedTowerBlock(), Direction.NORTH);
        Team team = gameUser.getTeam();
        if (team == Team.RED)
            this.redTowers.add(placedTower);
        else
            this.blueTowers.add(placedTower);

        MinecraftServer.getGlobalEventHandler().call(new PlayerTowerPlaceEvent(tower, gameUser));
    }

    private int generateTowerId() {
        return this.towerIdCounter.getAndIncrement();
    }

    public PlacedTower<?> getTower(@NotNull GameUser gameUser, int id) {
        return (gameUser.getTeam() == Team.RED ? this.redTowers : this.blueTowers).stream().filter(tower -> tower.getId() == id).findFirst().orElse(null);
    }

    public void removeTower(@NotNull GameUser gameUser, @NotNull PlacedTower<?> tower) {
        if (gameUser.getTeam() == Team.RED)
            this.redTowers.remove(tower);
        else
            this.blueTowers.remove(tower);
    }

    public @NotNull Set<PlacedTower<?>> getRedTowers() {
        return this.redTowers;
    }

    public @NotNull Set<PlacedTower<?>> getBlueTowers() {
        return this.blueTowers;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }
}
