package pink.zak.minestom.towerdefence.utils;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.utils.Direction;

public enum DirectionUtil {
    NORTH(Direction.NORTH, 0),
    EAST(Direction.EAST, 1),
    SOUTH(Direction.SOUTH, 2),
    WEST(Direction.WEST, 3);

    private final Direction direction;
    private final int turns;

    DirectionUtil(Direction direction, int turns) {
        this.direction = direction;
        this.turns = turns;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public int getTurns() {
        return this.turns;
    }

    public DirectionUtil rotate(int turns) {
        if (turns == 0) return this;
        int result = (this.turns + turns) % 4;
        return DirectionUtil.fromTurns(result);
    }

    public static DirectionUtil fromDirection(Direction direction) {
        for (DirectionUtil directionUtil : values()) {
            if (directionUtil.getDirection() == direction) {
                return directionUtil;
            }
        }
        return null;
    }

    public static DirectionUtil fromTurns(int turns) {
        for (DirectionUtil directionUtil : values()) {
            if (directionUtil.getTurns() == turns) {
                return directionUtil;
            }
        }
        return null;
    }

    public static Pos add(Pos pos, Direction direction, double amount) {
        return switch (direction) {
            case NORTH -> pos.sub(0, 0, amount);
            case EAST -> pos.add(amount, 0, 0);
            case SOUTH -> pos.add(0, 0, amount);
            case WEST -> pos.sub(amount, 0, 0);
            case UP -> pos.add(0, amount, 0);
            case DOWN -> pos.sub(0, amount, 0);
        };
    }

    public static Vec createVec(Direction direction, double magnitude) {
        return switch (direction) {
            case NORTH -> new Vec(0, 0, -magnitude);
            case EAST -> new Vec(magnitude, 0, 0);
            case SOUTH -> new Vec(0, 0, magnitude);
            case WEST -> new Vec(-magnitude, 0, 0);
            case UP -> new Vec(0, magnitude, 0);
            case DOWN -> new Vec(0, -magnitude, 0);
        };
    }

    public static float getYaw(Direction direction) {
        return switch (direction) {
            case NORTH -> 180;
            case EAST -> -90;
            case SOUTH -> 0;
            case WEST -> 90;
            default ->
                    throw new IllegalArgumentException("Direction must be NORTH, EAST, SOUTH or WEST. Provided direction was " + direction);
        };
    }
    }
