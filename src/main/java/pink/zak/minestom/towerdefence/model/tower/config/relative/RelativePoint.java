package pink.zak.minestom.towerdefence.model.tower.config.relative;

import com.google.gson.JsonObject;
import net.minestom.server.coordinate.Point;

public class RelativePoint {
    protected final double xOffset;
    protected final double yOffset;
    protected final double zOffset;

    public RelativePoint(JsonObject jsonObject) {
        this.xOffset = jsonObject.get("xOffset").getAsDouble();
        this.yOffset = jsonObject.get("yOffset").getAsDouble();
        this.zOffset = jsonObject.get("zOffset").getAsDouble();
    }

    public double getXOffset() {
        return this.xOffset;
    }

    public double getYOffset() {
        return this.yOffset;
    }

    public double getZOffset() {
        return this.zOffset;
    }

    public Point apply(Point point) {
        return point.add(this.xOffset, this.yOffset, this.zOffset);
    }
}
