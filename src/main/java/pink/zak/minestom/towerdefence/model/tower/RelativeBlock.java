package pink.zak.minestom.towerdefence.model.tower;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RelativeBlock {
    // treated as if the tower is facing north
    private final int xOffset;
    private final int zOffset;
    private final int yOffset;
    private final Block block;

    public RelativeBlock(int xOffset, int zOffset, int yOffset, Block block) {
        this.xOffset = xOffset;
        this.zOffset = zOffset;
        this.yOffset = yOffset;
        this.block = block;
    }

    public static Set<RelativeBlock> setFromJson(JsonArray jsonArray) {
        return StreamSupport.stream(jsonArray.spliterator(), true)
            .map(JsonElement::getAsJsonObject)
            .map(RelativeBlock::fromJson)
            .collect(Collectors.toUnmodifiableSet());
    }

    public static RelativeBlock fromJson(JsonObject jsonObject) {
        int xOffset = jsonObject.get("xOffset").getAsInt();
        int zOffset = jsonObject.get("zOffset").getAsInt();
        int yOffset = jsonObject.get("yOffset").getAsInt();
        Block block = getBlockFromJson(jsonObject.get("block").getAsJsonObject());

        return new RelativeBlock(xOffset, zOffset, yOffset, block);
    }

    private static Block getBlockFromJson(JsonObject jsonObject) {
        String id = jsonObject.get("material").getAsString();
        Map<String, String> properties;
        if (jsonObject.has("properties"))
            properties = getPropertiesFromJson(jsonObject.get("properties").getAsJsonObject());
        else
            return Block.fromNamespaceId(id);

        return Block.fromNamespaceId(id).withProperties(properties);
    }

    private static Map<String, String> getPropertiesFromJson(JsonObject jsonObject) {
        Map<String, String> map = Maps.newHashMap();

        for (String key : jsonObject.keySet())
            map.put(key, jsonObject.get(key).getAsString());

        return map;
    }

    public int getXOffset(Direction facing) {
        return switch (facing) {
            case NORTH -> this.xOffset;
            case EAST -> -this.zOffset;
            case SOUTH -> -this.xOffset;
            case WEST -> this.zOffset;
            default -> throw new IllegalArgumentException("Direction must be NORTH, EAST, SOUTH or WEST. Provided direction was " + facing);
        };
    }

    public int getZOffset(Direction facing) {
        return switch (facing) {
            case NORTH -> this.zOffset;
            case EAST -> this.xOffset;
            case SOUTH -> -this.zOffset;
            case WEST -> -this.xOffset;
            default -> throw new IllegalArgumentException("Direction must be NORTH, EAST, SOUTH or WEST. Provided direction was " + facing);
        };
    }

    public int getYOffset() {
        return this.yOffset;
    }

    public Block getBlock() {
        return this.block;
    }

    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("xOffset", this.xOffset);
        jsonObject.addProperty("zOffset", this.zOffset);
        jsonObject.addProperty("yOffset", this.yOffset);
        jsonObject.add("block", this.getBlockAsJson());

        return jsonObject;
    }

    private JsonObject getBlockAsJson() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("material", this.block.name());
        if (!this.block.properties().isEmpty())
            jsonObject.add("properties", this.getPropertiesAsJson());

        return jsonObject;
    }

    private JsonObject getPropertiesAsJson() {
        JsonObject jsonObject = new JsonObject();

        for (Map.Entry<String, String> entry : this.block.properties().entrySet())
            jsonObject.addProperty(entry.getKey(), entry.getValue());

        return jsonObject;
    }
}
