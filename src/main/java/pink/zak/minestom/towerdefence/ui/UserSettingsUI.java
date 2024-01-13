package pink.zak.minestom.towerdefence.ui;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.color.Color;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemHideFlag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.PotionMeta;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;
import pink.zak.minestom.towerdefence.model.user.settings.FlySpeed;
import pink.zak.minestom.towerdefence.model.user.settings.HealthDisplayMode;
import pink.zak.minestom.towerdefence.model.user.settings.ParticleThickness;

public final class UserSettingsUI extends Inventory {

    public static final @NotNull ItemStack HOTBAR_ITEM = ItemStack.builder(Material.COMMAND_BLOCK_MINECART)
            .displayName(Component.text("User Settings", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
            .build();

    private static final @NotNull Component MENU_TITLE = Component.text("User Settings", NamedTextColor.DARK_GRAY);
    private static final @NotNull ItemStack BASE_DAMAGE_INDICATORS_ITEM = ItemStack.builder(Material.OAK_SIGN)
            .displayName(Component.text("Show Damage Indicators", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
            .build();
    private static final @NotNull ItemStack BASE_HEATH_DISPLAY_ITEM = ItemStack.builder(Material.POTION)
            .displayName(Component.text("Health Display Mode", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
            .meta(PotionMeta.class, meta -> meta.color(new Color(NamedTextColor.RED)).hideFlag(ItemHideFlag.HIDE_POTION_EFFECTS))
            .build();
    private static final @NotNull ItemStack BASE_FLIGHT_SPEED_ITEM = ItemStack.builder(Material.FEATHER)
            .displayName(Component.text("Fly Speed", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
            .build();
    private static final @NotNull ItemStack BASE_PARTICLE_THICKNESS_ITEM = ItemStack.builder(Material.REDSTONE)
            .displayName(Component.text("Particle Thickness", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
            .build();

    private final @NotNull TDPlayer player;

    public UserSettingsUI(@NotNull TDPlayer player) {
        super(InventoryType.CHEST_1_ROW, MENU_TITLE);
        this.player = player;

        this.setItemStack(1, this.createDamageIndicatorsItem(player.isDamageIndicators()));
        this.setItemStack(3, this.createHealthDisplayItem(player.getHealthMode()));
        this.setItemStack(5, this.createFlySpeedItem(player.getFlySpeed()));
        this.setItemStack(7, this.createParticleThicknessItem(player.getParticleThickness()));

        this.addInventoryCondition((p, slot, clickType, result) -> {
            // always cancel the event
            result.setCancel(true);

            // this is not the player who opened the inventory, fast exit
            if (!p.equals(player)) return;

            // run the click handler
            this.onClick(slot);
        });
    }

    private void onClick(int slot) {
        switch (slot) {
            case 1 -> {
                player.setDamageIndicators(!player.isDamageIndicators());
                this.setItemStack(slot, this.createDamageIndicatorsItem(player.isDamageIndicators()));
            }
            case 3 -> {
                player.setHealthMode(player.getHealthMode().next());
                this.setItemStack(slot, this.createHealthDisplayItem(player.getHealthMode()));
            }
            case 5 -> {
                player.setFlySpeed(player.getFlySpeed().next());
                player.setFlyingSpeed(player.getFlySpeed().getSpeed());
                this.setItemStack(slot, this.createFlySpeedItem(player.getFlySpeed()));
            }
            case 7 -> {
                player.setParticleThickness(player.getParticleThickness().next());
                this.setItemStack(slot, this.createParticleThicknessItem(player.getParticleThickness()));
            }
        }
    }

    private @NotNull ItemStack createDamageIndicatorsItem(boolean isDamageIndicators) {
        return BASE_DAMAGE_INDICATORS_ITEM.withLore(List.of(
                Component.text("> enabled", isDamageIndicators ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false),
                Component.text("> disabled", !isDamageIndicators ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)
        ));
    }

    private @NotNull ItemStack createHealthDisplayItem(@NotNull HealthDisplayMode healthMode) {
        return BASE_HEATH_DISPLAY_ITEM.withLore(this.createLore(healthMode, HealthDisplayMode.values()));
    }

    private @NotNull ItemStack createFlySpeedItem(@NotNull FlySpeed flySpeed) {
        return BASE_FLIGHT_SPEED_ITEM.withLore(this.createLore(flySpeed, FlySpeed.values()));
    }

    private @NotNull ItemStack createParticleThicknessItem(@NotNull ParticleThickness particleThickness) {
        return BASE_PARTICLE_THICKNESS_ITEM.withLore(this.createLore(particleThickness, ParticleThickness.values()));
    }

    private <T> @NotNull List<Component> createLore(@NotNull T value, @NotNull T[] values) {
        List<Component> lore = new ArrayList<>();
        for (T loopValue : values) {
            String name = "> " + loopValue;
            NamedTextColor colour = loopValue == value ? NamedTextColor.GREEN : NamedTextColor.RED;
            lore.add(Component.text(name, colour).decoration(TextDecoration.ITALIC, false));
        }
        return lore;
    }

}
