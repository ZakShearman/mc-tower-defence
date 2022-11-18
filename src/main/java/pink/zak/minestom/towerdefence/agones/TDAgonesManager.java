package pink.zak.minestom.towerdefence.agones;

import cc.towerdefence.api.agonessdk.IgnoredStreamObserver;
import cc.towerdefence.minestom.Environment;
import cc.towerdefence.minestom.module.kubernetes.KubernetesModule;
import dev.agones.sdk.AgonesSDKProto;
import dev.agones.sdk.SDKGrpc;

public class TDAgonesManager {
    private final SDKGrpc.SDKStub sdkStub;

    public TDAgonesManager(KubernetesModule kubernetesModule) {
        this.sdkStub = kubernetesModule.getSdk();
    }

    public void setBackfill(boolean backfill) {
        if (!Environment.isProduction()) return;

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
