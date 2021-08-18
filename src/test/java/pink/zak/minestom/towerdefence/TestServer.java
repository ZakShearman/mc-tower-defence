package pink.zak.minestom.towerdefence;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.timer.SchedulerManager;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.time.TimeUnit;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

public class TestServer {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        MinecraftServer server = MinecraftServer.init();
        OptifineSupport.enable();
        MojangAuth.init();

        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
        SchedulerManager schedulerManager = MinecraftServer.getSchedulerManager();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        InstanceContainer instance = instanceManager.createInstanceContainer();

        schedulerManager.buildShutdownTask(TestServer::shutdown);

        startBenchmark();

        eventHandler.addListener(PlayerLoginEvent.class, event -> {
            event.getPlayer().setRespawnPoint(new Pos(0, 100, 0));
            event.setSpawningInstance(instance);

            Audiences.all().sendMessage(Component.text(event.getPlayer().getUsername() + " logged in"));
        });

        server.start("10.0.0.7", 25565);

        System.out.println("Did server stuff in " + (System.currentTimeMillis() - startTime) + "ms");
    }

    private static void startBenchmark() {
        BenchmarkManager benchmarkManager = MinecraftServer.getBenchmarkManager();
        benchmarkManager.enable(Duration.ofMillis(Long.MAX_VALUE));

        AtomicReference<TickMonitor> lastTick = new AtomicReference<>();
        MinecraftServer.getUpdateManager().addTickMonitor(lastTick::set);

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
            if (players.isEmpty())
                return;

            long ramUsage = benchmarkManager.getUsedMemory();
            ramUsage /= 1e6; // bytes to MB

            TickMonitor tickMonitor = lastTick.get();
            final Component header = Component.text("RAM USAGE: " + ramUsage + " MB")
                .append(Component.newline())
                .append(Component.text("TICK TIME: " + MathUtils.round(tickMonitor.getTickTime(), 2) + "ms"))
                .append(Component.newline())
                .append(Component.text("ACQ TIME: " + MathUtils.round(tickMonitor.getAcquisitionTime(), 2) + "ms"));
            final Component footer = benchmarkManager.getCpuMonitoringMessage();
            Audiences.players().sendPlayerListHeaderAndFooter(header, footer);
        }).repeat(10, TimeUnit.SERVER_TICK).schedule();
    }

    private static void shutdown() {
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.kick(Component.text("Server shutting down")));
    }
}
