package pink.zak.minestom.towerdefence.model.mob;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.enums.EnemyMobType;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.map.PathCorner;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.utils.DirectionUtils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EnemyMob extends EntityCreature {
    private final EnemyMobType mobType;
    private final Team team;

    private final int positionModifier;
    private final List<PathCorner> corners;
    private int currentCornerIndex;
    private PathCorner currentCorner;
    private PathCorner nextCorner;
    private double moveDistance;

    private final Task task;

    public EnemyMob(@NotNull EnemyMobType enemyMobType, Instance instance, TowerMap map, Team team) {
        super(enemyMobType.getEntityType());

        this.mobType = enemyMobType;
        this.team = team;
        this.positionModifier = ThreadLocalRandom.current().nextInt(-map.getRandomValue(), map.getRandomValue() + 1);
        this.corners = map.getCorners(team);
        this.currentCornerIndex = 0;
        this.currentCorner = this.corners.get(0);
        this.nextCorner = this.corners.get(1);

        this.setInstance(instance, (team == Team.RED ? map.getRedMobSpawn() : map.getBlueMobSpawn()).add(this.positionModifier, 0, this.positionModifier));
        this.task = this.scheduleMovements();
    }

    private Task scheduleMovements() {
        return MinecraftServer.getSchedulerManager().buildTask(this::updatePos)
            .repeat(1, TimeUnit.SERVER_TICK)
            .schedule();
    }

    private synchronized void updatePos() {
        this.refreshPosition(this.modifyPosition());
        this.moveDistance += this.mobType.getSpeed();
        if (this.nextCorner == null) {
            if (this.moveDistance >= this.currentCorner.distance() - this.positionModifier)
                this.nextCorner();
        } else if (this.moveDistance >= this.currentCorner.distance() + this.getLengthIncrease())
            this.nextCorner();
    }

    private int getLengthIncrease() {
        if (this.positionModifier == 0)
            return 0;
        if (this.nextCorner.direction() == Direction.SOUTH) {
            return -this.positionModifier * 2;
        } else if (this.nextCorner.direction() == Direction.EAST) {
            return 0;
        } else {
            return this.positionModifier * 2;
        }
    }

    private Pos modifyPosition() {
        Pos currentPos = this.getPosition();
        Pos newPos = switch (this.currentCorner.direction()) {
            case EAST -> currentPos.add(this.mobType.getSpeed(), 0, 0);
            case SOUTH -> currentPos.add(0, 0, this.mobType.getSpeed());
            case WEST -> currentPos.sub(this.mobType.getSpeed(), 0, 0);
            case NORTH -> currentPos.sub(0, 0, this.mobType.getSpeed());
            default -> throw new IllegalArgumentException("Direction must be NORTH, EAST, SOUTH or WEST. Provided direction was " + this.currentCorner.direction());
        };
        return newPos.withYaw(DirectionUtils.getYaw(this.currentCorner.direction()));
    }

    private void nextCorner() {
        int newCornerIndex = ++this.currentCornerIndex;
        int cornerSize = this.corners.size();
        if (this.nextCorner == null) {
            this.currentCornerIndex = -1;
            this.task.cancel();
            this.kill();
            return;
        }
        this.currentCorner = this.nextCorner;
        if (++newCornerIndex == cornerSize)
            this.nextCorner = null;
        else
            this.nextCorner = this.corners.get(newCornerIndex);

        this.moveDistance = 0;
    }
}
