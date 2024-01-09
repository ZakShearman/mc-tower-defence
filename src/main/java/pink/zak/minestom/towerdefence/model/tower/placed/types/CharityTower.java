package pink.zak.minestom.towerdefence.model.tower.placed.types;

import net.minestom.server.coordinate.Point;
import net.minestom.server.utils.Direction;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.config.towers.level.CharityTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

public final class CharityTower extends PlacedTower<CharityTowerLevel> {

    public CharityTower(TowerDefenceInstance instance, Tower tower, int id, GameUser owner, Point basePoint, Direction facing, int level) {
        super(instance, tower, id, owner, basePoint, facing, level);
    }

}
