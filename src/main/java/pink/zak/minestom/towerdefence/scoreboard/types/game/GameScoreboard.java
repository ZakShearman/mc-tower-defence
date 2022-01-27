package pink.zak.minestom.towerdefence.scoreboard.types.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.scoreboard.Sidebar;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.api.event.game.TowerDamageEvent;
import pink.zak.minestom.towerdefence.api.event.player.PlayerCoinChangeEvent;
import pink.zak.minestom.towerdefence.api.event.player.PlayerManaChangeEvent;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.model.GameUser;
import pink.zak.minestom.towerdefence.scoreboard.TowerScoreboard;

public class GameScoreboard {
    private final Sidebar sidebar = new Sidebar(TowerScoreboard.TITLE);
    private final GameUser gameUser;
    private final double maxHealth = 1000;

    public GameScoreboard(TowerDefencePlugin plugin, GameUser gameUser) {
        this.gameUser = gameUser;

        this.sidebar.createLine(new Sidebar.ScoreboardLine("empty-6", Component.empty(), 7));
        this.sidebar.createLine(new Sidebar.ScoreboardLine("coins", this.createCoins(), 6));
        this.sidebar.createLine(new Sidebar.ScoreboardLine("mana", this.createMana(), 5));
        this.sidebar.createLine(new Sidebar.ScoreboardLine("empty-4", Component.empty(), 4));
        this.sidebar.createLine(new Sidebar.ScoreboardLine("red-health", this.createRedHealth(new TowerDamageEvent(Team.RED, 0, (int) this.maxHealth)), 3));
        this.sidebar.createLine(new Sidebar.ScoreboardLine("blue-health", this.createBlueHealth(new TowerDamageEvent(Team.BLUE, 0, (int) this.maxHealth)), 2));
        this.sidebar.createLine(new Sidebar.ScoreboardLine("empty-1", Component.empty(), 1));
        this.sidebar.createLine(new Sidebar.ScoreboardLine("website", TowerScoreboard.DOMAIN, 0));

        this.sidebar.addViewer(this.gameUser.getPlayer());

        plugin.eventNode()
            .addListener(TowerDamageEvent.class, event -> {
                if (event.team() == Team.RED)
                    this.sidebar.updateLineContent("red-health", this.createRedHealth(event));
                else
                    this.sidebar.updateLineContent("blue-health", this.createBlueHealth(event));
            })
            .addListener(PlayerCoinChangeEvent.class, event -> this.sidebar.updateLineContent("coins", this.createCoins()))
            .addListener(PlayerManaChangeEvent.class, event -> this.sidebar.updateLineContent("mana", this.createMana()));
    }

    public void destroy() {
        this.sidebar.removeViewer(this.gameUser.getPlayer());
        // todo cleanup events
    }

    private Component createCoins() {
        return Component.text("Coins: ", NamedTextColor.WHITE)
            .append(Component.text(this.gameUser.getCoins(), NamedTextColor.YELLOW));
    }

    private Component createMana() {
        return Component.text("Mana: ", NamedTextColor.WHITE)
            .append(Component.text(this.gameUser.getMana(), NamedTextColor.AQUA));
    }

    private Component createRedHealth(TowerDamageEvent event) {
        return Component.text("Health: " + (event.health() / this.maxHealth) * 100 + "%", NamedTextColor.RED);
    }

    private Component createBlueHealth(TowerDamageEvent event) {
        return Component.text("Health: " + (event.health() / this.maxHealth) * 100 + "%", NamedTextColor.AQUA);
    }
}
