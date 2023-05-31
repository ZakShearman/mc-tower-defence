package pink.zak.minestom.towerdefence.cache;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minestom.server.coordinate.Vec;
import pink.zak.minestom.towerdefence.TowerDefenceModule;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.StreamSupport;

public class DamageIndicatorCache {
    private static final Path FILE_PATH = Path.of("precalculated/damageIndicators.json");
    private final List<Vec> preCalculatedVelocity;

    public DamageIndicatorCache(TowerDefenceModule plugin) {
        try (BufferedReader reader = Files.newBufferedReader(FILE_PATH)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            this.preCalculatedVelocity = this.parseVelocity(jsonObject);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load damage indicators", e);
        }
    }

    public Vec[] getPreCalculatedVelocity() {
        return this.preCalculatedVelocity.toArray(new Vec[0]);
    }

    private List<Vec> parseVelocity(JsonObject jsonObject) {
        return StreamSupport.stream(jsonObject.get("velocity").getAsJsonArray()
                        .spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .map(json -> new Vec(
                        json.get("x").getAsDouble(),
                        json.get("y").getAsDouble(),
                        json.get("z").getAsDouble()
                )).toList();
    }
}
