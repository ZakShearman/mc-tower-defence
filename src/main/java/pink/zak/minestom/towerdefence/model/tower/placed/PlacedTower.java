package pink.zak.minestom.towerdefence.model.tower.placed;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.time.TimeUnit;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.enums.TowerType;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.RelativeBlock;
import pink.zak.minestom.towerdefence.model.tower.Tower;
import pink.zak.minestom.towerdefence.model.tower.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.types.BomberTower;

public abstract class PlacedTower {
    public static final Tag<Short> ID_TAG = Tag.Short("towerId");

    protected final Instance instance;

    protected final Tower tower;
    protected final short id;
    protected final Team team;
    protected final Point basePoint;
    protected final Direction facing;

    protected TowerLevel level;
    protected int levelInt;

    protected LivingEnemyMob target;
    protected Task attackTask;

    protected PlacedTower(Instance instance, Tower tower, Material towerPlaceMaterial, short id, Team team, Point baseBlock, Direction facing, int level) {
        this.instance = instance;
        this.tower = tower;
        this.level = tower.getLevel(level);
        this.id = id;
        this.team = team;
        this.basePoint = baseBlock;
        this.facing = facing;
        this.levelInt = level;

        this.placeLevel();
        this.placeBase(towerPlaceMaterial);
        this.startFiring();
    }

    public static PlacedTower create(GameHandler gameHandler, Instance instance, Tower tower, Material towerPlaceMaterial, short id, Team team, Point baseBlock, Direction facing) {
        TowerType towerType = tower.getType();
        if (towerType == TowerType.BOMBER) {
            return new BomberTower(gameHandler, instance, tower, towerPlaceMaterial, id, team, baseBlock, facing, 1);
        }
        return null;
    }

    private void startFiring() {
        this.attackTask = MinecraftServer.getSchedulerManager()
            .buildTask(() -> {
                if (this.target != null)
                    this.fire();
            })
            .repeat(this.level.fireDelay(), TimeUnit.CLIENT_TICK)
            .schedule();
    }

    protected abstract void fire();

    public void upgrade() {
        this.placeLevel();
        this.level = this.tower.getLevel(this.levelInt);
    }

    private void placeLevel() {
        for (RelativeBlock relativeBlock : this.level.relativeBlocks()) {
            int x = this.basePoint.blockX() + relativeBlock.getXOffset(this.facing);
            int z = this.basePoint.blockZ() + relativeBlock.getZOffset(this.facing);
            int y = this.basePoint.blockY() + relativeBlock.getYOffset();
            Block block = relativeBlock.getBlock().withTag(ID_TAG, this.id);

            this.instance.setBlock(x, y, z, block);
        }
    }

    private void placeBase(Material towerPlaceMaterial) {
        int checkDistance = this.tower.getType().getSize().getCheckDistance();
        for (int x = this.basePoint.blockX() - checkDistance; x <= this.basePoint.blockX() + checkDistance; x++) {
            for (int z = this.basePoint.blockZ() - checkDistance; z <= this.basePoint.blockZ() + checkDistance; z++) {
                this.instance.setBlock(x, this.basePoint.blockY(), z, towerPlaceMaterial.block().withTag(ID_TAG, this.id));
            }
        }
    }

    public Tower getTower() {
        return this.tower;
    }

    public short getId() {
        return this.id;
    }

    public Point getBasePoint() {
        return this.basePoint;
    }

    public Direction getFacing() {
        return this.facing;
    }

    public TowerLevel getLevel() {
        return this.level;
    }

    public int getLevelInt() {
        return this.levelInt;
    }

    public LivingEnemyMob getTarget() {
        return this.target;
    }

    public void setTarget(LivingEnemyMob target) {
        this.target = target;
    }
}
