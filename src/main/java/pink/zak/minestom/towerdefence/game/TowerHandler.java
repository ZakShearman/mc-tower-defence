package pink.zak.minestom.towerdefence.game;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.api.event.player.PlayerTowerPlaceEvent;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.game.listeners.NecromancerDamageListener;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TowerHandler {
    private final @NotNull AtomicInteger towerIdCounter = new AtomicInteger(Integer.MIN_VALUE);

    private final @NotNull TowerDefenceInstance instance;
    private final @NotNull TowerMap map;
    private final @NotNull GameHandler gameHandler;

    private final @NotNull Set<PlacedTower<?>> redTowers = ConcurrentHashMap.newKeySet();
    private final @NotNull Set<PlacedTower<?>> blueTowers = ConcurrentHashMap.newKeySet();

    public TowerHandler(@NotNull TowerDefenceModule module, @NotNull GameHandler gameHandler) {
        this.gameHandler = gameHandler;
        this.instance = module.getInstance();
        this.map = this.instance.getTowerMap();

        new NecromancerDamageListener(module.getEventNode());
    }

    public void createTower(@NotNull Tower tower, @NotNull GameUser gameUser) {
        // todo calculate facing
        /*
        from base of tower:
  for y - 3 and + 3
  for +x, -x, +z, -z for 15 blocks
  if intersection with path block, log distance and direction.
  shortest direction to path wins and tower faces towards the cast direction
         */
        Point basePoint = gameUser.getLastClickedTowerBlock();
        Direction direction = this.determineFacing(gameUser.getTeam(), basePoint);
        PlacedTower<?> placedTower = PlacedTower.create(this.gameHandler, this.instance, tower, this.map.getTowerBaseMaterial(), this.generateTowerId(), gameUser, basePoint, direction);
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

    private Direction determineFacing(@NotNull Team team, Point basePoint) {
        int distance = 100;
        Direction direction = Direction.NORTH;
        for (int y = basePoint.blockY() - 5; y < basePoint.blockY() + 5; y++) {
            for (int x = basePoint.blockX() - 15; x < basePoint.blockX() + 15; x++) {
                Block block = this.instance.getBlock(x, y, basePoint.blockZ());
                String tagValue = block.getTag(TowerDefenceInstance.TOWER_PATH_TAG);
                if (tagValue == null || !tagValue.equals(team.toString())) continue;

                int tempDistance = Math.abs(x);
                if (tempDistance < distance) {
                    distance = tempDistance;
                    if (x > basePoint.blockX())
                        direction = Direction.EAST;
                    else
                        direction = Direction.WEST;
                }
            }
            for (int z = basePoint.blockZ() - 15; z < basePoint.blockZ() + 15; z++) {
                Block block = this.instance.getBlock(basePoint.blockX(), y, z);
                String tagValue = block.getTag(TowerDefenceInstance.TOWER_PATH_TAG);
                if (tagValue == null || !tagValue.equals(team.toString())) continue;

                int tempDistance = Math.abs(z);
                if (tempDistance < distance) {
                    if (z > basePoint.blockZ())
                        direction = Direction.SOUTH;
                    else
                        direction = Direction.NORTH;
                    distance = tempDistance;
                }
            }
        }
        return direction;
    }

    public @NotNull Set<PlacedTower<?>> getRedTowers() {
        return this.redTowers;
    }

    public @NotNull Set<PlacedTower<?>> getBlueTowers() {
        return this.blueTowers;
    }
}
