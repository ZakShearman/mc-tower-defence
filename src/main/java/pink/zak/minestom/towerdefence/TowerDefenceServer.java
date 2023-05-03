package pink.zak.minestom.towerdefence;


import dev.emortal.minestom.core.MinestomServer;

public class TowerDefenceServer {

    public static void main(String[] args) {
        new MinestomServer.Builder()
                .commonModules()
                .module(TowerDefenceModule.class, TowerDefenceModule::new)
                .build();
    }
}
