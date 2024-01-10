package pink.zak.minestom.towerdefence.ui.tower;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.model.tower.TowerManager;
import pink.zak.minestom.towerdefence.model.tower.config.Tower;
import pink.zak.minestom.towerdefence.model.tower.config.TowerLevel;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;

public final class TowerManagementUI extends Inventory {

    private static final @NotNull MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final @NotNull ItemStack BASE_PREVIEW_TOWER_RADIUS_ITEM = ItemStack.builder(Material.REDSTONE)
            .displayName(Component.text("Preview Tower Radius", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
            .build();
    private static final @NotNull ItemStack REMOVE_TOWER_ITEM = ItemStack.builder(Material.BARRIER)
            .displayName(Component.text("Remove Tower", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
            .lore(List.of(
                    Component.empty(),
                    Component.text("Removes the tower", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)
            )).build();

    private final @NotNull PlacedTower<?> tower;
    private final @NotNull GameUser user;
    private final @NotNull TowerManager towerManager;

    /**
     * Constructs a new {@link TowerManagementUI}. This method is not to be used outside {@link PlacedTower}.
     *
     * @param tower the tower to create the UI for
     */
    @ApiStatus.Internal
    public TowerManagementUI(@NotNull PlacedTower<?> tower, @NotNull GameUser user, @NotNull TowerManager towerManager) {
        super(InventoryType.CHEST_3_ROW, Component.text(tower.getTower().getName()));
        this.tower = tower;
        this.user = user;
        this.towerManager = towerManager;

        Tower type = tower.getTower();
        this.setItemStack(0, type.getBaseItem());
        this.setItemStack(11, type.getLevel(1).getOwnedUpgradeItem());
        this.setItemStack(17, REMOVE_TOWER_ITEM);
        this.setItemStack(26, this.createPreviewTowerRadiusItem());

        this.refresh();

        this.addInventoryCondition((p, slot, clickType, result) -> {
            // always cancel the event
            result.setCancel(true);

            // this is not the player who opened the inventory, fast exit
            if (!p.equals(this.user.getPlayer())) return;

            // run the click handler
            this.onClick(slot, clickType);
        });
    }

    public void refresh() {
        for (TowerLevel level : this.tower.getTower().getLevels()) {
            TowerLevel currentLevel = this.tower.getLevel();
            boolean purchased = currentLevel.getLevel() >= level.getLevel(); // todo: should probably make a way to compare levels better

            ItemStack item;
            if (!purchased) {
                int cost = this.tower.getCost(level);
                boolean canAfford = this.user.getCoins() >= cost;
                item = level.createBuyUpgradeItem(canAfford, cost, currentLevel);
            } else item = level.getOwnedUpgradeItem();

            this.setItemStack(level.getLevel() + 10, item);
        }
    }

    private void onClick(int slot, @NotNull ClickType clickType) {
        if (slot == 17) {
            this.towerManager.removeTower(this.tower);
            TDPlayer player = this.user.getPlayer();
            player.closeInventory();
            player.sendMessage(Component.text("Tower removed!", NamedTextColor.RED));
            return;
        }

        if (slot == 26) {
            this.showTowerRadius(clickType);
            return;
        }

        int clicked = slot - 10;
        TowerLevel currentLevel = this.tower.getLevel();

        if (clicked < 0) return;
        if (clicked > this.tower.getTower().getMaxLevel()) return;
        if (clicked <= currentLevel.getLevel()) return;

        this.tower.upgrade(clicked, this.user);
    }

    private @NotNull ItemStack createPreviewTowerRadiusItem() {
        return BASE_PREVIEW_TOWER_RADIUS_ITEM.withLore(Stream.of(
                "",
                "<i:false><dark_red>Left-click</dark_red><red> shows the tower's range",
                "<i:false><dark_red>Right-click</dark_red><red> shows all %s tower ranges".formatted(tower.getTower().getName())
        ).map(MINI_MESSAGE::deserialize).toList());
    }

    private void showTowerRadius(@NotNull ClickType clickType) {
        Set<SendablePacket> packets = clickType == ClickType.LEFT_CLICK ? createRadiusPackets(this.tower, Collections.emptySet()) : new HashSet<>();

        if (clickType == ClickType.RIGHT_CLICK) {
            Set<PlacedTower<?>> towers = this.towerManager.getTowers(this.user.getTeam()).stream()
                    .filter(tower -> tower.getTower().getType().equals(this.tower.getTower().getType()))
                    .collect(Collectors.toUnmodifiableSet());

            for (PlacedTower<?> tower : towers) {
                Set<PlacedTower<?>> otherTowers = new HashSet<>(towers);
                otherTowers.remove(tower);
                packets.addAll(createRadiusPackets(tower, otherTowers));
            }
        }

        Task task = MinecraftServer.getSchedulerManager()
                .buildTask(() -> this.user.getPlayer().sendPackets(packets))
                .repeat(750, TimeUnit.MILLISECOND)
                .schedule();

        MinecraftServer.getSchedulerManager()
                .buildTask(task::cancel)
                .delay(5, TimeUnit.SECOND)
                .schedule();
    }

    private static @NotNull Set<SendablePacket> createRadiusPackets(@NotNull PlacedTower<?> tower, @NotNull Set<PlacedTower<?>> otherTowers) {
        Point centre = tower.getBasePoint();
        double radius = tower.getLevel().getRange();

        Set<SendablePacket> packets = new HashSet<>();
        circumference: for (int i = 0; i <= 360; i++) {
            double c1 = radius * Math.cos(i);
            double c2 = radius * Math.sin(i);

            Point circumference = centre.add(c1, 1.5, c2);

            for (PlacedTower<?> otherTower : otherTowers) {
                if (circumference.distance(otherTower.getBasePoint()) < otherTower.getLevel().getRange()) {
                    continue circumference;
                }
            }

            packets.add(ParticleCreator.createParticlePacket(
                    Particle.DUST, true,
                    circumference.x(), circumference.y(), circumference.z(),
                    0, 0, 0,
                    0f, 1,
                    writer -> {
                        writer.writeFloat(1);
                        writer.writeFloat(0);
                        writer.writeFloat(0);
                        writer.writeFloat(1.5f);
                    }
            ));
        }

        return packets;
    }

}
