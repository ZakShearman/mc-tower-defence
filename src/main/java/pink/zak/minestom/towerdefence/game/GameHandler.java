package pink.zak.minestom.towerdefence.game;


import dev.agones.sdk.AgonesSDKProto;
import dev.emortal.api.agonessdk.IgnoredStreamObserver;
import dev.emortal.api.kurushimi.KurushimiMinestomUtils;
import dev.emortal.minestom.core.Environment;
import dev.emortal.minestom.core.module.kubernetes.KubernetesModule;
import dev.emortal.minestom.core.utils.ProgressBar;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.hologram.Hologram;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.actionbar.ActionBarHandler;
import pink.zak.minestom.towerdefence.api.event.game.CastleDamageEvent;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.game.listeners.MobMenuHandler;
import pink.zak.minestom.towerdefence.game.listeners.TowerPlaceHandler;
import pink.zak.minestom.towerdefence.game.listeners.TowerUpgradeHandler;
import pink.zak.minestom.towerdefence.game.listeners.UserSettingsMenuHandler;
import pink.zak.minestom.towerdefence.lobby.LobbyManager;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.user.LobbyPlayer;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class GameHandler {
    private final @NotNull TowerDefenceModule module;
    private final @NotNull TowerDefenceInstance instance;
    private final @NotNull TowerMap map;
    private final @NotNull LobbyManager lobbyManager;
    private final KubernetesModule kubernetesModule;

    private final @NotNull MobHandler mobHandler;
    private final @NotNull TowerHandler towerHandler;
    private final @NotNull MobMenuHandler mobMenuHandler;
    private final @NotNull UserSettingsMenuHandler userSettingsMenuHandler;

    private final Set<EnemyMob> defaultEnemyMobs;
    private final @NotNull AtomicInteger redTowerHealth = new AtomicInteger(10_000);
    private final @NotNull AtomicInteger blueTowerHealth = new AtomicInteger(10_000);
    private final @NotNull Map<Player, GameUser> users = new HashMap<>();
    private final AtomicBoolean ended = new AtomicBoolean(false);
    private Hologram redTowerHologram;
    private Hologram blueTowerHologram;

    public GameHandler(@NotNull TowerDefenceModule module, @NotNull LobbyManager lobbyManager) {
        this.module = module;
        this.instance = module.getInstance();
        this.map = this.instance.getTowerMap();
        this.lobbyManager = lobbyManager;
        this.kubernetesModule = module.getKubernetesModule();

        this.towerHandler = new TowerHandler(module, this);
        this.mobHandler = new MobHandler(module, this);
        this.mobMenuHandler = new MobMenuHandler(module, this);
        this.userSettingsMenuHandler = new UserSettingsMenuHandler(module);

        this.defaultEnemyMobs = module.getMobStorage().getEnemyMobs()
                .stream()
                .filter(mob -> mob.getLevel(1).getIncomeCost() <= 0)
                .collect(Collectors.toUnmodifiableSet());

        new TowerPlaceHandler(module, this);
        new TowerUpgradeHandler(module, this);

        module.getEventNode().addListener(PlayerDisconnectEvent.class, event -> this.users.remove(event.getPlayer()));
    }

    public void start() {
        this.module.setGameState(GameState.GAME);

        Set<LobbyPlayer> lobbyPlayers = this.lobbyManager.getLobbyPlayers();
        Set<LobbyPlayer> redPlayers = lobbyPlayers.stream().filter(lobbyPlayer -> lobbyPlayer.getTeam() == Team.RED).collect(Collectors.toSet());
        Set<LobbyPlayer> bluePlayers = lobbyPlayers.stream().filter(lobbyPlayer -> lobbyPlayer.getTeam() == Team.BLUE).collect(Collectors.toSet());

        this.configureTeam(Team.RED, redPlayers);
        this.configureTeam(Team.BLUE, bluePlayers);
        this.module.getScoreboardManager().startGame();

        for (Player player : this.users.keySet()) {
            player.getInventory().clear();
            player.getInventory().setItemStack(4, this.mobMenuHandler.getChestItem());
            player.getInventory().setItemStack(8, UserSettingsMenuHandler.getMenuItem()); // todo standardise static/non-static usage
            this.mobMenuHandler.onGameStart();
            this.userSettingsMenuHandler.onGameStart();
        }

        new IncomeHandler(this);
        new ActionBarHandler(this, MinecraftServer.getGlobalEventHandler());

        /*EnemyMob enemyMob = this.plugin.getMobStorage().getEnemyMob(EntityType.LLAMA);
        EnemyMobLevel enemyMobLevel = enemyMob.level(1);
        QueuedEnemyMob queuedEnemyMob = new QueuedEnemyMob(enemyMob,enemyMobLevel);
        for (int i = 0; i<100; i++) { // desired game count
            for (int z = 0; z<5000; z++) {// estimated max mobs in a game
                this.mobHandler.spawnMob(queuedEnemyMob, new GameUser(null, Team.RED));
            }
        }*/
    }

    private void configureTeam(@NotNull Team team, @NotNull Set<LobbyPlayer> players) {
        Pos spawnPoint = this.map.getSpawn(team);

        if (team == Team.RED) {
            this.redTowerHologram = new Hologram(this.instance, this.map.getRedTowerHologram(), this.createTowerHologram(Team.RED));
        } else {
            this.blueTowerHologram = new Hologram(this.instance, this.map.getBlueTowerHologram(), this.createTowerHologram(Team.BLUE));
        }

        for (LobbyPlayer lobbyPlayer : players) {
            TDPlayer tdPlayer = lobbyPlayer.getPlayer();
            GameUser gameUser = new GameUser(tdPlayer, this.defaultEnemyMobs, team);
            this.users.put(tdPlayer, gameUser);

            tdPlayer.setFlyingSpeed(tdPlayer.getFlySpeed().getSpeed());
            tdPlayer.teleport(spawnPoint);
            tdPlayer.setGameMode(GameMode.SURVIVAL);
            tdPlayer.setAllowFlying(true);
            tdPlayer.setFlying(true); // set flying here so they don't fall after teleporting
        }
    }

    private @NotNull Component createTowerHologram(Team team) {
        int health = team == Team.RED ? this.redTowerHealth.get() : this.blueTowerHealth.get();
        int maxHealth = 10_000;
        float percentageRemaining = (float) health / maxHealth;
        return ProgressBar.create(
                percentageRemaining,
                40,
                "|",
                NamedTextColor.GREEN,
                NamedTextColor.RED
        );
    }

    public void damageTower(@NotNull Team team, int damage) {
        if (this.ended.get()) return;

        AtomicInteger health = team == Team.RED ? this.redTowerHealth : this.blueTowerHealth;
        Hologram hologram = team == Team.RED ? this.redTowerHologram : this.blueTowerHologram;

        int oldHealth = health.getAndUpdate(currentHealth -> currentHealth - damage);
        int newHealth = health.get();

        hologram.setText(this.createTowerHologram(team));

        if (newHealth <= 0) {
            if (this.ended.compareAndSet(false, true)) {
                this.endGame(team == Team.RED ? Team.BLUE : Team.RED);
            }
        }

        MinecraftServer.getGlobalEventHandler().call(new CastleDamageEvent(team, damage, Math.max(newHealth, 0)));
    }

    public void endGame(Team winningTeam) {
        this.module.setGameState(GameState.END);
        this.shutdownTask();
        // todo properly clean up
    }

    private void shutdownTask() {
        Instant startTime = Instant.now();
        AtomicReference<BossBar> lastBossBar = new AtomicReference<>(null);
        Task task = MinecraftServer.getSchedulerManager().buildTask(() -> {
                    int remainingSeconds = 30 - (int) Duration.between(startTime, Instant.now()).getSeconds();
                    float progress = remainingSeconds / 30f;
                    Component text;
                    if (remainingSeconds > 10)
                        text = Component.text("Game over!").color(NamedTextColor.RED);
                    else
                        text = Component.text("Server will close in %s seconds".formatted(remainingSeconds), NamedTextColor.RED);

                    BossBar newBossBar = BossBar.bossBar(text, progress, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
                    BossBar previousBossBar = lastBossBar.getAndSet(newBossBar);
                    for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                        if (previousBossBar != null) player.hideBossBar(previousBossBar);
                        player.showBossBar(newBossBar);
                    }
                })
                .repeat(250, ChronoUnit.MILLIS)
                .schedule();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
                    task.cancel();

                    if (Environment.isProduction()) {
                        KurushimiMinestomUtils.sendToLobby(MinecraftServer.getConnectionManager().getOnlinePlayers(), () -> {
                            this.kubernetesModule.getSdk().shutdown(AgonesSDKProto.Empty.getDefaultInstance(), new IgnoredStreamObserver<>());
                        }, () -> {
                            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                                player.kick(Component.text("Server shutting down"));
                            }
                            this.kubernetesModule.getSdk().shutdown(AgonesSDKProto.Empty.getDefaultInstance(), new IgnoredStreamObserver<>());
                        });
                    }
                })
                .delay(29, ChronoUnit.SECONDS)
                .schedule();
    }

    public @NotNull MobHandler getMobHandler() {
        return this.mobHandler;
    }

    public @NotNull TowerHandler getTowerHandler() {
        return this.towerHandler;
    }

    public int getCastleHealth(@NotNull Team team) {
        return team == Team.RED ? this.redTowerHealth.get() : this.blueTowerHealth.get();
    }

    public boolean isInGame(Player player) {
        return this.users.containsKey(player);
    }

    public @Nullable GameUser getGameUser(Player player) {
        return this.users.get(player);
    }

    public @NotNull Map<Player, GameUser> getUsers() {
        return this.users;
    }

    public Instance getInstance() {
        return this.instance;
    }
}
