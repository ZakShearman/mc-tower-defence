package pink.zak.minestom.towerdefence.game.handlers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.color.Color;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemHideFlag;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.PotionMeta;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;
import pink.zak.minestom.towerdefence.model.user.settings.FlySpeed;
import pink.zak.minestom.towerdefence.model.user.settings.HealthDisplayMode;
import pink.zak.minestom.towerdefence.model.user.settings.ParticleThickness;

import java.util.ArrayList;
import java.util.List;

public class UserSettingsMenuHandler {
    private static final @NotNull Component MENU_TITLE = Component.text("User Settings", NamedTextColor.DARK_GRAY);

    private static final @NotNull ItemStack SETTINGS_ITEM = ItemStack.builder(Material.COMMAND_BLOCK_MINECART)
            .displayName(Component.text("User Settings", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
            .build();

    private final @NotNull TowerDefenceModule plugin;

    public UserSettingsMenuHandler(@NotNull TowerDefenceModule plugin) {
        this.plugin = plugin;
    }

    public static @NotNull ItemStack getMenuItem() {
        return SETTINGS_ITEM;
    }

    public void onGameStart() {
        this.plugin.getEventNode().addListener(PlayerUseItemEvent.class, event -> {
            if (this.plugin.getGameState() == GameState.GAME && event.getItemStack().material() == Material.COMMAND_BLOCK_MINECART) {
                this.createGui((TDPlayer) event.getPlayer());
            }
        });
        this.startListener();
    }

    private void createGui(@NotNull TDPlayer player) {
        Inventory inventory = new Inventory(InventoryType.CHEST_1_ROW, MENU_TITLE);
        inventory.setItemStack(1, this.createDamageIndicatorsItem(player));
        inventory.setItemStack(3, this.createHealthDisplayItem(player));
        inventory.setItemStack(5, this.createFlySpeedItem(player));
        inventory.setItemStack(7, this.createThinParticlesItem(player));

        player.openInventory(inventory);
    }

    private void startListener() {
        this.plugin.getEventNode().addListener(InventoryPreClickEvent.class, event -> {
            Inventory inventory = event.getInventory();
            if (inventory == null || inventory.getTitle() != MENU_TITLE)
                return;
            event.setCancelled(true);

            TDPlayer player = (TDPlayer) event.getPlayer();
            switch (event.getSlot()) {
                case 1 -> {
                    player.setDamageIndicators(!player.isDamageIndicators());
                    inventory.setItemStack(1, this.createDamageIndicatorsItem(player));
                }
                case 3 -> {
                    player.setHealthMode(player.getHealthMode().next());
                    inventory.setItemStack(3, this.createHealthDisplayItem(player));
                }
                case 5 -> {
                    player.setFlySpeed(player.getFlySpeed().next());
                    player.setFlyingSpeed(player.getFlySpeed().getSpeed());
                    inventory.setItemStack(5, this.createFlySpeedItem(player));
                }
                case 7 -> {
                    player.setParticleThickness(player.getParticleThickness().next());
                    inventory.setItemStack(7, this.createThinParticlesItem(player));
                }
            }
        });
    }

    private @NotNull ItemStack createDamageIndicatorsItem(@NotNull TDPlayer user) {
        return ItemStack.builder(Material.OAK_SIGN)
                .displayName(Component.text("Show Damage Indicators", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.text("> enabled", user.isDamageIndicators() ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false),
                        Component.text("> disabled", !user.isDamageIndicators() ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)
                )
                .build();
    }

    private @NotNull ItemStack createHealthDisplayItem(@NotNull TDPlayer user) {
        HealthDisplayMode healthMode = user.getHealthMode();
        List<Component> lore = new ArrayList<>();

        for (HealthDisplayMode loopHealthMode : HealthDisplayMode.values())
            lore.add(Component.text("> " + loopHealthMode.toString().toLowerCase().replace('_', ' '),
                    loopHealthMode == healthMode ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));

        return ItemStack.builder(Material.POTION)
                .meta(PotionMeta.class, builder -> builder
                        .color(new Color(NamedTextColor.RED))
                        .hideFlag(ItemHideFlag.HIDE_POTION_EFFECTS)
                ).displayName(Component.text("Health Display Mode", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
                .lore(lore)
                .build();
    }

    private @NotNull ItemStack createFlySpeedItem(@NotNull TDPlayer user) {
        FlySpeed flySpeed = user.getFlySpeed();
        List<Component> lore = new ArrayList<>();

        for (FlySpeed loopFlySpeed : FlySpeed.values())
            lore.add(Component.text("> " + loopFlySpeed.toString().toLowerCase().replace('_', ' ') + " (" + loopFlySpeed.getSpeed() + ")",
                    loopFlySpeed == flySpeed ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));


        return ItemStack.builder(Material.FEATHER)
                .displayName(Component.text("Fly Speed", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
                .lore(lore)
                .build();
    }

    private @NotNull ItemStack createThinParticlesItem(@NotNull TDPlayer user) {
        List<Component> lore = new ArrayList<>();

        ParticleThickness thickness = user.getParticleThickness();
        for (ParticleThickness loopThickness : ParticleThickness.values())
            lore.add(Component.text("> " + loopThickness.toString().toLowerCase().replace('_', ' ') + " (" + loopThickness.getSpacing() + ")",
                    loopThickness == thickness ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));

        return ItemStack.builder(Material.REDSTONE)
                .displayName(Component.text("Thin Particles", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
                .lore(lore)
                .build();
    }
}
