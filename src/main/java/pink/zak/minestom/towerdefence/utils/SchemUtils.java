package pink.zak.minestom.towerdefence.utils;

import net.hollowcube.util.schem.Rotation;
import net.hollowcube.util.schem.Schematic;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class SchemUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchemUtils.class);

    public static Set<Point> getRelativeBlockPoints(@NotNull Rotation rotation, @NotNull Schematic schem) {
        Set<Point> points = new HashSet<>();
        schem.apply(rotation, (point, block) -> {
            if (block.isAir()) return;
            points.add(point);
        });
        return points;
    }
}
