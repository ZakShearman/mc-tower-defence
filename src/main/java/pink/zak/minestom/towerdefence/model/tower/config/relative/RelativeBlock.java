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
    private final RelativePoint relativePoint;
    private final Block block;

    public RelativeBlock(JsonObject jsonObject) {
        int xOffset = jsonObject.get("xOffset").getAsInt();
        int yOffset = jsonObject.get("yOffset").getAsInt();
        int zOffset = jsonObject.get("zOffset").getAsInt();
        this.relativePoint = new RelativePoint(xOffset, yOffset, zOffset);

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
            properties = getPropertiesFromJson(jsonObject.get("properties").getAsJsonArray());
        else
            return Block.fromNamespaceId(id);

        return Block.fromNamespaceId(id).withProperties(properties);
    }

    private static Map<String, String> getPropertiesFromJson(JsonArray jsonArray) {
        Map<String, String> map = new HashMap<>();

        for (JsonElement jsonElement : jsonArray) {
            String[] split = jsonElement.getAsString().split("=");
            map.put(split[0], split[1]);
        }

        return map;
    }

    public RelativePoint getRelativePoint() {
        return this.relativePoint;
    }

    public int getXOffset(Direction facing) {
        return (int) switch (facing) {
            case NORTH -> this.relativePoint.getXOffset();
            case EAST -> -this.relativePoint.getZOffset();
            case SOUTH -> -this.relativePoint.getXOffset();
            case WEST -> this.relativePoint.getZOffset();
            default ->
                    throw new IllegalArgumentException("Direction must be NORTH, EAST, SOUTH or WEST. Provided direction was " + facing);
        };
    }

    public int getZOffset(Direction facing) {
        return (int) switch (facing) {
            case NORTH -> this.relativePoint.getZOffset();
            case EAST -> this.relativePoint.getXOffset();
            case SOUTH -> -this.relativePoint.getZOffset();
            case WEST -> -this.relativePoint.getXOffset();
            default ->
                    throw new IllegalArgumentException("Direction must be NORTH, EAST, SOUTH or WEST. Provided direction was " + facing);
        };
    }

    public int getYOffset() {
        return (int) this.relativePoint.getYOffset();
    }

    public Block getBlock() {
        return this.block;
    }
}
