package pink.zak.minestom.towerdefence.model.tower;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public record TowerLevel(String name, int level, int cost, int fireDelay, double range, List<String> description,
                         Set<RelativeBlock> relativeBlocks) {

    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        JsonArray blockArray = new JsonArray();
        JsonArray descriptionArray = new JsonArray();
        for (RelativeBlock block : this.relativeBlocks)
            blockArray.add(block.toJsonObject());
        for (String line : this.description)
            descriptionArray.add(line);

        jsonObject.addProperty("name", this.name);
        jsonObject.addProperty("level", this.level);
        jsonObject.addProperty("cost", this.cost);
        jsonObject.addProperty("fireDelay", this.fireDelay);
        jsonObject.addProperty("range", this.range);
        jsonObject.add("description", descriptionArray);
        jsonObject.add("relativeBlocks", blockArray);

        return jsonObject;
    }

    public static TowerLevel fromJsonObject(JsonObject jsonObject) {
        String name = jsonObject.get("name").getAsString();
        int level = jsonObject.get("level").getAsInt();
        int cost = jsonObject.get("cost").getAsInt();
        int fireDelay = jsonObject.get("fireDelay").getAsInt();
        double range = jsonObject.get("range").getAsDouble();

        List<String> description = StreamSupport.stream(jsonObject.get("description").getAsJsonArray().spliterator(), true)
            .map(JsonElement::getAsString).toList();
        Set<RelativeBlock> relativeBlocks = StreamSupport.stream(jsonObject.get("relativeBlocks").getAsJsonArray().spliterator(), true)
            .map(JsonElement::getAsJsonObject)
            .map(RelativeBlock::fromJson)
            .collect(Collectors.toUnmodifiableSet());

        return new TowerLevel(name, level, cost, fireDelay, range * range, description, relativeBlocks);
    }
}
