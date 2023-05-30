package pink.zak.minestom.towerdefence;


import dev.emortal.minestom.core.MinestomServer;
import dev.emortal.minestom.core.module.monitoring.MonitoringModule;

public class TowerDefenceServer {

    public static void main(String[] args) {
        new MinestomServer.Builder()
                .commonModules()
                .module(MonitoringModule.class, env -> new MonitoringModule(env))
                .module(TowerDefenceModule.class, TowerDefenceModule::new)
                .build();
    }
}
