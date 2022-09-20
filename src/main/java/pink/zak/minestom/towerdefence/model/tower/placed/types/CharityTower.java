package pink.zak.minestom.towerdefence.model.tower.placed.types;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.config.towers.CharityTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;

public class CharityTower extends PlacedTower<CharityTowerLevel> {

    public CharityTower(Instance instance, Tower tower, Material towerPlaceMaterial, int id, GameUser owner, Point baseBlock, Direction facing, int level) {
        super(instance, tower, towerPlaceMaterial, id, owner, baseBlock, facing, level);
    }
}
