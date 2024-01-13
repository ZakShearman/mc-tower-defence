package pink.zak.minestom.towerdefence.game;


import dev.agones.sdk.AgonesSDKProto;
import dev.emortal.api.agonessdk.IgnoredStreamObserver;
import dev.emortal.minestom.core.Environment;
import dev.emortal.minestom.core.module.kubernetes.KubernetesModule;
import dev.emortal.minestom.core.module.messaging.MessagingModule;
import dev.emortal.minestom.core.utils.KurushimiMinestomUtils;
import dev.emortal.minestom.core.utils.ProgressBar;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.hologram.Hologram;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.timer.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.actionbar.ActionBarHandler;
import pink.zak.minestom.towerdefence.agones.GameStateManager;
import pink.zak.minestom.towerdefence.api.event.game.CastleDamageEvent;
import pink.zak.minestom.towerdefence.enums.GameState;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.game.handlers.NecromancerDamageListener;
import pink.zak.minestom.towerdefence.gametracker.GameTrackerHelper;
import pink.zak.minestom.towerdefence.lobby.LobbyManager;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.model.mob.config.EnemyMob;
import pink.zak.minestom.towerdefence.model.tower.TowerManager;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.model.user.LobbyPlayer;
import pink.zak.minestom.towerdefence.model.user.TDPlayer;
import pink.zak.minestom.towerdefence.ui.HotbarHandler;
import pink.zak.minestom.towerdefence.ui.InteractionHandler;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

public final class GameHandler {
    public static final int DEFAULT_TOWER_HEALTH = 500;

    private static final Logger LOGGER = LoggerFactory.getLogger(GameHandler.class);

    private final @NotNull TowerDefenceModule module;
    private final @NotNull TowerDefenceInstance instance;
    private final @NotNull TowerMap map;
    private final @NotNull LobbyManager lobbyManager;
    private final KubernetesModule kubernetesModule;

    private final @NotNull MobHandler mobHandler;
    private final @Nullable GameTrackerHelper gameTrackerHelper;
    private final @NotNull HotbarHandler hotbarHandler;
    private final @NotNull InteractionHandler interactionHandler;

    private final Set<EnemyMob> defaultEnemyMobs;
    private final @NotNull AtomicInteger redTowerHealth = new AtomicInteger(DEFAULT_TOWER_HEALTH);
    private final @NotNull AtomicInteger blueTowerHealth = new AtomicInteger(DEFAULT_TOWER_HEALTH);
    private final @NotNull Map<Player, GameUser> users = new HashMap<>();
    private final AtomicBoolean ended = new AtomicBoolean(false);
    private Hologram redTowerHologram;
    private Hologram blueTowerHologram;
    private final @NotNull TowerManager towerManager;

    public GameHandler(@NotNull TowerDefenceModule module, @NotNull LobbyManager lobbyManager,
                       @NotNull GameStateManager gameStateManager, @Nullable MessagingModule messagingModule) {
        this.module = module;
        this.instance = module.getInstance();
        this.map = this.instance.getTowerMap();
        this.lobbyManager = lobbyManager;
        this.kubernetesModule = module.getKubernetesModule();

        this.towerManager = new TowerManager(this.instance, this);
        this.mobHandler = new MobHandler(module, this);
        this.hotbarHandler = new HotbarHandler(module, MinecraftServer.getGlobalEventHandler()); // todo: replace with game event node
        this.interactionHandler = new InteractionHandler(module, MinecraftServer.getGlobalEventHandler()); // todo: replace with game event node

        this.defaultEnemyMobs = module.getMobStorage().getEnemyMobs()
                .stream()
                .filter(mob -> mob.getLevel(1).getUnlockCost() <= 0)
                .collect(Collectors.toUnmodifiableSet());

        module.getEventNode().addListener(PlayerDisconnectEvent.class, event -> this.users.remove(event.getPlayer()));

        if (messagingModule != null) {
            this.gameTrackerHelper = new GameTrackerHelper(this, gameStateManager, messagingModule.getKafkaProducer());
        } else {
            LOGGER.warn("Messaging module not found, game tracking will not be started");
            this.gameTrackerHelper = null;
        }
    }

