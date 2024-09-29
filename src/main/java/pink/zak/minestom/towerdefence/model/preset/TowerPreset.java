package pink.zak.minestom.towerdefence.model.preset;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minestom.server.coordinate.Vec;
import org.apache.commons.collections4.set.UnmodifiableSet;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.enums.TowerType;
import pink.zak.minestom.towerdefence.model.tower.TowerManager;
import pink.zak.minestom.towerdefence.model.tower.TowerPlaceResult;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.storage.TowerStorage;

import java.util.HashSet;
import java.util.Set;

public class TowerPreset {
    private final Set<@NotNull Tower> towers;

    public TowerPreset(@NotNull JsonArray json) {
        Set<Tower> towers = new HashSet<>();
        for (int i = 0; i < json.size(); i++) {
            JsonObject jsonObject = json.get(i).getAsJsonObject();
            towers.add(new Tower(jsonObject));
        }

        this.towers = UnmodifiableSet.unmodifiableSet(towers);
    }

    public TowerPreset(@NotNull Set<PlacedTower<? extends TowerLevel>> placedTowers) {
        Set<Tower> towers = new HashSet<>();
        for (PlacedTower<? extends TowerLevel> placedTower : placedTowers) {
            towers.add(new Tower(placedTower));
        }

        this.towers = UnmodifiableSet.unmodifiableSet(towers);
    }

    public void placeTowers(@NotNull TowerStorage storage, @NotNull TowerManager manager, @NotNull GameUser user) {
        for (Tower tower : this.towers) {
            tower.place(storage, manager, user);
        }
    }

    public Set<Tower> getTowers() {
        return this.towers;
    }

    public @NotNull JsonArray toJson() {
        JsonArray jsonArray = new JsonArray();
        for (Tower tower : this.towers) {
            jsonArray.add(tower.toJson());
        }

        return jsonArray;
    }

    public static class Tower {
        private final @NotNull TowerType towerType;
        private final int level;
        private final @NotNull Vec position;

        public Tower(@NotNull JsonObject jsonObject) {
            this.towerType = TowerType.valueOf(jsonObject.get("type").getAsString());
            this.level = jsonObject.get("level").getAsInt();
            this.position = new Vec(jsonObject.get("x").getAsDouble(), jsonObject.get("y").getAsDouble(), jsonObject.get("z").getAsDouble());
        }

        public Tower(@NotNull TowerType towerType, int level, @NotNull Vec position) {
            this.towerType = towerType;
            this.level = level;
            this.position = position;
        }

        public Tower(@NotNull PlacedTower<? extends TowerLevel> placedTower) {
            this(
                    placedTower.getConfiguration().getType(),
                    placedTower.getLevel().asInteger(),
                    Vec.fromPoint(placedTower.getBasePoint())
            );
        }

        public void place(@NotNull TowerStorage storage, @NotNull TowerManager manager, @NotNull GameUser user) {
            TowerPlaceResult result = manager.placeTower(storage.getTower(this.towerType), this.position, user, true);

            if (!result.isSuccessful()) {
                throw new IllegalStateException("Failed to place tower: " + result.failureReason());
            }

            if (this.level > 1) {
                result.tower().upgrade(this.level, null);
            }
        }

        public @NotNull JsonObject toJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", this.towerType.name());
            jsonObject.addProperty("level", this.level);
            jsonObject.addProperty("x", this.position.x());
            jsonObject.addProperty("y", this.position.y());
            jsonObject.addProperty("z", this.position.z());
            return jsonObject;
        }

        public @NotNull TowerType getTowerType() {
            return this.towerType;
        }

        public int getLevel() {
            return this.level;
        }

        public @NotNull Vec getPosition() {
            return this.position;
        }
    }
}
