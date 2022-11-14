package pink.zak.minestom.towerdefence.world;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.tag.Tag;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.utils.FileUtils;
import pink.zak.minestom.towerdefence.utils.mechanic.CustomExplosion;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TowerDefenceInstance extends InstanceContainer {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(new TypeToken<PreLoadWorldData>(){}.getType(), PreLoadWorldData.GsonConverter.INSTANCE)
            .create();

    public static final Tag<String> TOWER_PATH_TAG = Tag.String("path_team");

    private final Path preLoadDataPath;

    private final Path towerMapPath;
    private final TowerMap towerMap;

    public TowerDefenceInstance(DimensionType dimensionType, String worldName) {
        super(UUID.randomUUID(), dimensionType, new AnvilLoader(worldName));
        this.enableAutoChunkLoad(false);

        this.preLoadDataPath = Path.of(worldName, "preload_data.json");

        this.towerMapPath = Path.of(worldName, "map.json");
        this.towerMap = TowerMap.fromJson(FileUtils.fileToJsonObject(this.towerMapPath.toFile()));

        this.loadAllChunks();
        this.setExplosionSupplier((centerX, centerY, centerZ, strength, additionalData) -> new CustomExplosion(centerX, centerY, centerZ, strength));
    }

    // todo add timing stats
    private void loadAllChunks() {
        try {
            PreLoadWorldData preLoadWorldData = GSON.fromJson(new FileReader(this.preLoadDataPath.toFile()), PreLoadWorldData.class);

            Set<CompletableFuture<Chunk>> futures = new HashSet<>();
            for (int x = preLoadWorldData.minX(); x <= preLoadWorldData.maxX(); x++) {
                for (int z = preLoadWorldData.minZ(); z <= preLoadWorldData.maxZ(); z++) {
                    futures.add(this.loadChunk(x, z));
                }
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // todo trace the path and add TOWER_PATH_TAG to every block

    @Override
    public @NotNull CompletableFuture<Void> saveInstance() {
        return super.saveInstance().thenAccept(unused -> this.saveTowerMapData());
    }

    public void saveTowerMapData() {
        FileUtils.saveJsonObject(this.towerMapPath, this.towerMap.toJson());
    }

    public TowerMap getTowerMap() {
        return towerMap;
    }
}
