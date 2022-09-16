package pink.zak.minestom.towerdefence;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.MojangAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;

public class TestServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestServer.class);

    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();
        MojangAuth.init();

        MinecraftServer.getSchedulerManager().buildShutdownTask(TestServer::shutdown);

        MinecraftServer.getGlobalEventHandler().addListener(ServerListPingEvent.class, TestServer::handleServerPing);

        String ip = System.getenv("minestom.address");
        int port = Integer.parseInt(System.getenv("minestom.port"));
        LOGGER.info("Creating server with IP {}:{}", ip, port);
        server.start(ip, port);
    }

    private static void shutdown() {
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.kick(Component.text("Server shutting down")));
    }

    private static void handleServerPing(ServerListPingEvent event) {
        try {
            String base64 = Base64.getEncoder().encodeToString(TestServer.class.getResourceAsStream("/favicon.png").readAllBytes());
            event.getResponseData().setFavicon("data:image/png;base64," + base64);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
