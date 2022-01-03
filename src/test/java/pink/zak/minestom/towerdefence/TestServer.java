package pink.zak.minestom.towerdefence;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.optifine.OptifineSupport;

public class TestServer {
    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();
        OptifineSupport.enable();
        MojangAuth.init();

        MinecraftServer.getSchedulerManager().buildShutdownTask(TestServer::shutdown);
        System.out.println("Creating server with IP " + System.getenv("minestom.address") + ":" + System.getenv("minestom.port"));
        server.start(System.getenv("minestom.address"), Integer.parseInt(System.getenv("minestom.port")));
    }

    private static void shutdown() {
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.kick(Component.text("Server shutting down")));
    }
}
