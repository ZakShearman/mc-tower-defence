package pink.zak.minestom.towerdefence.model.tower.placed;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.Direction;
import pink.zak.minestom.towerdefence.enums.TowerType;
import pink.zak.minestom.towerdefence.model.tower.RelativeBlock;
import pink.zak.minestom.towerdefence.model.tower.Tower;
import pink.zak.minestom.towerdefence.model.tower.TowerLevel;

public class PlacedTower {
    public static final Tag<Short> ID_TAG = Tag.Short("towerId");

    private final Instance instance;

    private final Tower tower;
    private final short id;
    private final Point basePoint;
    private final Direction facing;
    private int level;

    protected PlacedTower(Instance instance, Tower tower, Material towerPlaceMaterial, short id, Point baseBlock, Direction facing, int level) {
        this.instance = instance;
        this.tower = tower;
        this.id = id;
        this.basePoint = baseBlock;
        this.facing = facing;
        this.level = level;

        this.placeLevel(1);
        this.placeBase(towerPlaceMaterial);
    }

    public static PlacedTower create(Instance instance, Tower tower, Material towerPlaceMaterial, short id, Point baseBlock, Direction facing) {
        TowerType towerType = tower.getType();
        if (towerType == TowerType.BOMBER) {

        }
        return new PlacedTower(instance, tower, towerPlaceMaterial, id, baseBlock, facing, 1);
    }

    public void upgrade() {
        this.placeLevel(++this.level);
    }

    private void placeLevel(int level) {
        TowerLevel towerLevel = this.tower.getLevel(level);

        for (RelativeBlock relativeBlock : towerLevel.relativeBlocks()) {
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

    public int getLevel() {
        return this.level;
    }
}
