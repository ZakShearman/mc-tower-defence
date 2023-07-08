package pink.zak.minestom.towerdefence.model.tower.config.towers.level;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.tower.config.AttackingTowerLevel;
import pink.zak.minestom.towerdefence.model.tower.config.relative.RelativePoint;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class EarthquakeTowerLevel extends AttackingTowerLevel {
    private final @NotNull Set<RelativePoint> relativeShulkerBoxes;
    private final int shulkerAnimationTicks;
    private final int stunTicks;

    public EarthquakeTowerLevel(@NotNull JsonObject jsonObject) {
        super("Earthquake", jsonObject);

        this.relativeShulkerBoxes = StreamSupport.stream(jsonObject.get("relativeShulkerBoxes").getAsJsonArray().spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .map(RelativePoint::new)
                .collect(Collectors.toUnmodifiableSet());

        this.shulkerAnimationTicks = jsonObject.get("shulkerAnimationTicks").getAsInt();
        this.stunTicks = jsonObject.get("stunTicks").getAsInt();
    }

    public @NotNull Set<RelativePoint> getRelativeShulkerBoxes() {
        return this.relativeShulkerBoxes;
    }

    public int getShulkerAnimationTicks() {
        return this.shulkerAnimationTicks;
    }

    public int getStunTicks() {
        return this.stunTicks;
    }
}
