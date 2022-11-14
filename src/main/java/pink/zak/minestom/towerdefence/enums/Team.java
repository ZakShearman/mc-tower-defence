package pink.zak.minestom.towerdefence.enums;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public enum Team {
    BLUE(NamedTextColor.AQUA, (byte) 0),
    RED(NamedTextColor.RED, (byte) 1);

    private final @NotNull TextColor color;
    private final byte id;

    Team(@NotNull TextColor color, byte id) {
        this.color = color;
        this.id = id;
    }

    public @NotNull TextColor getColor() {
        return this.color;
    }

    public byte getId() {
        return this.id;
    }
}
