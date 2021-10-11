package pink.zak.minestom.towerdefence.cache;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minestom.server.coordinate.Vec;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.utils.FileUtils;

import java.util.List;

public class DamageIndicatorCache {
    private final List<Vec> preCalculatedVelocity;

    public DamageIndicatorCache(TowerDefencePlugin plugin) {
        JsonObject jsonObject = FileUtils.resourceToJsonObject(plugin, "precalculated/damageindicators.json");
        this.preCalculatedVelocity = this.parseVelocity(jsonObject);
    }

    public List<Vec> getPreCalculatedVelocity() {
        return this.preCalculatedVelocity;
    }

    private List<Vec> parseVelocity(JsonObject jsonObject) {
        JsonArray velocityArray = jsonObject.get("velocity").getAsJsonArray();
        ImmutableList.Builder<Vec> listBuilder = ImmutableList.builder();

        for (int i = 0; i < velocityArray.size(); i++) {
            JsonObject vecObject = velocityArray.get(i).getAsJsonObject();
            Vec vec = new Vec(
                vecObject.get("x").getAsDouble(),
                vecObject.get("y").getAsDouble(),
                vecObject.get("z").getAsDouble()
            );
            listBuilder.add(vec);
        }
        return listBuilder.build();
    }
}
