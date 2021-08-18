package pink.zak.minestom.towerdefence.model.tower;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public record TowerLevel(int level, int cost, String name, List<String> description, Set<RelativeBlock> relativeBlocks) {

    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        JsonArray blockArray = new JsonArray();
        JsonArray descriptionArray = new JsonArray();
        for (RelativeBlock block : this.relativeBlocks)
            blockArray.add(block.toJsonObject());
        for (String line : this.description)
            descriptionArray.add(line);

        jsonObject.addProperty("level", this.level);
        jsonObject.addProperty("cost", this.cost);
        jsonObject.addProperty("name", this.name);
        jsonObject.add("description", descriptionArray);
        jsonObject.add("relativeBlocks", blockArray);

        return jsonObject;
    }

    public static TowerLevel fromJsonObject(JsonObject jsonObject) {
        int level = jsonObject.get("level").getAsInt();
        int cost = jsonObject.get("cost").getAsInt();
        String name = jsonObject.get("name").getAsString();
        List<String> description = StreamSupport.stream(jsonObject.get("description").getAsJsonArray().spliterator(), true)
            .map(JsonElement::getAsString).toList();
        Set<RelativeBlock> relativeBlocks = StreamSupport.stream(jsonObject.get("relativeBlocks").getAsJsonArray().spliterator(), true)
            .map(JsonElement::getAsJsonObject)
            .map(RelativeBlock::fromJson)
            .collect(Collectors.toUnmodifiableSet());

        return new TowerLevel(level, cost, name, description, relativeBlocks);
    }
}
