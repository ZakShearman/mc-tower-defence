package pink.zak.minestom.towerdefence.lobby;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import pink.zak.minestom.towerdefence.api.event.player.PlayerTeamSwitchEvent;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.user.LobbyPlayer;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;

import java.util.List;

public class SpawnItemHandler {
    private static final EventNode<Event> EVENT_NODE = EventNode.all("spawn-items");
    private static final GlobalEventHandler GLOBAL_EVENT_HANDLER = MinecraftServer.getGlobalEventHandler();

    static {
        LobbyManager.getEventNode().addChild(EVENT_NODE);
    }

    private final LobbyManager lobbyManager;

    private ItemStack blueItem;
    private ItemStack redItem;

    public SpawnItemHandler(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;

        // todo update item on join
        this.handleTeamSwitchers();
    }

    private void handleTeamSwitchers() {
        this.updateTeamItem(Team.RED);
        this.updateTeamItem(Team.BLUE);

        EVENT_NODE
                .addListener(PlayerSpawnEvent.class, event -> {
                    // this is called after the new player is assigned a team.
                    // call updateTeamItems before adding ItemStacks to inv to save packets :)
                    this.updateTeamItems();

                    Player player = event.getPlayer();
                    player.getInventory().setItemStack(0, this.redItem);
                    player.getInventory().setItemStack(1, this.blueItem);

                    // todo update timer
//                    if (this.plugin.getGameState() != GameState.IN_PROGRESS) {
//                        MinecraftServer.getSchedulerManager().buildTask(() -> {
//                            this.plugin.getGameHandler().start(player.getInstance());
//                        }).delay(1, TimeUnit.SECOND).schedule();
//                    }
                })
                .addListener(InventoryPreClickEvent.class, event -> {
                    if (event.getClickType() == ClickType.START_DOUBLE_CLICK) return;
                    Material clickedMaterial = event.getClickedItem().material();

                    Audiences.players().sendMessage(Component.text("Slot: " + event.getSlot() + " material " + clickedMaterial.name() + " click type " + event.getClickType()));
                    event.setCancelled(true);
                })
                .addListener(PlayerUseItemEvent.class, event -> this.handleTeamItemClick((TDPlayer) event.getPlayer(), event.getItemStack()))
                .addListener(PlayerUseItemOnBlockEvent.class, event -> this.handleTeamItemClick((TDPlayer) event.getPlayer(), event.getItemStack()));
    }

    private void handleTeamItemClick(TDPlayer player, ItemStack itemStack) {
        LobbyPlayer lobbyPlayer = this.lobbyManager.getLobbyPlayer(player);
        Material clickedMaterial = itemStack.material();
        if (clickedMaterial.equals(Material.RED_WOOL)) {
            if (lobbyPlayer.getTeam() == Team.RED) {
                player.sendMessage(Component.text("You are already on the red team", NamedTextColor.RED));
            } else {
                player.sendMessage(Component.text("You are now on the red team", NamedTextColor.RED));
                this.lobbyManager.setPlayerTeam(lobbyPlayer, Team.RED);

                this.updateTeamItems();

                GLOBAL_EVENT_HANDLER.call(new PlayerTeamSwitchEvent(Team.RED, Team.BLUE, player));
            }
        } else if (clickedMaterial.equals(Material.BLUE_WOOL)) {
            if (lobbyPlayer.getTeam() == Team.BLUE) {
                player.sendMessage(Component.text("You are already on the blue team", NamedTextColor.AQUA));
            } else {
                player.sendMessage(Component.text("You are now on the blue team", NamedTextColor.AQUA));
                this.lobbyManager.setPlayerTeam(lobbyPlayer, Team.BLUE);

                this.updateTeamItems();

                GLOBAL_EVENT_HANDLER.call(new PlayerTeamSwitchEvent(Team.BLUE, Team.RED, player));
            }
        }
    }

    private void updateTeamItems() {
        this.updateTeamItem(Team.RED);
        this.updateTeamItem(Team.BLUE);
    }

    private synchronized void updateTeamItem(Team team) {
        Material material;
        String name;
        List<? extends Component> lore;
        if (team == Team.BLUE) {
            material = Material.BLUE_WOOL;
            name = "Blue Team (" + this.lobbyManager.getTeamSize(Team.BLUE).get() + "/6)";
            lore = this.lobbyManager.getLobbyPlayers().stream()
                    .filter(lobbyPlayer -> lobbyPlayer.getTeam() == Team.BLUE)
                    .map(lobbyPlayer -> Component.text("  - " + lobbyPlayer.getPlayer().getUsername(), Style.style(NamedTextColor.AQUA, TextDecoration.ITALIC.withState(TextDecoration.State.FALSE))))
                    .toList();
        } else {
            material = Material.RED_WOOL;
            name = "Red Team (" + this.lobbyManager.getTeamSize(Team.RED).get() + "/6)";
            lore = this.lobbyManager.getLobbyPlayers().stream()
                    .filter(lobbyPlayer -> lobbyPlayer.getTeam() == Team.RED)
                    .map(lobbyPlayer -> Component.text("  - " + lobbyPlayer.getPlayer().getUsername(), Style.style(NamedTextColor.RED, TextDecoration.ITALIC.withState(TextDecoration.State.FALSE))))
                    .toList();
        }

        ItemStack teamItem = ItemStack.builder(material)
                .displayName(Component.text(name, team.getColor()).decoration(TextDecoration.ITALIC, false))
                .lore(lore)
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
