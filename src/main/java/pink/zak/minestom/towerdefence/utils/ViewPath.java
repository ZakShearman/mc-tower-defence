package pink.zak.minestom.towerdefence.utils;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.block.BlockIterator;

public class ViewPath {

    public static boolean isClear(Player player, int distance) {
        Instance instance = player.getInstance();
        BlockIterator blockIterator = new BlockIterator(player, distance);

        while (blockIterator.hasNext())
            if (!instance.getBlock(blockIterator.next()).isAir())
                return false;
        return true;
    }

    public static boolean isClear(Player player, GameMode gameMode) {
        if (gameMode == GameMode.SPECTATOR)
            throw new IllegalArgumentException("gameMode cannot be SPECTATOR");
        return isClear(player, gameMode == GameMode.CREATIVE ? 6 : 5);
    }

    public static boolean isClear(Player player) {
        return isClear(player, player.getGameMode());
    }
}
