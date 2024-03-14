package pink.zak.minestom.towerdefence.utils;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public class Hologram extends Entity {

    public Hologram(@NotNull Instance instance, @NotNull Pos pos, @NotNull Component text) {
        super(EntityType.TEXT_DISPLAY);

        this.setInstance(instance, pos);
        this.setText(text);
    }

    public void setText(@NotNull Component text) {
        ((TextDisplayMeta) this.entityMeta).setText(text);
    }
}
