package pink.zak.minestom.towerdefence.model.tower.placed;

import net.hollowcube.schem.Rotation;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.types.BlizzardTower;
import pink.zak.minestom.towerdefence.model.tower.placed.types.BomberTower;
import pink.zak.minestom.towerdefence.model.tower.placed.types.CharityTower;
import pink.zak.minestom.towerdefence.model.tower.placed.types.EarthquakeTower;
import pink.zak.minestom.towerdefence.model.tower.placed.types.LightningTower;
import pink.zak.minestom.towerdefence.model.tower.placed.types.NecromancerTower;
import pink.zak.minestom.towerdefence.model.tower.placed.types.ScorcherTower;
import pink.zak.minestom.towerdefence.model.tower.placed.types.archer.ArcherTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.ui.tower.TowerManagementUI;
import pink.zak.minestom.towerdefence.utils.DirectionUtil;
import pink.zak.minestom.towerdefence.utils.SchemUtils;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class PlacedTower<T extends TowerLevel> {
    public static final Tag<Integer> ID_TAG = Tag.Integer("towerId");

    protected final @NotNull GameHandler gameHandler;

    protected final @NotNull Tower configuration;

    protected final int id;
    protected final @NotNull Point basePoint;
    protected final @NotNull Direction facing;
    protected final @NotNull GameUser owner;

    protected @NotNull T level;

    protected PlacedTower(@NotNull GameHandler gameHandler, Tower configuration, int id, @NotNull GameUser owner, @NotNull Point basePoint, @NotNull Direction facing, int level) {
        this.gameHandler = gameHandler;

        this.configuration = configuration;

        this.id = id;
        this.basePoint = basePoint;
        this.facing = facing;
        this.owner = owner;

        this.level = (T) configuration.getLevel(level);

        this.placeLevel();
        this.placeBase();
    }

    public static PlacedTower<?> create(GameHandler gameHandler, Tower tower, int id, GameUser owner, Point basePoint, Direction facing) {
        return switch (tower.getType()) {
            case ARCHER -> new ArcherTower(gameHandler, (AttackingTower) tower, id, owner, basePoint, facing, 1);
            case BOMBER -> new BomberTower(gameHandler, (AttackingTower) tower, id, owner, basePoint, facing, 1);
            case BLIZZARD -> new BlizzardTower(gameHandler, (AttackingTower) tower, id, owner, basePoint, facing, 1);
            case CHARITY -> new CharityTower(gameHandler, tower, id, owner, basePoint, facing, 1);
            case EARTHQUAKE -> new EarthquakeTower(gameHandler, (AttackingTower) tower, id, owner, basePoint, facing, 1);
            case LIGHTNING -> new LightningTower(gameHandler, (AttackingTower) tower, id, owner, basePoint, facing, 1);
            case NECROMANCER -> new NecromancerTower(gameHandler, (AttackingTower) tower, id, owner, basePoint, facing, 1);
            case SCORCHER -> new ScorcherTower(gameHandler, (AttackingTower) tower, id, owner, basePoint, facing, 1);
        };
    }

    public void upgrade(int level, @Nullable GameUser user) {
        if (level == this.level.asInteger()) throw new IllegalStateException("Cannot upgrade to the same level");
        TowerLevel targetLevel = this.configuration.getLevel(level);
        if (targetLevel == null) throw new IllegalStateException("Cannot upgrade to a level that doesn't exist");

        if (user != null) {
            // calculate cost of upgrade
            int cost = this.getCost(targetLevel);

            // check if the user can afford the upgrade
            if (user.getCoins() < cost) return;

            // charge user for upgrade
            user.updateCoins(balance -> balance - cost);
        }

        T oldLevel = this.level;
        this.level = (T) targetLevel;

        // update management UI
        for (GameUser player : this.gameHandler.getTeamUsers(this.owner.getTeam())) {
            Inventory inventory = player.getPlayer().getOpenInventory();
            if (inventory == null) continue;

            if (inventory instanceof TowerManagementUI ui) ui.refresh();
        }

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

            this.gameHandler.getInstance().setBlock(this.basePoint.add(relativePoint), block);
        });
    }

    private void placeBase() {
        int checkDistance = this.configuration.getType().getSize().getCheckDistance();
        TowerDefenceInstance instance = this.gameHandler.getInstance();
        Block baseBlock = instance.getTowerMap().getTowerBaseMaterial().block().withTag(ID_TAG, this.id);

        for (int x = this.basePoint.blockX() - checkDistance; x <= this.basePoint.blockX() + checkDistance; x++) {
            for (int z = this.basePoint.blockZ() - checkDistance; z <= this.basePoint.blockZ() + checkDistance; z++) {
                instance.setBlock(x, this.basePoint.blockY(), z, baseBlock);
            }
        }
    }

    private void removeNonUpdatedBlocks(TowerLevel oldLevel, TowerLevel newLevel) {
        int turns = DirectionUtil.fromDirection(this.facing).getTurns();
        Rotation rotation = Rotation.values()[turns];

        Set<Point> oldRelativePoints = SchemUtils.getRelativeBlockPoints(rotation, oldLevel.getSchematic());
        Set<Point> newRelativePoints = SchemUtils.getRelativeBlockPoints(rotation, newLevel.getSchematic());

        TowerDefenceInstance instance = this.gameHandler.getInstance();
        for (Point relativePoint : oldRelativePoints) {
            if (!newRelativePoints.contains(relativePoint)) {
                instance.setBlock(this.basePoint.add(relativePoint), Block.AIR);
            }
        }
    }

    public void destroy() {
        Set<Point> relativePositions = this.getConfiguration().getLevels().stream()
                .filter(level -> level.asInteger() <= this.level.asInteger())
                .map(TowerLevel::getSchematic)
                .map(schem -> SchemUtils.getRelativeBlockPoints(Rotation.NONE, schem))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        TowerDefenceInstance instance = this.gameHandler.getInstance();
        for (Point relativePos : relativePositions) {
            instance.setBlock(this.basePoint.add(relativePos), Block.AIR);
        }

        // set the base back to normal
        this.normaliseBase();
    }

    /**
     * returns the blocks below the tower to normal, removing their ID_TAG property.
     */
    private void normaliseBase() {
        TowerDefenceInstance instance = this.gameHandler.getInstance();
        int checkDistance = this.configuration.getType().getSize().getCheckDistance();
        for (int x = this.basePoint.blockX() - checkDistance; x <= this.basePoint.blockX() + checkDistance; x++) {
            for (int z = this.basePoint.blockZ() - checkDistance; z <= this.basePoint.blockZ() + checkDistance; z++) {
                instance.setBlock(x, this.basePoint.blockY(), z, instance.getTowerMap().getTowerBaseMaterial().block());
            }
        }
    }

    public int getCost(@NotNull TowerLevel level) {
        int currentLevel = this.level.asInteger();
        int cost = 0;

        for (int i = currentLevel + 1; i <= level.asInteger(); i++) {
            TowerLevel l = this.configuration.getLevel(i);
            if (l == null) throw new IllegalStateException("Tower " + this.configuration.getName() + " is missing level " + i);
            cost += l.getCost();
        }

        return cost;
    }

    public @NotNull Tower getConfiguration() {
        return this.configuration;
    }

    public int getId() {
        return this.id;
    }

    public @NotNull Point getBasePoint() {
        return this.basePoint;
    }

    public @NotNull Direction getFacing() {
        return this.facing;
    }

    public @NotNull T getLevel() {
        return this.level;
    }

    public @NotNull GameUser getOwner() {
        return this.owner;
    }
}
