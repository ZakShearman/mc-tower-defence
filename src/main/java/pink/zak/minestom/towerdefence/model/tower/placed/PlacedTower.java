package pink.zak.minestom.towerdefence.model.tower.placed;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.Direction;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.enums.TowerType;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.relative.RelativeBlock;
import pink.zak.minestom.towerdefence.model.tower.config.relative.RelativePoint;
import pink.zak.minestom.towerdefence.model.tower.placed.types.*;
import pink.zak.minestom.towerdefence.model.user.GameUser;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class PlacedTower<T extends TowerLevel> {
    public static final Tag<Integer> ID_TAG = Tag.Integer("towerId");

    protected final Instance instance;

    protected final Tower tower;
    protected final Material towerBaseMaterial;

    protected final int id;
    protected final Team team;
    protected final Point basePoint;
    protected final Direction facing;
    protected final GameUser owner;

    protected T level;
    protected int levelInt;

    protected PlacedTower(Instance instance, Tower tower, Material towerBaseMaterial, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        this.instance = instance;

        this.tower = tower;
        this.towerBaseMaterial = towerBaseMaterial;

        this.id = id;
        this.team = owner.getTeam();
        this.basePoint = basePoint;
        this.facing = facing;
        this.owner = owner;

        this.level = (T) tower.getLevel(level);
        this.levelInt = level;

        this.placeLevel();
        this.placeBase();
    }

    public static PlacedTower<?> create(TowerDefenceModule plugin, GameHandler gameHandler, Instance instance, Tower tower, Material towerBaseMaterial, int id, GameUser owner, Point basePoint, Direction facing) {
        TowerType towerType = tower.getType();
        return switch (towerType) {
            case BOMBER ->
                    new BomberTower(gameHandler, instance, (AttackingTower) tower, towerBaseMaterial, id, owner, basePoint, facing, 1);
            case BLIZZARD -> new BlizzardTower(instance, (AttackingTower) tower, towerBaseMaterial, id, owner, basePoint, facing, 1, gameHandler.getMap());
            case CHARITY -> new CharityTower(instance, tower, towerBaseMaterial, id, owner, basePoint, facing, 1);
            case LIGHTNING ->
                    new LightningTower(instance, (AttackingTower) tower, towerBaseMaterial, id, owner, basePoint, facing, 1);
            case NECROMANCER -> new NecromancerTower(instance, (AttackingTower) tower, towerBaseMaterial, id, owner, basePoint, facing, 1);
            default -> throw new RuntimeException("Missing tower - %s is not coded in but was created".formatted(towerType));
        };
    }

    public void upgrade() {
        T oldLevel = this.level;
        this.level = (T) this.tower.getLevel(++this.levelInt);

        this.removeUnUpdatedBlocks(oldLevel, this.level);
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

    private void placeBase() {
        int checkDistance = this.tower.getType().getSize().getCheckDistance();
        for (int x = this.basePoint.blockX() - checkDistance; x <= this.basePoint.blockX() + checkDistance; x++) {
            for (int z = this.basePoint.blockZ() - checkDistance; z <= this.basePoint.blockZ() + checkDistance; z++) {
                this.instance.setBlock(x, this.basePoint.blockY(), z, this.towerBaseMaterial.block().withTag(ID_TAG, this.id));
            }
        }
    }

    private void removeUnUpdatedBlocks(TowerLevel oldLevel, TowerLevel newLevel) {
        Set<RelativePoint> oldPoints = oldLevel.getRelativeBlocks().stream().map(RelativeBlock::getRelativePoint).collect(Collectors.toSet());
        Set<RelativePoint> newPoints = newLevel.getRelativeBlocks().stream().map(RelativeBlock::getRelativePoint).collect(Collectors.toSet());

        for (RelativePoint relativePoint : oldPoints) {
            if (!newPoints.contains(relativePoint)) {
                int x = (int) (this.basePoint.blockX() + relativePoint.getXOffset());
                int z = (int) (this.basePoint.blockZ() + relativePoint.getZOffset());
                int y = (int) (this.basePoint.blockY() + relativePoint.getYOffset());
                this.instance.setBlock(x, y, z, Block.AIR);
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
    private void normaliseBase() {
        int checkDistance = this.tower.getType().getSize().getCheckDistance();
        for (int x = this.basePoint.blockX() - checkDistance; x <= this.basePoint.blockX() + checkDistance; x++) {
            for (int z = this.basePoint.blockZ() - checkDistance; z <= this.basePoint.blockZ() + checkDistance; z++) {
                this.instance.setBlock(x, this.basePoint.blockY(), z, this.towerBaseMaterial.block());
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
