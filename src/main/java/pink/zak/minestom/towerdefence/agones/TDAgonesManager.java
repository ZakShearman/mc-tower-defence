package pink.zak.minestom.towerdefence.agones;

import cc.towerdefence.api.agonessdk.IgnoredStreamObserver;
import cc.towerdefence.minestom.Environment;
import cc.towerdefence.minestom.module.kubernetes.KubernetesModule;
import dev.agones.sdk.AgonesSDKProto;
import dev.agones.sdk.SDKGrpc;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import pink.zak.minestom.towerdefence.TowerDefenceModule;

import java.util.concurrent.atomic.AtomicBoolean;

public class TDAgonesManager {
    private final SDKGrpc.SDKStub sdkStub;
    private final AtomicBoolean backfill = new AtomicBoolean(false);
    private final int maxPlayers = 12; // todo get from env variable? or we determine ourselves based on map?

    public TDAgonesManager(TowerDefenceModule module, KubernetesModule kubernetesModule) {
        this.sdkStub = kubernetesModule.getSdk();

        module.getEventNode().addListener(PlayerLoginEvent.class, event -> {
                    int onlinePlayers = MinecraftServer.getConnectionManager().getOnlinePlayers().size();
                    if (onlinePlayers >= this.maxPlayers) {
                        this.setBackfill(false);
                    } else {
                        this.setBackfill(true);
                    }
                })
                .addListener(PlayerDisconnectEvent.class, event -> this.setBackfill(true));
    }

    public void setBackfill(boolean backfill) {
        if (!Environment.isProduction()) return;
        boolean original = this.backfill.getAndSet(backfill);
        if (original == backfill) return;

        this.sdkStub.setLabel(AgonesSDKProto.KeyValue.newBuilder()
                .setKey("backfill")
                .setValue(backfill ? "true" : "false").build(), new IgnoredStreamObserver<>());
    }


    /**
     * @param phase either lobby or game
     */
    public void setPhase(String phase) {
        if (!Environment.isProduction()) return;

        this.sdkStub.setLabel(AgonesSDKProto.KeyValue.newBuilder()
                .setKey("phase")
                .setValue(phase).build(), new IgnoredStreamObserver<>());
    }
}
