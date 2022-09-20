package pink.zak.minestom.towerdefence.model.tower.placed;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.Direction;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.enums.TowerType;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.relative.RelativeBlock;
import pink.zak.minestom.towerdefence.model.tower.placed.types.BomberTower;
import pink.zak.minestom.towerdefence.model.tower.placed.types.CharityTower;
import pink.zak.minestom.towerdefence.model.tower.placed.types.LightningTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class PlacedTower<T extends TowerLevel> {
    public static final Tag<Integer> ID_TAG = Tag.Integer("towerId");

    protected final Instance instance;

    protected final Tower tower;
    protected final int id;
    protected final Team team;
    protected final Point basePoint;
    protected final Direction facing;
    protected final GameUser owner;

    protected T level;
    protected int levelInt;

    protected PlacedTower(Instance instance, Tower tower, Material towerPlaceMaterial, int id, GameUser owner, Point baseBlock, Direction facing, int level) {
        this.instance = instance;

        this.tower = tower;
        this.id = id;
        this.team = owner.getTeam();
        this.basePoint = baseBlock;
        this.facing = facing;
        this.owner = owner;

        this.level = (T) tower.getLevel(level);
        this.levelInt = level;

        this.placeLevel();
        this.placeBase(towerPlaceMaterial);
    }

    public static PlacedTower<?> create(TowerDefencePlugin plugin, GameHandler gameHandler, Instance instance, Tower tower, Material towerPlaceMaterial, int id, GameUser owner, Point baseBlock, Direction facing) {
        TowerType towerType = tower.getType();
        return switch (towerType) {
            case BOMBER ->
                    new BomberTower(gameHandler, instance, (AttackingTower) tower, towerPlaceMaterial, id, owner, baseBlock, facing, 1);
            case CHARITY -> new CharityTower(instance, tower, towerPlaceMaterial, id, owner, baseBlock, facing, 1);
            case LIGHTNING ->
                    new LightningTower(plugin, instance, (AttackingTower) tower, towerPlaceMaterial, id, owner, baseBlock, facing, 1);
            default -> throw new RuntimeException("Missing tower - " + towerType + " is not coded in but was created");
        };
    }

    public void upgrade() {
        this.level = (T) this.tower.getLevel(++this.levelInt);
        this.placeLevel();
    }

    private void placeLevel() {
        for (RelativeBlock relativeBlock : this.level.getRelativeBlocks()) {
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

    public void destroy() {
        Set<RelativeBlock> allRelativeBlocks = this.getTower().getLevels().values().stream()
                .filter(towerLevel -> towerLevel.getLevel() <= this.levelInt)
                .flatMap(towerLevel -> towerLevel.getRelativeBlocks().stream())
                .collect(Collectors.toUnmodifiableSet());

        for (RelativeBlock relativeBlock : allRelativeBlocks) {
            int x = this.basePoint.blockX() + relativeBlock.getXOffset(this.facing);
            int z = this.basePoint.blockZ() + relativeBlock.getZOffset(this.facing);
            int y = this.basePoint.blockY() + relativeBlock.getYOffset();

            this.instance.setBlock(x, y, z, Block.AIR);
        }
        this.normaliseBase();
    }

    // returns the blocks below the tower to normal, removing their ID_TAG property.
    // todo add support for map-specific blocks/returning to the original block. Currently hardcoded as Material.CYAN_TERRACOTTA
    private void normaliseBase() {
        int checkDistance = this.tower.getType().getSize().getCheckDistance();
        for (int x = this.basePoint.blockX() - checkDistance; x <= this.basePoint.blockX() + checkDistance; x++) {
            for (int z = this.basePoint.blockZ() - checkDistance; z <= this.basePoint.blockZ() + checkDistance; z++) {
                this.instance.setBlock(x, this.basePoint.blockY(), z, Material.CYAN_TERRACOTTA.block());
            }
        }
    }

    public Tower getTower() {
        return this.tower;
    }

    public int getId() {
        return this.id;
    }

    public Point getBasePoint() {
        return this.basePoint;
    }

    public Direction getFacing() {
        return this.facing;
    }

    public T getLevel() {
        return this.level;
    }

    public int getLevelInt() {
        return this.levelInt;
    }
}
