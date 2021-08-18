package pink.zak.minestom.towerdefence.utils;

import net.minestom.server.utils.Direction;

public class DirectionUtils {

    public static float getYaw(Direction direction) {
        return switch (direction) {
            case NORTH -> 180;
            case EAST -> -90;
            case SOUTH -> 0;
            case WEST -> 90;
            default -> throw new IllegalArgumentException("Direction must be NORTH, EAST, SOUTH or WEST. Provided direction was " + direction);
        };
    }

    public static Direction opposite(Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.SOUTH;
            case EAST -> Direction.WEST;
            case SOUTH -> Direction.NORTH;
            case WEST -> Direction.EAST;
            case UP -> Direction.DOWN;
            case DOWN -> Direction.UP;
        };
    }
}
