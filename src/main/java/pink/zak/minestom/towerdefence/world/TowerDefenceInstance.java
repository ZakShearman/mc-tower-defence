package pink.zak.minestom.towerdefence.world;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.emortal.minestom.core.Environment;
import net.hollowcube.polar.PolarLoader;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.Direction;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.map.PathCorner;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TowerDefenceInstance extends InstanceContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TowerDefenceInstance.class);
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(new TypeToken<PreLoadWorldData>() {
            }.getType(), PreLoadWorldData.GsonConverter.INSTANCE)
            .create();

    private static final Path MAP_PATH = Path.of("maps");

    public static final Tag<String> TOWER_PATH_TAG = Tag.String("path_team");

    private final Path preLoadDataPath;

    private final Path towerMapPath;
    private final TowerMap towerMap;

    public TowerDefenceInstance(DimensionType dimensionType, String worldName) {
        super(UUID.randomUUID(), WorldLoader.DIMENSION_TYPE_KEY, (IChunkLoader) null);
        Path mapPath = MAP_PATH.resolve(worldName);

        PolarLoader loader;
        try {
            loader = new PolarLoader(mapPath.resolve("world.polar"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.setChunkLoader(loader);

        this.preLoadDataPath = mapPath.resolve("preload_data.json");
        this.towerMapPath = mapPath.resolve("map_data.json");

        this.towerMap = TowerMap.fromJson(FileUtils.fileToJsonObject(this.towerMapPath.toFile()));

        try {
            PreLoadWorldData preLoadWorldData = GSON.fromJson(new FileReader(this.preLoadDataPath.toFile()), PreLoadWorldData.class);
            this.enableAutoChunkLoad(preLoadWorldData.autoChunkLoad());
            this.loadPreloadChunks(preLoadWorldData);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        this.tagMobPath(Team.RED);
        this.tagMobPath(Team.BLUE);
    }

    private void loadPreloadChunks(PreLoadWorldData preLoadWorldData) {
        Instant start = Instant.now();

        Set<CompletableFuture<Chunk>> futures = new HashSet<>();
        for (int x = preLoadWorldData.minX(); x <= preLoadWorldData.maxX(); x++) {
            for (int z = preLoadWorldData.minZ(); z <= preLoadWorldData.maxZ(); z++) {
                futures.add(this.loadChunk(x, z));
            }
        }
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        LOGGER.info("Loaded {} chunks in {}ms", futures.size(), Instant.now().toEpochMilli() - start.toEpochMilli());
    }

    private void tagMobPath(Team team) {
        Point origin = this.towerMap.getMobSpawn(team);
        List<PathCorner> corners = this.towerMap.getCorners(team);

        Point current = origin.sub(0, 1, 0);
        for (PathCorner corner : corners) {
            Direction direction = corner.direction();
            for (int i = 0; i < corner.distance(); i++) {
                current = current.add(direction.normalX(), direction.normalY(), direction.normalZ());
                Block block = this.getBlock(current);

                Block newBlock = (Environment.isProduction() || System.getenv("TD_PROD_PATH") != null ? block : Block.BEDROCK)
                        .withTag(TOWER_PATH_TAG, team.name());

                this.setBlock(current.withY(current.y() - 1), newBlock);
            }
        }
    }

    @Override
    public @NotNull CompletableFuture<Void> saveInstance() {
        return super.saveInstance().thenAccept(unused -> this.saveTowerMapData());
    }

    public void saveTowerMapData() {
        FileUtils.saveJsonObject(this.towerMapPath, this.towerMap.toJson());
    }

    public TowerMap getTowerMap() {
        return this.towerMap;
    }
}
