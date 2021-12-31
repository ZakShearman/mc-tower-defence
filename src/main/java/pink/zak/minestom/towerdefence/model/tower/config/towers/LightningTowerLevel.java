package pink.zak.minestom.towerdefence.model.tower.config.towers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.relative.RelativePoint;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class LightningTowerLevel extends AttackingTowerLevel {
    private final Set<RelativePoint> relativeCastPoints;

    public LightningTowerLevel(JsonObject jsonObject) {
        super(jsonObject);
        this.relativeCastPoints = StreamSupport.stream(jsonObject.get("relativeCastPoints")
                .getAsJsonArray().spliterator(), true)
            .map(JsonElement::getAsJsonObject)
            .map(RelativePoint::new)
            .collect(Collectors.toSet());
    }

    public Set<RelativePoint> getRelativeCastPoints() {
        return this.relativeCastPoints;
    }
}
