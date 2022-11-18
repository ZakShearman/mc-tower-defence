package pink.zak.minestom.towerdefence.utils.properties.converter;

import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.utils.properties.Rotator;

import java.util.Map;

public class DefaultAxisRotator implements Rotator {

    @Override
    public void rotate(@NotNull Map<String, String> properties, int turns) {
        if (turns % 2 == 0) return; // if it's an even no of turns, it's the same - | - | - |

        String originalValue = properties.get("axis");
        properties.put("axis", this.flipAxis(originalValue));
    }

    private String flipAxis(String current) {
        return switch (current) {
            case "x" -> "z";
            case "z" -> "x";
            default -> current;
        };
    }
}