    public void start() {
        this.module.setGameState(GameState.GAME);

        Set<LobbyPlayer> lobbyPlayers = this.lobbyManager.getLobbyPlayers();
        Set<LobbyPlayer> redPlayers = lobbyPlayers.stream().filter(lobbyPlayer -> lobbyPlayer.getTeam() == Team.RED).collect(Collectors.toSet());
        Set<LobbyPlayer> bluePlayers = lobbyPlayers.stream().filter(lobbyPlayer -> lobbyPlayer.getTeam() == Team.BLUE).collect(Collectors.toSet());

        this.configureTeam(Team.RED, redPlayers);
        this.configureTeam(Team.BLUE, bluePlayers);
        this.module.getScoreboardManager().startGame();

        new IncomeHandler(this);
        new ActionBarHandler(this, MinecraftServer.getGlobalEventHandler()); // todo: replace with game event node
        new NecromancerDamageListener(MinecraftServer.getGlobalEventHandler()); // todo: replace with game event node
        this.hotbarHandler.initialise(this.users.keySet());
        this.interactionHandler.initialise();

        if (this.gameTrackerHelper != null) {
            this.gameTrackerHelper.startGame();

            MinecraftServer.getSchedulerManager().buildTask(this.gameTrackerHelper::updateGame)
                    .repeat(30, ChronoUnit.SECONDS)
                    .schedule();
        }

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
            GameUser gameUser = new GameUser(tdPlayer, this.defaultEnemyMobs, team, this.mobHandler);
            this.users.put(tdPlayer, gameUser);

            tdPlayer.setFlyingSpeed(tdPlayer.getFlySpeed().getSpeed());
            tdPlayer.teleport(spawnPoint);
            tdPlayer.setGameMode(GameMode.SURVIVAL);
            tdPlayer.setAllowFlying(true);
            tdPlayer.setFlying(true); // set flying here so they don't fall after teleporting
        }
    }

    private @NotNull Component createTowerHologram(@NotNull Team team) {
        int health = team == Team.RED ? this.redTowerHealth.get() : this.blueTowerHealth.get();
        float percentageRemaining = (float) health / DEFAULT_TOWER_HEALTH;
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

        int newHealth = health.updateAndGet(currentHealth -> Math.max(currentHealth - damage, 0));

        hologram.setText(this.createTowerHologram(team));

        if (newHealth == 0) {
            if (this.ended.compareAndSet(false, true)) {
                this.endGame(team == Team.RED ? Team.BLUE : Team.RED);
            }
        }

        MinecraftServer.getGlobalEventHandler().call(new CastleDamageEvent(team, damage, newHealth));
    }

    public void endGame(@NotNull Team winningTeam) {
        this.module.setGameState(GameState.END);
        this.shutdownTask();

        if (this.gameTrackerHelper != null)
            this.gameTrackerHelper.finishGame(winningTeam);

        // todo proper win effect
        Audiences.all().sendMessage(Component.text("Game over! %s team won!".formatted(winningTeam.name()), NamedTextColor.RED));
        // todo properly clean up

        // shutdown components
        this.hotbarHandler.shutdown();
        this.interactionHandler.shutdown();
    }

    private void shutdownTask() {
        Instant startTime = Instant.now();
        AtomicReference<BossBar> lastBossBar = new AtomicReference<>(null);
        Task bossBarTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
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
                    bossBarTask.cancel();

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

    public @NotNull TowerManager getTowerManager() {
        return this.towerManager;
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

    public @NotNull List<GameUser> getTeamUsers(@NotNull Team team) {
        return this.users.values().stream().filter(gameUser -> gameUser.getTeam() == team).toList();
    }

    public @NotNull Map<Player, GameUser> getUsers() {
        return this.users;
    }

    public @NotNull TowerDefenceInstance getInstance() {
        return this.instance;
    }
}
