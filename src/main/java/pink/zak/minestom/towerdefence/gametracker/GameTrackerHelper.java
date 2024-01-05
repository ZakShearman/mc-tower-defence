package pink.zak.minestom.towerdefence.gametracker;

import com.google.protobuf.Any;
import dev.emortal.api.message.gametracker.CommonGameData;
import dev.emortal.api.message.gametracker.GameFinishMessage;
import dev.emortal.api.message.gametracker.GameStartMessage;
import dev.emortal.api.message.gametracker.GameUpdateMessage;
import dev.emortal.api.model.gametracker.*;
import dev.emortal.api.utils.ProtoTimestampConverter;
import dev.emortal.api.utils.kafka.FriendlyKafkaProducer;
import dev.emortal.minestom.core.Environment;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import pink.zak.minestom.towerdefence.agones.GameStateManager;
import pink.zak.minestom.towerdefence.enums.Team;
import pink.zak.minestom.towerdefence.game.GameHandler;

import java.util.ArrayList;
import java.util.List;

public class GameTrackerHelper {
    private final GameHandler gameHandler;
    private final GameStateManager gameStateManager;
    private final FriendlyKafkaProducer kafkaProducer;

    // originalCommonData is the first common data sent and will have all the original players in the game to be reused
    private CommonGameData originalCommonData;

    public GameTrackerHelper(@NotNull GameHandler gameHandler, @NotNull GameStateManager gameStateManager,
                             @NotNull FriendlyKafkaProducer kafkaProducer) {
        this.gameHandler = gameHandler;
        this.gameStateManager = gameStateManager;
        this.kafkaProducer = kafkaProducer;
    }

    public void startGame() {
        this.originalCommonData = this.createCommonData();

        GameStartMessage gameStartMessage = GameStartMessage.newBuilder()
                .setStartTime(ProtoTimestampConverter.now())
                .setCommonData(this.originalCommonData)
                .setMapId(this.gameStateManager.getGameCreationInfo().mapId())
                .addAllContent(List.of(
                        Any.pack(TowerDefenceStartData.newBuilder().setHealthData(this.createHealthData()).build()),
                        Any.pack(this.createTeamData())
                )).build();

        this.kafkaProducer.produceAndForget(gameStartMessage);
    }

    public void updateGame() {
        GameUpdateMessage gameUpdateMessage = GameUpdateMessage.newBuilder()
                .setCommonData(this.createCommonData())
                .addAllContent(List.of(
                        Any.pack(TowerDefenceUpdateData.newBuilder().setHealthData(this.createHealthData()).build()),
                        Any.pack(this.createTeamData())
                )).build();

        this.kafkaProducer.produceAndForget(gameUpdateMessage);
    }

    public void finishGame(@NotNull Team winningTeam) {
        List<String> winnerIds = this.gameHandler.getTeamUsers(winningTeam).stream()
                .map(user -> user.getPlayer().getUuid().toString())
                .toList();

        GameFinishMessage gameFinishMessage = GameFinishMessage.newBuilder()
                .setCommonData(this.createCommonData())
                .setEndTime(ProtoTimestampConverter.now())
                .addAllContent(List.of(
                        Any.pack(TowerDefenceFinishData.newBuilder().setHealthData(this.createHealthData()).build()),
                        Any.pack(this.createTeamData()),
                        Any.pack(CommonGameFinishWinnerData.newBuilder()
                                .addAllWinnerIds(winnerIds)
                                .addAllLoserIds(this.originalCommonData.getPlayersList().stream()
                                        .map(BasicGamePlayer::getId)
                                        .filter(playerId -> !winnerIds.contains(playerId))
                                        .toList())
                                .build())
                )).build();

        this.kafkaProducer.produceAndForget(gameFinishMessage);
    }

    private @NotNull CommonGameData createCommonData() {
        return CommonGameData.newBuilder()
                .setGameId(this.gameStateManager.getGameCreationInfo().id())
                .setServerId(Environment.getHostname())
                .addAllPlayers(this.currentPlayers())
                .build();
    }

    private @NotNull TowerDefenceHealthData createHealthData() {
        return TowerDefenceHealthData.newBuilder()
                .setRedHealth(this.gameHandler.getCastleHealth(Team.RED))
                .setBlueHealth(this.gameHandler.getCastleHealth(Team.BLUE))
                .setMaxHealth(GameHandler.DEFAULT_TOWER_HEALTH)
                .build();
    }

    private @NotNull CommonGameTeamData createTeamData() {
        return CommonGameTeamData.newBuilder()
                .addAllTeams(List.of(this.createTeam(Team.RED), this.createTeam(Team.BLUE)))
                .build();
    }

    private @NotNull List<BasicGamePlayer> currentPlayers() {
        List<BasicGamePlayer> players = new ArrayList<>();

        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            players.add(BasicGamePlayer.newBuilder()
                    .setId(player.getUuid().toString())
                    .setUsername(player.getUsername()).build());
        }

        return players;
    }

    private @NotNull dev.emortal.api.model.gametracker.Team createTeam(@NotNull Team team) {
        return dev.emortal.api.model.gametracker.Team.newBuilder()
                .setId(team.name())
                .setColor(team.getColor().value())
                .addAllPlayerIds(
                        this.gameHandler.getTeamUsers(team).stream()
                                .map(user -> user.getPlayer().getUuid().toString())
                                .toList())
                .build();
    }
}
