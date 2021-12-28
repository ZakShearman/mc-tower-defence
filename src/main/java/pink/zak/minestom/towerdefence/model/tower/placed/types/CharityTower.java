package pink.zak.minestom.towerdefence.model.tower.placed.types;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Direction;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.model.tower.Tower;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;

public class CharityTower extends PlacedTower {

    public CharityTower(Instance instance, Tower tower, Material towerPlaceMaterial, short id, GameUser owner, Point baseBlock, Direction facing, int level) {
        super(instance, tower, towerPlaceMaterial, id, owner, baseBlock, facing, level);
    }
}
