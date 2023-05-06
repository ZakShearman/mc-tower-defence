package pink.zak.minestom.towerdefence.enums;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public enum Team {
    BLUE(NamedTextColor.AQUA, "aqua", (byte) 0),
    RED(NamedTextColor.RED, "red", (byte) 1);

    private final @NotNull TextColor color;
    private final @NotNull String miniMessageColor;
    private final byte id;

    Team(@NotNull TextColor color, @NotNull String miniMessageColor, byte id) {
        this.color = color;
        this.miniMessageColor = miniMessageColor;
        this.id = id;
    }

    public @NotNull TextColor getColor() {
        return this.color;
    }

    public @NotNull String getMiniMessageColor() {
        return this.miniMessageColor;
    }

    public byte getId() {
        return this.id;
    }
}
