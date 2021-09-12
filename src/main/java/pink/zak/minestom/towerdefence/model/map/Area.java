package pink.zak.minestom.towerdefence.model.map;

import com.google.gson.JsonObject;
import net.minestom.server.coordinate.Point;

public class Area {
    private int minX;
    private int maxX;
    private int minZ;
    private int maxZ;

    public Area(Point point1, Point point2) {
        this.minX = Math.min(point1.blockX(), point2.blockX());
        this.maxX = Math.max(point1.blockX(), point2.blockX());
        this.minZ = Math.min(point1.blockZ(), point2.blockZ());
        this.maxZ = Math.max(point1.blockZ(), point2.blockZ());
    }

    public Area(int minX, int maxX, int minZ, int maxZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    public static Area fromJson(JsonObject jsonObject) {
        int minX = jsonObject.get("minX").getAsInt();
        int maxX = jsonObject.get("maxX").getAsInt();
        int minZ = jsonObject.get("minZ").getAsInt();
        int maxZ = jsonObject.get("maxZ").getAsInt();

        return new Area(minX, maxX, minZ, maxZ);
    }

    public int getMinX() {
        return this.minX;
    }

    public void setMinX(int minX) {
        this.minX = minX;
    }

    public int getMaxX() {
        return this.maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMinZ() {
        return this.minZ;
    }

    public void setMinZ(int minZ) {
        this.minZ = minZ;
    }

    public int getMaxZ() {
        return this.maxZ;
    }

    public void setMaxZ(int maxZ) {
        this.maxZ = maxZ;
    }

    public boolean isWithin(Point pos) {
        return pos.x() < this.maxX && pos.x() > this.minX
            && pos.z() < this.maxZ && pos.z() > this.minZ;
    }

    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("minX", this.minX);
        jsonObject.addProperty("maxX", this.maxX);
        jsonObject.addProperty("minZ", this.minZ);
        jsonObject.addProperty("maxZ", this.maxZ);

        return jsonObject;
    }
}
