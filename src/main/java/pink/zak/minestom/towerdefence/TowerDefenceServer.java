package pink.zak.minestom.towerdefence;

import cc.towerdefence.minestom.MinestomServer;
import cc.towerdefence.minestom.module.chat.ChatModule;
import cc.towerdefence.minestom.module.core.CoreModule;
import cc.towerdefence.minestom.module.kubernetes.KubernetesModule;
import cc.towerdefence.minestom.module.permissions.PermissionModule;

public class TowerDefenceServer {

    public static void main(String[] args) {
        new MinestomServer.Builder()
                .module(KubernetesModule.class, KubernetesModule::new)
                .module(CoreModule.class, CoreModule::new)
                .module(PermissionModule.class, PermissionModule::new)
                .module(ChatModule.class, ChatModule::new)

                .module(TowerDefenceModule.class, TowerDefenceModule::new)
                .build();
    }
}
