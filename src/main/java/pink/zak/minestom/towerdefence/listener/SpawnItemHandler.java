package pink.zak.minestom.towerdefence.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.api.event.player.PlayerTeamSwitchEvent;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.enums.Team;

import java.util.Set;

public class SpawnItemHandler {
    private final GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
    private final TowerDefencePlugin plugin;
    private final Set<Player> redPlayers;
    private final Set<Player> bluePlayers;

    private ItemStack blueItem;
    private ItemStack redItem;

    public SpawnItemHandler(TowerDefencePlugin plugin) {
        this.plugin = plugin;
        this.redPlayers = plugin.getRedPlayers();
        this.bluePlayers = plugin.getBluePlayers();

        this.handleTeamSwitchers();
    }

    private void handleTeamSwitchers() {
        this.updateTeamItem(Team.RED);
        this.updateTeamItem(Team.BLUE);

        this.plugin.getEventNode()
            .addListener(PlayerSpawnEvent.class, event -> {
                Player player = event.getPlayer();
                player.getInventory().setItemStack(0, this.redItem);
                player.getInventory().setItemStack(1, this.blueItem);
            })
            .addListener(PlayerDisconnectEvent.class, event -> {
                Player player = event.getPlayer();
                if (this.redPlayers.remove(player))
                    this.globalEventHandler.call(new PlayerTeamSwitchEvent(null, Team.RED, player));
                else if (this.bluePlayers.remove(player))
                    this.globalEventHandler.call(new PlayerTeamSwitchEvent(null, Team.BLUE, player));
            })
            .addListener(InventoryPreClickEvent.class, event -> {
                if (event.getClickType() == ClickType.START_DOUBLE_CLICK)
                    return;
                Material clickedMaterial = event.getClickedItem().getMaterial();

                Audiences.players().sendMessage(Component.text("Slot: " + event.getSlot() + " material " + clickedMaterial.name() + " click type " + event.getClickType()));
                if (this.plugin.getGameState() == GameState.LOBBY)
                    event.setCancelled(true);
            })
            .addListener(PlayerUseItemEvent.class, event -> this.handleTeamItemClick(event.getPlayer(), event.getItemStack()))
            .addListener(PlayerUseItemOnBlockEvent.class, event -> this.handleTeamItemClick(event.getPlayer(), event.getItemStack()));
    }

    private void handleTeamItemClick(Player player, ItemStack itemStack) {
        Material clickedMaterial = itemStack.getMaterial();
        if (clickedMaterial.equals(Material.RED_WOOL)) {
            if (this.redPlayers.contains(player)) {
                player.sendMessage(Component.text("You are already on the red team", NamedTextColor.RED));
            } else {
                player.sendMessage(Component.text("You are now on the red team", NamedTextColor.RED));
                boolean contained = this.bluePlayers.remove(player);
                this.redPlayers.add(player);

                this.updateTeamItem(Team.RED);
                if (contained)
                    this.updateTeamItem(Team.BLUE);

                this.globalEventHandler.call(new PlayerTeamSwitchEvent(Team.RED, contained ? Team.BLUE : null, player));
            }
        } else if (clickedMaterial.equals(Material.BLUE_WOOL)) {
            if (this.bluePlayers.contains(player)) {
                player.sendMessage(Component.text("You are already on the blue team", NamedTextColor.AQUA));
            } else {
                player.sendMessage(Component.text("You are now on the blue team", NamedTextColor.AQUA));
                boolean contained = this.redPlayers.remove(player);
                this.bluePlayers.add(player);

                this.updateTeamItem(Team.BLUE);
                if (contained)
                    this.updateTeamItem(Team.RED);

                this.globalEventHandler.call(new PlayerTeamSwitchEvent(Team.BLUE, contained ? Team.RED : null, player));
            }
        }
    }

    private Team getCurrentTeam(Player player) {
        if (this.redPlayers.contains(player))
            return Team.RED;
        else if (this.bluePlayers.contains(player))
            return Team.BLUE;
        return null;
    }

    private void updateTeamItem(Team team) {
        Material material;
        String name;
        if (team == Team.BLUE) {
            material = Material.BLUE_WOOL;
            name = "Blue Team (" + this.bluePlayers.size() + "/6)";
        } else {
            material = Material.RED_WOOL;
            name = "Red Team (" + this.redPlayers.size() + "/6)";
        }

        ItemStack teamItem = ItemStack.builder(material)
            .displayName(Component.text(name, team.getColor()).decoration(TextDecoration.ITALIC, false))
            .build();

        if (team == Team.BLUE)
            this.blueItem = teamItem;
        else
            this.redItem = teamItem;

        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            player.getInventory().setItemStack(team == Team.RED ? 0 : 1, teamItem);
        }
    }
}
