package pink.zak.minestom.towerdefence.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;

public interface TowerScoreboard {

    Component TITLE = Component.text("TowerDefence", NamedTextColor.YELLOW);
    Component DOMAIN = Component.text("mc.emortal.dev", NamedTextColor.YELLOW);

    boolean removeViewer(Player player);

    enum Type {
        LOBBY,
        IN_GAME,
        SPECTATOR
    }
}
