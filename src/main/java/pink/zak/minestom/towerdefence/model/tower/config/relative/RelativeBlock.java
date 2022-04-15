package pink.zak.minestom.towerdefence.model.tower.config.relative;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Direction;

import java.util.HashMap;
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

    public RelativeBlock(JsonObject jsonObject) {
        this.xOffset = jsonObject.get("xOffset").getAsInt();
        this.yOffset = jsonObject.get("yOffset").getAsInt();
        this.zOffset = jsonObject.get("zOffset").getAsInt();
        this.block = getBlockFromJson(jsonObject.get("block").getAsJsonObject());
    }

    public static Set<RelativeBlock> setFromJson(JsonArray jsonArray) {
        return StreamSupport.stream(jsonArray.spliterator(), true)
            .map(JsonElement::getAsJsonObject)
            .map(RelativeBlock::new)
            .collect(Collectors.toUnmodifiableSet());
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
        Map<String, String> map = new HashMap<>();

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
}
