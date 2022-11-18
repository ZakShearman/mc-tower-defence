package pink.zak.minestom.towerdefence.utils.properties;

import java.util.Optional;

public interface BlockRotatorStore {

    Optional<Rotator> rotator(String property);

    Optional<Rotator> rotator(String... properties);
}
