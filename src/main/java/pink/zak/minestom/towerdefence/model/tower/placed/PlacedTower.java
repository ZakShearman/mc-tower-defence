package pink.zak.minestom.towerdefence.model.tower.placed;

import java.util.Set;
import java.util.stream.Collectors;
import net.hollowcube.schem.Rotation;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.enums.TowerType;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.game.MobHandler;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.types.ArcherTower;
import pink.zak.minestom.towerdefence.model.tower.placed.types.BlizzardTower;
import pink.zak.minestom.towerdefence.model.tower.placed.types.BomberTower;
import pink.zak.minestom.towerdefence.model.tower.placed.types.CharityTower;
import pink.zak.minestom.towerdefence.model.tower.placed.types.EarthquakeTower;
import pink.zak.minestom.towerdefence.model.tower.placed.types.LightningTower;
import pink.zak.minestom.towerdefence.model.tower.placed.types.NecromancerTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.utils.DirectionUtil;
import pink.zak.minestom.towerdefence.utils.SchemUtils;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

public abstract class PlacedTower<T extends TowerLevel> {
    public static final Tag<Integer> ID_TAG = Tag.Integer("towerId");

    protected final Instance instance;

    protected final Tower tower;
    protected final Material towerBaseMaterial;

    protected final int id;
    protected final Team team;
    protected final Point basePoint;
    protected final Direction facing;
    protected final @NotNull GameUser owner;

    protected T level;
    protected int levelInt;

    protected PlacedTower(Instance instance, Tower tower, Material towerBaseMaterial, int id,
                          @NotNull GameUser owner, Point basePoint, Direction facing, int level) {
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

    public static PlacedTower<?> create(GameHandler gameHandler, TowerDefenceInstance instance, Tower tower, Material towerBaseMaterial, int id, GameUser owner, Point basePoint, Direction facing) {
        TowerType towerType = tower.getType();
        MobHandler mobHandler = gameHandler.getMobHandler();
        return switch (towerType) {
            case ARCHER ->
                    new ArcherTower(mobHandler, instance, (AttackingTower) tower, towerBaseMaterial, id, owner, basePoint, facing, 1);
            case BOMBER ->
                    new BomberTower(mobHandler, gameHandler, instance, (AttackingTower) tower, towerBaseMaterial, id, owner, basePoint, facing, 1);
            case BLIZZARD ->
                    new BlizzardTower(mobHandler, instance, (AttackingTower) tower, towerBaseMaterial, id, owner, basePoint, facing, 1);
            case CHARITY ->
                    new CharityTower(instance, tower, towerBaseMaterial, id, owner, basePoint, facing, 1);
            case EARTHQUAKE ->
                    new EarthquakeTower(mobHandler, instance, (AttackingTower) tower, towerBaseMaterial, id, owner, basePoint, facing, 1);
            case LIGHTNING ->
                    new LightningTower(mobHandler, instance, (AttackingTower) tower, towerBaseMaterial, id, owner, basePoint, facing, 1);
            case NECROMANCER ->
                    new NecromancerTower(instance, (AttackingTower) tower, towerBaseMaterial, id, owner, basePoint, facing, 1);
        };
    }

    public void upgrade() {
        T oldLevel = this.level;
        this.level = (T) this.tower.getLevel(++this.levelInt);

        this.removeNonUpdatedBlocks(oldLevel, this.level);
        this.placeLevel();
    }

    // todo re-investigate using a batch to apply in the future
    // it was buggy last time it was tried
    private void placeLevel() {
        int turns = DirectionUtil.fromDirection(this.facing).getTurns();
        Rotation rotation = Rotation.values()[turns];

        this.level.getSchematic().apply(rotation, (relativePoint, block) -> {
            // Add the ID_TAG with the tower's ID to each block
            block = block.withTag(ID_TAG, this.id);

            this.instance.setBlock(this.basePoint.add(relativePoint), block);
        });
    }

    private void placeBase() {
        int checkDistance = this.tower.getType().getSize().getCheckDistance();
        for (int x = this.basePoint.blockX() - checkDistance; x <= this.basePoint.blockX() + checkDistance; x++) {
            for (int z = this.basePoint.blockZ() - checkDistance; z <= this.basePoint.blockZ() + checkDistance; z++) {
                this.instance.setBlock(x, this.basePoint.blockY(), z, this.towerBaseMaterial.block().withTag(ID_TAG, this.id));
            }
        }
    }

    private void removeNonUpdatedBlocks(TowerLevel oldLevel, TowerLevel newLevel) {
        int turns = DirectionUtil.fromDirection(this.facing).getTurns();
        Rotation rotation = Rotation.values()[turns];

        Set<Point> oldRelativePoints = SchemUtils.getRelativeBlockPoints(rotation, oldLevel.getSchematic());
        Set<Point> newRelativePoints = SchemUtils.getRelativeBlockPoints(rotation, newLevel.getSchematic());

        for (Point relativePoint : oldRelativePoints) {
            if (!newRelativePoints.contains(relativePoint)) {
                this.instance.setBlock(this.basePoint.add(relativePoint), Block.AIR);
            }
        }
    }

    public void destroy() {
        Set<Point> relativePositions = this.getTower().getLevels().values().stream()
                .filter(towerLevel -> towerLevel.getLevel() <= this.levelInt)
                .map(TowerLevel::getSchematic)
                .map(schem -> SchemUtils.getRelativeBlockPoints(Rotation.NONE, schem))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        for (Point relativePos : relativePositions) {
            this.instance.setBlock(this.basePoint.add(relativePos), Block.AIR);
        }

        // set the base back to normal
        this.normaliseBase();
    }

    /**
     * returns the blocks below the tower to normal, removing their ID_TAG property.
     */
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

    public @NotNull GameUser getOwner() {
        return this.owner;
    }
}
