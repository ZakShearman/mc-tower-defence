package pink.zak.minestom.towerdefence.game;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.api.event.player.PlayerTowerPlaceEvent;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class TowerHandler {
    private final @NotNull AtomicReference<Short> towerIdCounter = new AtomicReference<>(Short.MIN_VALUE);
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
        PlacedTower<?> placedTower = PlacedTower.create(this.plugin, this.gameHandler, this.instance, tower, this.map.getTowerPlaceMaterial(), this.generateTowerId(), gameUser, gameUser.getLastClickedTowerBlock(), Direction.NORTH);
        Team team = gameUser.getTeam();
        if (team == Team.RED)
            this.redTowers.add(placedTower);
        else
            this.blueTowers.add(placedTower);

        MinecraftServer.getGlobalEventHandler().call(new PlayerTowerPlaceEvent(tower, gameUser));
    }

    private short generateTowerId() {
        return this.towerIdCounter.getAndUpdate(aShort -> ++aShort);
    }

    public PlacedTower<?> getTower(@NotNull GameUser gameUser, short id) {
        return (gameUser.getTeam() == Team.RED ? this.redTowers : this.blueTowers).stream().filter(tower -> tower.getId() == id).findFirst().orElse(null);
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
