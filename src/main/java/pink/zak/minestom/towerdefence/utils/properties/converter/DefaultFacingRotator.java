package pink.zak.minestom.towerdefence.utils.properties.converter;

import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.utils.DirectionUtil;
import pink.zak.minestom.towerdefence.utils.properties.Rotator;

import java.util.Map;

public class DefaultFacingRotator implements Rotator {

    @Override
    public void rotate(@NotNull Map<String, String> properties, int turns) {
        String originalValue = properties.get("facing");
        properties.put("facing", this.evaluateFacing(originalValue, turns));
    }

    private String evaluateFacing(String original, int turns) {
        Direction originalDirection = Direction.valueOf(original.toUpperCase());
        if (turns == 0 || originalDirection == Direction.UP || originalDirection == Direction.DOWN) return original;

        DirectionUtil directionUtil = DirectionUtil.fromDirection(originalDirection);

        return directionUtil.rotate(turns).getDirection().name().toLowerCase();
    }
}
