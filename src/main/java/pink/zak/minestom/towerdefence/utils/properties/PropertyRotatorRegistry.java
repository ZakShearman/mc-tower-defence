package pink.zak.minestom.towerdefence.utils.properties;

import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import pink.zak.minestom.towerdefence.utils.properties.stores.DefaultBlockRotatorStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class PropertyRotatorRegistry {
    private static final Map<Material, BlockRotatorStore> ROTATOR_STORES = new HashMap<>();
    private static final BlockRotatorStore DEFAULT_STORE = new DefaultBlockRotatorStore();

    private static final Set<Pattern> IGNORED_MATERIALS = Set.of(
            Pattern.compile(".*_slab"),
            Pattern.compile("minecraft:cauldron"),
            Pattern.compile("minecraft:tnt")
    );

    static {
//        ROTATOR_STORES.put(Material, BlockRotatorStore);
    }


    public static Block rotateProperties(Block originalBlock, int turns) {
        Map<String, String> properties = new HashMap<>(originalBlock.properties());
        if (properties.isEmpty() || IGNORED_MATERIALS.stream().anyMatch(pattern -> pattern.matcher(originalBlock.name()).matches()))
            return originalBlock;
        Material material = originalBlock.registry().material();

        boolean needsDirectionRotator = false;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String property = entry.getKey();
            if (property == null) continue;
            if (property.equals("north") || property.equals("east") || property.equals("south") || property.equals("west")) {
                needsDirectionRotator = true;
                continue;
            }

            findRotator(material, property).ifPresent(rotator -> rotator.rotate(properties, turns));
        }
        if (needsDirectionRotator)
            findRotator(material, "north", "east", "south", "west").ifPresent(rotator -> rotator.rotate(properties, turns));

        return originalBlock.withProperties(properties);
    }

    private static Optional<Rotator> findRotator(Material material, String... properties) {
        BlockRotatorStore materialStore = ROTATOR_STORES.get(material);

        if (materialStore == null) return DEFAULT_STORE.rotator(properties);

        Optional<Rotator> optionalRotator = materialStore.rotator(properties);
        return optionalRotator.isPresent() ? optionalRotator : DEFAULT_STORE.rotator(properties);
    }
}
