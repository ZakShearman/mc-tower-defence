package pink.zak.minestom.towerdefence.utils.properties.converter;

import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.utils.properties.Rotator;

import java.util.Map;

public class DefaultRotationRotator implements Rotator {

    @Override
    public void rotate(@NotNull Map<String, String> properties, int turns) {
        int currentValue = Integer.parseInt(properties.get("rotation"));
        int modification = turns * 4;
        int newValue = (currentValue + modification) % 16;
        properties.put("rotation", String.valueOf(newValue));
    }
}
