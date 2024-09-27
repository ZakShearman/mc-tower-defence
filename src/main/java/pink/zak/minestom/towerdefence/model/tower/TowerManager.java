package pink.zak.minestom.towerdefence.model.tower;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.api.event.player.PlayerTowerPlaceEvent;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.utils.Result;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class TowerManager {

    private static final @NotNull AtomicInteger TOWER_ID_COUNTER = new AtomicInteger(Integer.MIN_VALUE);

    private final @NotNull Map<Team, Set<PlacedTower<?>>> towers = new HashMap<>();

    private final @NotNull TowerDefenceInstance instance;
    private final @NotNull GameHandler gameHandler;

    public TowerManager(@NotNull TowerDefenceInstance instance, @NotNull GameHandler gameHandler) {
        this.instance = instance;
        this.gameHandler = gameHandler;
    }

    public @NotNull Result<TowerPlaceFailureReason> placeTower(@NotNull Tower tower, @NotNull Point position, @NotNull GameUser user) {
        // check if the user can afford the tower
        TowerLevel level = tower.getLevel(1);
        int cost = level.getCost();
//        Audiences.all().sendMessage(Component.text("%s tried to place tower %s at cost %s. Has %s coins".formatted(user.getPlayer().getUsername(), tower.getName(), cost, user.getCoins())));
        if (user.getCoins() < cost) return Result.failure(TowerPlaceFailureReason.CAN_NOT_AFFORD);

        // check if the tower is in a valid position
        if (!tower.isSpaceClear(this.instance, position)) return Result.failure(TowerPlaceFailureReason.AREA_NOT_CLEAR);

        // place tower
        PlacedTower<?> placedTower = PlacedTower.create(
                this.gameHandler,
                tower,
                TOWER_ID_COUNTER.getAndIncrement(),
                user,
                position,
                this.determineDirection(position, user.getTeam())
        );
        this.getTowers(user.getTeam()).add(placedTower);
        user.updateCoins(current -> current - cost);

        MinecraftServer.getGlobalEventHandler().call(new PlayerTowerPlaceEvent(tower, placedTower, user));

        return Result.success();
    }

    public void removeTower(@NotNull PlacedTower<?> tower) {
        tower.destroy();
        this.getTowers(tower.getOwner().getTeam()).remove(tower);

        // todo: tower remove event
    }

    public @Nullable PlacedTower<?> getTower(@NotNull Team team, int id) {
        return this.getTowers(team).stream()
                .filter(tower -> tower.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public @NotNull Set<PlacedTower<?>> getTowers(@NotNull Team team) {
        return this.towers.computeIfAbsent(team, t -> ConcurrentHashMap.newKeySet());
    }

    public @NotNull Set<PlacedTower<?>> getTowers() {
        return this.towers.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    // todo: do something about this method
    private @NotNull Direction determineDirection(@NotNull Point position, @NotNull Team team) {
        double currentDistanceSquared = Integer.MAX_VALUE;
        Direction currentDirection = Direction.NORTH;

        for (int yDist = -5; yDist < 5; yDist++) {
            for (int xDist = -15; xDist < 15; xDist++) {
                for (int zDist = -15; zDist < 15; zDist++) {
                    Point point = new Pos(xDist + position.blockX(), yDist + position.blockY(), zDist + position.blockZ());
                    Block block = this.instance.getBlock(point);
                    String tagValue = block.getTag(TowerDefenceInstance.TOWER_PATH_TAG);
                    if (tagValue == null || !tagValue.equals(team.toString())) continue;

                    int xDistance = Math.abs(xDist);
                    int zDistance = Math.abs(zDist);
                    double distanceSquared = point.distanceSquared(position);
                    if (distanceSquared < currentDistanceSquared) {
                        // figure out facing
                        if (xDistance > zDistance) currentDirection = xDist > 0 ? Direction.EAST : Direction.WEST;
                        else currentDirection = zDist > 0 ? Direction.SOUTH : Direction.NORTH;
                        currentDistanceSquared = distanceSquared;
                    }
                }
            }
        }

        // https://xkcd.com/1172
        new ChangeGameStatePacket(ChangeGameStatePacket.Reason.ENABLE_RESPAWN_SCREEN, 1);

        return currentDirection;
    }

}
