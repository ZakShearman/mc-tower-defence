package pink.zak.minestom.towerdefence.utils.mechanic;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Explosion;
import net.minestom.server.instance.Instance;

import java.util.ArrayList;
import java.util.List;

public class CustomExplosion extends Explosion {

    public CustomExplosion(float centerX, float centerY, float centerZ, float strength) {
        super(centerX, centerY, centerZ, strength);
    }

    @Override
    protected List<Point> prepare(Instance instance) {
        return new ArrayList<>();
    }
}