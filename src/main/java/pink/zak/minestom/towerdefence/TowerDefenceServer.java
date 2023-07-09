package pink.zak.minestom.towerdefence;


import dev.emortal.minestom.core.MinestomServer;
import dev.emortal.minestom.core.module.monitoring.MonitoringModule;

public class TowerDefenceServer {

    public static void main(String[] args) {
        MinestomServer.builder()
                .commonModules()
                .module(MonitoringModule.class, MonitoringModule::new)
                .module(TowerDefenceModule.class, TowerDefenceModule::new)
                .buildAndStart();
    }
}
