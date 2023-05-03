package pink.zak.minestom.towerdefence.lobby.starting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.builder.Command;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.lobby.LobbyManager;

public class DevLobbyStartingManager {
    private final LobbyManager lobbyManager;
    private final TowerDefenceModule module;

    private final Command forceStartCommand;

    public DevLobbyStartingManager(@NotNull LobbyManager lobbyManager, @NotNull TowerDefenceModule module) {
        this.lobbyManager = lobbyManager;
        this.module = module;

        EventNode<Event> eventNode = EventNode.all("lobby-starter");
        lobbyManager.getEventNode().addChild(eventNode);

        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            event.getPlayer().sendMessage(Component.text("This game is running in DEVELOPMENT mode. Use /forcestart to start the game."));
        });

        this.forceStartCommand = new Command("forcestart");
        this.forceStartCommand.setDefaultExecutor((sender, context) -> this.startGameIfPossible());

        MinecraftServer.getCommandManager().register(this.forceStartCommand);
    }

    private void startGameIfPossible() {
        if (!this.areTeamsBalanced()) {
            Audiences.all().sendMessage(Component.text("The teams are not balanced so the game will not start.", NamedTextColor.RED)
                    .append(Component.newline())
                    .append(Component.text("Please change your team to ensure teams are balanced.", NamedTextColor.RED)));
            return;
        }

        this.startGame();
    }

    private boolean areTeamsBalanced() {
        int blueSize = this.lobbyManager.getTeamSize(Team.BLUE).get();
        int redSize = this.lobbyManager.getTeamSize(Team.RED).get();
        int differential = Math.abs(blueSize - redSize);

        if (blueSize + redSize <= 6) return differential <= 1; // Max differential of 1 for 6 or less players
        return differential <= 2; // Max differential of 2 for 7 or more players
    }

    private void startGame() {
        this.module.getGameHandler().start();
        this.destroy();

        Audiences.all().sendMessage(Component.text("Game starting!", NamedTextColor.GREEN));
    }

    private void destroy() {
        this.lobbyManager.destroy();
        MinecraftServer.getCommandManager().unregister(this.forceStartCommand);
    }
}
