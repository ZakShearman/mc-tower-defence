package pink.zak.minestom.towerdefence.model.tower.placed;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.damage.DamageType;
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
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.mob.living.LivingEnemyMob;
import pink.zak.minestom.towerdefence.model.tower.RelativeBlock;
import pink.zak.minestom.towerdefence.model.tower.Tower;
import pink.zak.minestom.towerdefence.model.tower.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.types.BomberTower;
import pink.zak.minestom.towerdefence.model.tower.placed.types.CharityTower;

public abstract class PlacedTower {
    public static final Tag<Short> ID_TAG = Tag.Short("towerId");

    protected final Instance instance;

    protected final Tower tower;
    protected final short id;
    protected final Team team;
    protected final Point basePoint;
    protected final Direction facing;
    protected final GameUser owner;

    protected TowerLevel level;
    protected int levelInt;

    protected PlacedTower(Instance instance, Tower tower, Material towerPlaceMaterial, short id, GameUser owner, Point baseBlock, Direction facing, int level) {
        this.instance = instance;

        this.tower = tower;
        this.id = id;
        this.team = owner.getTeam();
        this.basePoint = baseBlock;
        this.facing = facing;
        this.owner = owner;

        this.level = tower.level(level);
        this.levelInt = level;

        this.placeLevel();
        this.placeBase(towerPlaceMaterial);
    }

    public static PlacedTower create(GameHandler gameHandler, Instance instance, Tower tower, Material towerPlaceMaterial, short id, GameUser owner, Point baseBlock, Direction facing) {
        TowerType towerType = tower.type();
        return switch (towerType) {
            case BOMBER -> new BomberTower(gameHandler, instance, tower, towerPlaceMaterial, id, owner, baseBlock, facing, 1);
            case CHARITY -> new CharityTower(instance, tower, towerPlaceMaterial, id, owner, baseBlock, facing, 1);
            default -> null;
        };
    }

    public void upgrade() {
        this.level = this.tower.level(++this.levelInt);
        this.placeLevel();
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
        int checkDistance = this.tower.type().getSize().getCheckDistance();
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

    public GameUser getOwner() {
        return this.owner;
    }

    public TowerLevel getLevel() {
        return this.level;
    }

    public int getLevelInt() {
        return this.levelInt;
    }
}
