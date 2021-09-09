package pink.zak.minestom.towerdefence.game;

import com.google.common.collect.Sets;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Direction;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.tower.Tower;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class TowerHandler {
    private final AtomicReference<Short> towerIdCounter = new AtomicReference<>(Short.MIN_VALUE);
    private final GameHandler gameHandler;
    private final Set<PlacedTower> redTowers = Sets.newConcurrentHashSet();
    private final Set<PlacedTower> blueTowers = Sets.newConcurrentHashSet();

    private final TowerMap map;
    private Instance instance;

    public TowerHandler(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
        this.map = gameHandler.getMap();
    }

    public void createTower(Tower tower, GameUser gameUser) {
        Team team = gameUser.getTeam();
        PlacedTower placedTower = PlacedTower.create(this.gameHandler, this.instance, tower, this.map.getTowerPlaceMaterial(), this.generateTowerId(), team, gameUser.getLastClickedTowerBlock(), Direction.NORTH);
        if (team == Team.RED)
            this.redTowers.add(placedTower);
        else
            this.blueTowers.add(placedTower);
    }

    private short generateTowerId() {
        return this.towerIdCounter.getAndUpdate(aShort -> ++aShort);
    }

    public Set<PlacedTower> getRedTowers() {
        return this.redTowers;
    }

    public Set<PlacedTower> getBlueTowers() {
        return this.blueTowers;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }
}
