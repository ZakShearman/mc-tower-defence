package pink.zak.minestom.towerdefence.world;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;

public class WorldLoader {
    public static final NamespaceID DIMENSION_TYPE_ID = NamespaceID.from("towerdefence", "dimension_type");
    public static final DynamicRegistry.Key<DimensionType> DIMENSION_TYPE_KEY = DynamicRegistry.Key.of(DIMENSION_TYPE_ID);

    private static final DimensionType DIMENSION_TYPE = DimensionType.builder()
            .fixedTime(1000L)
            .hasSkylight(true)
            .build();

    public TowerDefenceInstance load() {
        DynamicRegistry<DimensionType> dimensionRegistry = MinecraftServer.getDimensionTypeRegistry();
        dimensionRegistry.register(DIMENSION_TYPE_ID, DIMENSION_TYPE);

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        TowerDefenceInstance instance = new TowerDefenceInstance(DIMENSION_TYPE, "world");
        instanceManager.registerInstance(instance);

        instance.setChunkLoader(IChunkLoader.noop());

        return instance;
    }
}
