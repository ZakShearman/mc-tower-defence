package pink.zak.minestom.towerdefence.game.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.color.Color;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.PotionMeta;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.cache.TDUserCache;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.model.user.TDUser;
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

    private final @NotNull TowerDefencePlugin plugin;
    private final @NotNull TDUserCache userCache;

    public UserSettingsMenuHandler(@NotNull TowerDefencePlugin plugin) {
        this.plugin = plugin;
        this.userCache = plugin.getUserCache();
    }

    public static @NotNull ItemStack getMenuItem() {
        return SETTINGS_ITEM;
    }

    public void onGameStart() {
        this.plugin.getEventNode().addListener(PlayerUseItemEvent.class, event -> {
            if (this.plugin.getGameState() == GameState.IN_PROGRESS && event.getItemStack().getMaterial() == Material.COMMAND_BLOCK_MINECART) {
                this.createGui(event.getPlayer());
            }
        });
        this.startListener();
    }

    private void createGui(@NotNull Player player) {
        TDUser user = this.userCache.getUser(player.getUuid());

        Inventory inventory = new Inventory(InventoryType.CHEST_1_ROW, MENU_TITLE);
        inventory.setItemStack(1, this.createDamageIndicatorsItem(user));
        inventory.setItemStack(3, this.createHealthDisplayItem(user));
        inventory.setItemStack(5, this.createFlySpeedItem(user));
        inventory.setItemStack(7, this.createThinParticlesItem(user));

        player.openInventory(inventory);
    }

    private void startListener() {
        this.plugin.getEventNode().addListener(InventoryPreClickEvent.class, event -> {
            Inventory inventory = event.getInventory();
            if (inventory == null || inventory.getTitle() != MENU_TITLE)
                return;
            event.setCancelled(true);

            Player player = event.getPlayer();
            TDUser user = this.userCache.getUser(player.getUuid());
            switch (event.getSlot()) {
                case 1 -> {
                    user.setDamageIndicators(!user.isDamageIndicators());
                    inventory.setItemStack(1, this.createDamageIndicatorsItem(user));
                }
                case 3 -> {
                    user.setHealthMode(user.getHealthMode().next());
                    inventory.setItemStack(3, this.createHealthDisplayItem(user));
                }
                case 5 -> {
                    user.setFlySpeed(user.getFlySpeed().next());
                    player.setFlyingSpeed(user.getFlySpeed().getSpeed());
                    inventory.setItemStack(5, this.createFlySpeedItem(user));
                }
                case 7 -> {
                    user.setParticleThickness(user.getParticleThickness().next());
                    inventory.setItemStack(7, this.createThinParticlesItem(user));
                }
            }
        });
    }

    private @NotNull ItemStack createDamageIndicatorsItem(@NotNull TDUser user) {
        return ItemStack.builder(Material.OAK_SIGN)
                .displayName(Component.text("Show Damage Indicators", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
                .lore(
                        Component.text("> enabled", user.isDamageIndicators() ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false),
                        Component.text("> disabled", !user.isDamageIndicators() ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)
                )
                .build();
    }

    private @NotNull ItemStack createHealthDisplayItem(@NotNull TDUser user) {
        HealthDisplayMode healthMode = user.getHealthMode();
        List<Component> lore = new ArrayList<>();

        for (HealthDisplayMode loopHealthMode : HealthDisplayMode.values())
            lore.add(Component.text("> " + loopHealthMode.toString().toLowerCase().replace('_', ' '),
                    loopHealthMode == healthMode ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));

        return ItemStack.builder(Material.POTION)
                .meta(PotionMeta.class, builder -> builder.color(new Color(NamedTextColor.RED)))
                .displayName(Component.text("Health Display Mode", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
                .lore(lore)
                .build();
    }

    private @NotNull ItemStack createFlySpeedItem(@NotNull TDUser user) {
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

    private @NotNull ItemStack createThinParticlesItem(@NotNull TDUser user) {
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
