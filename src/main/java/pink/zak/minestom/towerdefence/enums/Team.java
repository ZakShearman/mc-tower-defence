package pink.zak.minestom.towerdefence.enums;

import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public enum Team {
    BLUE(NamedTextColor.AQUA, (byte) 0),
    RED(NamedTextColor.RED, (byte) 1);

    private final @NotNull NamedTextColor color;
    private final byte id;

    Team(@NotNull NamedTextColor color, byte id) {
        this.color = color;
        this.id = id;
    }

    public @NotNull NamedTextColor getColor() {
        return this.color;
    }

    public byte getId() {
        return this.id;
    }
}
