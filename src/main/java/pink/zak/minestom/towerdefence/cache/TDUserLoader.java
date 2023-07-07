package pink.zak.minestom.towerdefence.cache;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.network.PlayerProvider;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;

import java.util.UUID;

public class TDUserLoader implements PlayerProvider {
//    private final Repository<UUID, TDUser> userRepository; // todo load from gRPC API

    public TDUserLoader(TowerDefenceModule module) {
        module.getEventNode().addListener(PlayerDisconnectEvent.class, event -> this.save(event.getPlayer()));

        MinecraftServer.getSchedulerManager().buildShutdownTask(this::saveAll);
    }

    @Override
    public @NotNull Player createPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection connection) {
        return new TDPlayer(uuid, username, connection);
    }

    private void save(Player player) {
        // todo save to gRPC API
    }

    public void saveAll() {
        // todo save all to gRPC API
    }
}
