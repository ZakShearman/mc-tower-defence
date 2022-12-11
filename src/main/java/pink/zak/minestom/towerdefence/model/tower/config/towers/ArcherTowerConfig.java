package pink.zak.minestom.towerdefence.model.tower.config.towers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTower;
import pink.zak.minestom.towerdefence.model.tower.config.relative.RelativePoint;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ArcherTowerConfig extends AttackingTower {
    private final @NotNull Set<RelativePoint> relativeFiringPoints;

    public ArcherTowerConfig(JsonObject jsonObject, Map<Integer, JsonObject> levelJson) {
        super(jsonObject, levelJson);

        Set<RelativePoint> points = new HashSet<>();
        for (JsonElement relativePoint : jsonObject.get("relativeFiringPoints").getAsJsonArray()) {
            points.add(new RelativePoint(relativePoint.getAsJsonObject()));
        }
        this.relativeFiringPoints = Collections.unmodifiableSet(points);
    }

    public @NotNull Set<RelativePoint> getRelativeFiringPoints() {
        return this.relativeFiringPoints;
    }
}
