package pink.zak.minestom.towerdefence.model.tower.config.towers.level;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.relative.RelativePoint;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class LightningTowerLevel extends AttackingTowerLevel {
    private final RelativePoint relativeCastPoint; // Where the particles will connect to and then fire from
    private final Set<RelativePoint> relativeSpawnPoints; // Where the particles will spawn from

    public LightningTowerLevel(@NotNull JsonObject jsonObject) {
        super("Lightning", jsonObject);
        this.relativeSpawnPoints = StreamSupport.stream(jsonObject.get("relativeSpawnPoints")
                        .getAsJsonArray().spliterator(), true)
                .map(JsonElement::getAsJsonObject)
                .map(RelativePoint::new)
                .collect(Collectors.toUnmodifiableSet());

        this.relativeCastPoint = new RelativePoint(jsonObject.get("relativeCastPoint").getAsJsonObject());
    }

    public RelativePoint getRelativeCastPoint() {
        return this.relativeCastPoint;
    }

    public Set<RelativePoint> getRelativeSpawnPoints() {
        return this.relativeSpawnPoints;
    }
}
