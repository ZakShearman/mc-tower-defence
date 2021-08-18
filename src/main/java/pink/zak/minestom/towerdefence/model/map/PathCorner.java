package pink.zak.minestom.towerdefence.model.map;

import com.google.gson.JsonObject;
import net.minestom.server.utils.Direction;

public record PathCorner(Direction direction, int distance) {

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("direction", this.direction.toString());
        jsonObject.addProperty("distance", this.distance);
        return jsonObject;
    }

    public static PathCorner fromJson(JsonObject jsonObject) {
        Direction direction = Direction.valueOf(jsonObject.get("direction").getAsString());
        int distance = jsonObject.get("distance").getAsInt();
        return new PathCorner(direction, distance);
    }
}
