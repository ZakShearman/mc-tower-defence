package pink.zak.minestom.towerdefence.model.map;

import com.google.gson.JsonObject;
import net.minestom.server.utils.Direction;

public record PathCorner(Direction direction, int distance, boolean modify, boolean multiplyModifier,
                         boolean negativeModifier) {

    public static PathCorner fromJson(JsonObject jsonObject) {
        Direction direction = Direction.valueOf(jsonObject.get("direction").getAsString());
        int distance = jsonObject.get("distance").getAsInt();
        boolean modify = jsonObject.get("modify").getAsBoolean();
        boolean multiplyModifier = jsonObject.get("multiplyModifier").getAsBoolean();
        boolean negativeModifier = jsonObject.get("negativeModifier").getAsBoolean();
        return new PathCorner(direction, distance, modify, multiplyModifier, negativeModifier);
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("direction", this.direction.toString());
        jsonObject.addProperty("distance", this.distance);
        jsonObject.addProperty("modify", this.modify);
        jsonObject.addProperty("multiplyModifier", this.multiplyModifier);
        jsonObject.addProperty("negativeModifier", this.negativeModifier);
        return jsonObject;
    }
}
