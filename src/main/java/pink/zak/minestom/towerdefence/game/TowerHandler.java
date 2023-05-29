package pink.zak.minestom.towerdefence.game;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.api.event.player.PlayerTowerPlaceEvent;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.game.handlers.NecromancerDamageListener;
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
        double currentDistanceSquared = Integer.MAX_VALUE;
        Direction currentDirection = Direction.NORTH;

        for (int yDist = -5; yDist < 5; yDist++) {
            for (int xDist = -15; xDist < 15; xDist++) {
                for (int zDist = -15; zDist < 15; zDist++) {
                    Point point = new Pos(xDist + basePoint.blockX(), yDist + basePoint.blockY(), zDist + basePoint.blockZ());
                    Block block = this.instance.getBlock(point);
                    String tagValue = block.getTag(TowerDefenceInstance.TOWER_PATH_TAG);
                    if (tagValue == null || !tagValue.equals(team.toString())) continue;

                    int xDistance = Math.abs(xDist);
                    int zDistance = Math.abs(zDist);
                    double distanceSquared = point.distanceSquared(basePoint);
                    if (distanceSquared < currentDistanceSquared) {
                        // figure out facing
                        if (xDistance > zDistance) {
                            currentDirection = xDist > 0 ? Direction.EAST : Direction.WEST;
                        } else {
                            currentDirection = zDist > 0 ? Direction.SOUTH : Direction.NORTH;
                        }
                        currentDistanceSquared = distanceSquared;
                    }
                }
            }
        }

        new ChangeGameStatePacket(ChangeGameStatePacket.Reason.ENABLE_RESPAWN_SCREEN, 1);
        return currentDirection;
    }

    public @NotNull Set<PlacedTower<?>> getTowers(Team team) {
        return team == Team.RED ? this.redTowers : this.blueTowers;
    }
}
