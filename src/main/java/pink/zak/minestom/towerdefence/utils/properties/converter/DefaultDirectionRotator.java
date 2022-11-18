package pink.zak.minestom.towerdefence.utils.properties.converter;

import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.utils.DirectionUtil;
import pink.zak.minestom.towerdefence.utils.properties.Rotator;

import java.util.EnumMap;
import java.util.Map;

public class DefaultDirectionRotator implements Rotator {

    @Override
    public void rotate(@NotNull Map<String, String> properties, int turns) {
        Map<DirectionUtil, String> directionValues = new EnumMap<>(DirectionUtil.class);

        directionValues.put(DirectionUtil.NORTH, properties.get("north"));
        directionValues.put(DirectionUtil.EAST, properties.get("east"));
        directionValues.put(DirectionUtil.SOUTH, properties.get("south"));
        directionValues.put(DirectionUtil.WEST, properties.get("west"));

        Map<DirectionUtil, String> resultDirectionValues = new EnumMap<>(DirectionUtil.class);
        for (Map.Entry<DirectionUtil, String> entry : directionValues.entrySet()) {
            DirectionUtil directionUtil = entry.getKey();
            String value = entry.getValue();

            properties.remove(directionUtil.name().toLowerCase());
            if (value == null) continue;
            resultDirectionValues.put(directionUtil.rotate(turns), value);
        }

        for (Map.Entry<DirectionUtil, String> entry : resultDirectionValues.entrySet())
            properties.put(entry.getKey().name().toLowerCase(), entry.getValue());

    }
}
