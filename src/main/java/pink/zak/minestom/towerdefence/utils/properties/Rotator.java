package pink.zak.minestom.towerdefence.utils.properties;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface Rotator {

    /**
     * @param properties The original properties map
     * @param turns      each turn is a 90 deg change (clockwise), e.g north -> east
     */
    void rotate(@NotNull Map<String, String> properties, int turns);
}
