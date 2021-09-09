package pink.zak.minestom.towerdefence.utils.mechanic;

import com.google.common.collect.Lists;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Explosion;
import net.minestom.server.instance.Instance;

import java.util.List;

public class CustomExplosion extends Explosion {
    public static final String DONT_DESTROY_BLOCKS_KEY = "minestom:no_block_damage";

    public CustomExplosion(float centerX, float centerY, float centerZ, float strength) {
        super(centerX, centerY, centerZ, strength);
    }

    @Override
    protected List<Point> prepare(Instance instance) {
        return Lists.newArrayList();
    }
}