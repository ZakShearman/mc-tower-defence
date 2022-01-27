package pink.zak.minestom.towerdefence.cache;

import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;
import pink.zak.minestom.towerdefence.model.TDUser;
import pink.zak.minestom.towerdefence.storage.dynamic.repository.JsonUserRepository;
import pink.zak.minestom.towerdefence.utils.storage.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TDUserCache {
    private final Map<UUID, TDUser> tdUsers = new ConcurrentHashMap<>();
    private final Repository<UUID, TDUser> userRepository;

    public TDUserCache(TowerDefencePlugin plugin) {
        this.userRepository = plugin.getUserRepository();

        plugin.eventNode().addListener(PlayerLoginEvent.class, event -> this.load(event.getPlayer().getUuid()));
        plugin.eventNode().addListener(PlayerDisconnectEvent.class, event -> this.invalidate(event.getPlayer().getUuid()));
    }

    public Collection<TDUser> getAllUsers() {
        return this.tdUsers.values();
    }

    public @NotNull TDUser getUser(UUID uuid) {
        TDUser user = this.tdUsers.get(uuid);
        if (user == null) {
            Optional<TDUser> optionalUser = this.load(uuid);
            if (optionalUser.isEmpty())
                user = this.create(uuid);
            else
                user = optionalUser.get();
            this.tdUsers.put(uuid, user);
        }
        return user;
    }

    private TDUser create(UUID uuid) {
        TDUser user = new TDUser(uuid);
        this.userRepository.save(uuid, user);
        return user;
    }

    private Optional<TDUser> load(UUID uuid) {
        return this.userRepository.findById(uuid);
    }

    private void invalidate(UUID uuid) {
        TDUser user = this.tdUsers.remove(uuid);
        if (user != null)
            this.userRepository.save(uuid, user);
    }

    public void invalidateAll() {
        for (Map.Entry<UUID, TDUser> entry : this.tdUsers.entrySet()) {
            this.userRepository.save(entry.getKey(), entry.getValue());
        }
        this.tdUsers.clear();
    }
}
