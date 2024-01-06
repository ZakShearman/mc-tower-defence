package pink.zak.minestom.towerdefence.model.mob.living;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.prediction.DamagePrediction;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;
import pink.zak.minestom.towerdefence.utils.StringUtils;

public interface LivingTDMob {

    default @NotNull Component createNameComponent(@NotNull Player player) {
        TDPlayer tdPlayer = (TDPlayer) player;
        String health = tdPlayer.getHealthMode().resolve(this);
        TextComponent.Builder builder = Component.text()
                .append(Component.text(StringUtils.namespaceToName(this.getTDEntityType().name()) + " " + StringUtils.integerToCardinal(this.getLevel()), NamedTextColor.DARK_GREEN))
                .append(Component.text(" | ", NamedTextColor.GREEN))
                .append(Component.text(health, NamedTextColor.DARK_GREEN))
                .style(Style.style(TextDecoration.BOLD));

        return builder.build();
    }

    void updateCustomName();

    @NotNull EntityType getTDEntityType();

    int getLevel();

    float getHealth();

    float getMaxHealth();

    default boolean isDead() {
        return this.getHealth() <= 0;
    }

    double getEyeHeight();

    @NotNull Instance getInstance();

    @NotNull Pos getPosition();

    @NotNull BoundingBox getBoundingBox();
}
