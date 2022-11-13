package pink.zak.minestom.towerdefence.model.tower.config.relative;

import com.google.gson.JsonObject;
import net.minestom.server.coordinate.Point;

import java.util.Objects;

public class RelativePoint {
    protected final double xOffset;
    protected final double yOffset;
    protected final double zOffset;

    public RelativePoint(double xOffset, double yOffset, double zOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelativePoint that = (RelativePoint) o;
        return Double.compare(that.xOffset, xOffset) == 0 && Double.compare(that.yOffset, yOffset) == 0 && Double.compare(that.zOffset, zOffset) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(xOffset, yOffset, zOffset);
    }
}
