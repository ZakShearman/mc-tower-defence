package pink.zak.minestom.towerdefence.model.mob.living;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.map.PathCorner;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.EnemyMob;
import pink.zak.minestom.towerdefence.model.mob.EnemyMobLevel;
import pink.zak.minestom.towerdefence.model.mob.living.types.LlamaLivingEnemyMob;
import pink.zak.minestom.towerdefence.utils.DirectionUtils;
import pink.zak.minestom.towerdefence.utils.StringUtils;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class LivingEnemyMob extends EntityCreature {
    protected final EnemyMob enemyMob;
    protected final EnemyMobLevel level;
    protected final Team team;

    protected final int positionModifier;
    protected final List<PathCorner> corners;
    protected int currentCornerIndex;
    protected PathCorner currentCorner;
    protected PathCorner nextCorner;
    protected double moveDistance;

    protected AtomicInteger health;

    protected Task task;

    protected LivingEnemyMob(@NotNull EnemyMob enemyMob, Instance instance, TowerMap map, Team team, int level) {
        super(enemyMob.entityType());

        this.enemyMob = enemyMob;
        this.team = team;
        this.level = enemyMob.level(level);
        this.positionModifier = ThreadLocalRandom.current().nextInt(-map.getRandomValue(), map.getRandomValue() + 1);
        this.corners = map.getCorners(team);
        this.currentCornerIndex = 0;
        this.currentCorner = this.corners.get(0);
        this.nextCorner = this.corners.get(1);

        this.health = new AtomicInteger(this.level.health());

        if (enemyMob.flying())
            this.setNoGravity(true);

        this.setCustomName(this.createCustomName());
        this.setCustomNameVisible(true);
        this.setInstance(instance, (team == Team.RED ? map.getRedMobSpawn() : map.getBlueMobSpawn()).add(this.positionModifier, enemyMob.flying() ? 5 : 0, this.positionModifier));

        this.task = this.scheduleMovements();
    }

    public static LivingEnemyMob createMob(EnemyMob enemyMob, Instance instance, TowerMap map, Team team, int level) {
        if (enemyMob.entityType() == EntityType.LLAMA)
            return new LlamaLivingEnemyMob(enemyMob, instance, map, team, level);
        else
            return new LivingEnemyMob(enemyMob, instance, map, team, level);
    }

    private Component createCustomName() {
        return Component.text(StringUtils.namespaceToName(this.entityType.name()) + " " + StringUtils.integerToCardinal(this.level.level()), NamedTextColor.DARK_GREEN)
            .append(Component.text(" (", NamedTextColor.GREEN))
            .append(Component.text((this.level.health() / this.health.get()) * 100 + "%", NamedTextColor.DARK_GREEN))
            .append(Component.text(")", NamedTextColor.GREEN));
    }

    private Task scheduleMovements() {
        return MinecraftServer.getSchedulerManager().buildTask(this::updatePos)
            .repeat(1, TimeUnit.SERVER_TICK)
            .schedule();
    }

    private void updatePos() {
        //this.getNavigator().moveTowards(this.modifyPosition(), this.mobType.getSpeed() * 3);
        this.refreshPosition(this.modifyPosition());
        this.moveDistance += this.level.movementSpeed();
        if (this.nextCorner == null) {
            if (this.moveDistance >= this.currentCorner.distance() - this.positionModifier)
                this.nextCorner();
        } else if (this.moveDistance >= this.currentCorner.distance() + this.getLengthIncrease())
            this.nextCorner();
    }

    private Pos modifyPosition() {
        Pos currentPos = this.getPosition();
        Pos newPos = switch (this.currentCorner.direction()) {
            case EAST -> currentPos.add(this.level.movementSpeed(), 0, 0);
            case SOUTH -> currentPos.add(0, 0, this.level.movementSpeed());
            case WEST -> currentPos.sub(this.level.movementSpeed(), 0, 0);
            case NORTH -> currentPos.sub(0, 0, this.level.movementSpeed());
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
            this.startAttackingCastle();
            return;
        }
        this.currentCorner = this.nextCorner;
        if (++newCornerIndex == cornerSize)
            this.nextCorner = null;
        else
            this.nextCorner = this.corners.get(newCornerIndex);

        this.moveDistance = 0;
    }

    protected void attackCastle() {
        this.swingMainHand();
        this.swingOffHand();
    }

    private void startAttackingCastle() {
        this.task = MinecraftServer.getSchedulerManager()
            .buildTask(this::attackCastle)
            .repeat(2, ChronoUnit.SECONDS)
            .schedule();
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
}
