package pink.zak.minestom.towerdefence.utils.properties.stores;

import pink.zak.minestom.towerdefence.utils.properties.BlockRotatorStore;
import pink.zak.minestom.towerdefence.utils.properties.Rotator;
import pink.zak.minestom.towerdefence.utils.properties.converter.DefaultAxisRotator;
import pink.zak.minestom.towerdefence.utils.properties.converter.DefaultDirectionRotator;
import pink.zak.minestom.towerdefence.utils.properties.converter.DefaultFacingRotator;
import pink.zak.minestom.towerdefence.utils.properties.converter.DefaultRotationRotator;

import java.util.List;
import java.util.Optional;

public class DefaultBlockRotatorStore implements BlockRotatorStore {
    private final Rotator facingRotator = new DefaultFacingRotator();
    private final Rotator directionRotator = new DefaultDirectionRotator();
    private final Rotator axisRotator = new DefaultAxisRotator();
    private final Rotator rotationRotator = new DefaultRotationRotator();

    @Override
    public Optional<Rotator> rotator(String property) {
        return Optional.ofNullable(switch (property) {
            case "facing" -> this.facingRotator;
            case "axis" -> this.axisRotator;
            case "rotation" -> this.rotationRotator;
            default -> null;
        });
    }

    @Override
    public Optional<Rotator> rotator(String... propertiesArray) {
        if (propertiesArray.length == 1) return this.rotator(propertiesArray[0]);
        List<String> properties = List.of(propertiesArray);

        if (properties.size() == 4 && properties.containsAll(List.of("north", "east", "south", "west")))
            return Optional.of(this.directionRotator);

        return Optional.empty();
    }
}
