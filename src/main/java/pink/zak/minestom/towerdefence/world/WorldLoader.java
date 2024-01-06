package pink.zak.minestom.towerdefence.world;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.DimensionTypeManager;

public class WorldLoader {
    private static final DimensionType DIMENSION_TYPE = DimensionType.builder(NamespaceID.from("towerdefence:main"))
            .fixedTime(1000L)
            .skylightEnabled(true)
            .build();

    public TowerDefenceInstance load() {
        DimensionTypeManager dimensionTypeManager = MinecraftServer.getDimensionTypeManager();
        dimensionTypeManager.addDimension(DIMENSION_TYPE);

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        TowerDefenceInstance instance = new TowerDefenceInstance(DIMENSION_TYPE, "world");
        instanceManager.registerInstance(instance);

        // Remove the chunk loader to save memory (like 50MB, 7MB per most chunks!)
        instance.setChunkLoader(null);

        return instance;
    }
}
