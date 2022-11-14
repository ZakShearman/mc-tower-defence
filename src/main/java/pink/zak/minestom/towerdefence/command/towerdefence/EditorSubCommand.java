package pink.zak.minestom.towerdefence.command.towerdefence;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.map.Area;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.utils.ViewPath;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EditorSubCommand implements CommandExecutor {
    private final TowerDefenceModule module;
    private final TowerDefenceInstance instance;
    private final TowerMap map;

    private final Map<Player, EditorInfo> editors = new ConcurrentHashMap<>();

    private final ItemStack redTeamWandItem;
    private final ItemStack blueTeamWandItem;

    public EditorSubCommand(TowerDefenceModule module) {
        this.module = module;
        this.instance = module.getInstance();
        this.map = this.instance.getTowerMap();

        ItemStack.Builder wandBuilder = ItemStack.builder(Material.DIAMOND_HOE)
                .lore(
                        Component.text("Left-click for pos1", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false),
                        Component.text("Right-click for pos2", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text("Right-click the air to change team", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)
                );

        this.redTeamWandItem = wandBuilder
                .displayName(Component.text("Team area wand item", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false)).build();

        this.blueTeamWandItem = wandBuilder
                .displayName(Component.text("Team area wand item", NamedTextColor.BLUE)
                        .decoration(TextDecoration.ITALIC, false)).build();

        module.getEventNode()
                .addListener(PlayerStartDiggingEvent.class, event -> { // left click block
                    Player player = event.getPlayer();
                    EditorInfo editorInfo = this.editors.get(player);
                    if (editorInfo == null || !player.getItemInMainHand().material().equals(Material.DIAMOND_HOE))
                        return;
                    Point clickPoint = event.getBlockPosition();

                    editorInfo.setPos1(clickPoint);
                    player.sendMessage(Component.text("Set pos1", NamedTextColor.GREEN));
                    this.tryCreateArea(player, editorInfo);
                })
                .addListener(PlayerBlockInteractEvent.class, event -> { // right click block
                    Player player = event.getPlayer();
                    EditorInfo editorInfo = this.editors.get(player);
                    if (event.getHand() != Player.Hand.MAIN || editorInfo == null || !player.getItemInMainHand().material().equals(Material.DIAMOND_HOE))
                        return;
                    Point clickPoint = event.getBlockPosition();

                    editorInfo.setPos2(clickPoint);
                    player.sendMessage(Component.text("Set pos2", NamedTextColor.GREEN));
                    this.tryCreateArea(player, editorInfo);
                })
                .addListener(PlayerUseItemEvent.class, event -> { // right click air
                    Player player = event.getPlayer();
                    if (event.getHand() != Player.Hand.MAIN || !ViewPath.isClear(event.getPlayer()) || !player.getItemInMainHand().material().equals(Material.DIAMOND_HOE)) {
                        return;
                    }
                    EditorInfo editorInfo = this.editors.get(player);
                    if (editorInfo != null) {
                        Team newTeam = editorInfo.getTeam() == Team.RED ? Team.BLUE : Team.RED;
                        editorInfo.setTeam(newTeam);
                        player.getInventory().setItemStack(8, newTeam == Team.RED ? this.redTeamWandItem : this.blueTeamWandItem);
                        player.sendMessage(Component.text("Changed team to " + newTeam.name().toLowerCase(Locale.ROOT), newTeam.getColor()));
                    }
                });
    }

    @Override
    public void apply(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = (Player) sender;
        if (this.editors.remove(player) != null) {
            player.sendMessage(Component.text("You are no longer in editor mode", NamedTextColor.GREEN));
            this.removePlayerChanges(player);
            this.removeEditorItems(player);
            return;
        }
        this.editors.put(player, new EditorInfo());
        this.giveEditorItems(player);
        this.applyPlayerChanges(player);
        player.sendMessage(Component.text("You are now in editor mode", NamedTextColor.GREEN));
    }

    private void tryCreateArea(Player player, EditorInfo editorInfo) {
        if (editorInfo.getPos1() != null && editorInfo.getPos2() != null) {
            Team team = editorInfo.getTeam();
            boolean update;
            if (team == Team.RED) {
                update = this.map.getRedArea() != null;
                this.map.setRedArea(new Area(editorInfo.getPos1(), editorInfo.getPos2()));
            } else {
                update = this.map.getBlueArea() != null;
                this.map.setBlueArea(new Area(editorInfo.getPos1(), editorInfo.getPos2()));
            }

            if (update)
                player.sendMessage(Component.text("Updated " + team.name().toLowerCase() + "'s tower area", NamedTextColor.GREEN));
            else
                player.sendMessage(Component.text("Set " + team.name().toLowerCase() + "'s tower area", NamedTextColor.GREEN));

            this.instance.saveTowerMapData();
        }
    }

    private void applyPlayerChanges(Player player) {
        player.setAllowFlying(true);
        player.setFlying(true);
    }

    private void removePlayerChanges(Player player) {
        player.setAllowFlying(false);
        player.setFlying(false);
    }

    private void giveEditorItems(Player player) {
        if (this.module.getGameState() == GameState.LOBBY) {
            PlayerInventory inventory = player.getInventory();
            inventory.setItemStack(8, this.redTeamWandItem);
        }
    }

    private void removeEditorItems(Player player) {
        PlayerInventory inventory = player.getInventory();
        if (inventory.getItemStack(8).material().equals(Material.DIAMOND_HOE)) { // check they still have the item
            inventory.setItemStack(8, ItemStack.AIR);
        }
    }

    private static class EditorInfo {
        private Point pos1;
        private Point pos2;
        private Team team = Team.RED;

        public Point getPos1() {
            return this.pos1;
        }

        public void setPos1(Point pos1) {
            this.pos1 = pos1;
        }

        public Point getPos2() {
            return this.pos2;
        }

        public void setPos2(Point pos2) {
            this.pos2 = pos2;
        }

        public Team getTeam() {
            return this.team;
        }

        public void setTeam(Team team) {
            this.team = team;
        }
    }
}
