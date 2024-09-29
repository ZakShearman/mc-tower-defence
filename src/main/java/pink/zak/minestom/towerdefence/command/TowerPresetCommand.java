package pink.zak.minestom.towerdefence.command;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonWriter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.preset.TowerPreset;
import pink.zak.minestom.towerdefence.model.tower.TowerManager;
import pink.zak.minestom.towerdefence.model.tower.placed.PlacedTower;
import pink.zak.minestom.towerdefence.model.user.GameUser;
import pink.zak.minestom.towerdefence.storage.TowerStorage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class TowerPresetCommand extends Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(TowerPresetCommand.class);
    private static final Gson GSON = new Gson();

    private final @NotNull GameHandler gameHandler;
    private final @NotNull TowerManager towerManager;
    private final @NotNull TowerStorage towerStorage;

    public TowerPresetCommand(@NotNull GameHandler gameHandler, @NotNull TowerStorage towerStorage) {
        super("towerpreset");
        this.gameHandler = gameHandler;
        this.towerManager = gameHandler.getTowerManager();
        this.towerStorage = towerStorage;

        ArgumentLiteral saveArg = new ArgumentLiteral("save");
        ArgumentLiteral loadArg = new ArgumentLiteral("load");
        ArgumentWord presetIdArg = new ArgumentWord("presetId");

        this.addSyntax(this::executeSave, saveArg, presetIdArg);
        this.addSyntax(this::executeLoad, loadArg, presetIdArg);
    }

    private void executeSave(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("You must be a player to use this command.");
            return;
        }

        String fileName = context.get("presetId") + ".json";
        Path path = Path.of("towerPresets", fileName);
        if (Files.exists(path)) {
            sender.sendMessage("Overwriting existing tower preset...");
        } else {
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            } catch (Exception e) {
                LOGGER.error("Failed to create tower preset file", e);
                sender.sendMessage("Failed to create tower preset file.");
                return;
            }
        }

        GameUser user = this.gameHandler.getGameUser(player);
        Set<PlacedTower<?>> towers = this.gameHandler.getTowerManager().getTowers(user.getTeam());
        TowerPreset preset = new TowerPreset(towers);

        // Save the tower preset
        try (JsonWriter writer = GSON.newJsonWriter(Files.newBufferedWriter(path))) {
            GSON.toJson(preset.toJson(), writer);
            sender.sendMessage("Tower preset saved.");
        } catch (Exception e) {
            LOGGER.error("Failed to save tower preset", e);
            sender.sendMessage("Failed to save tower preset.");
        }
    }

    private void executeLoad(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("You must be a player to use this command.");
            return;
        }

        String presetName = context.get("presetId");
        String fileName = presetName + ".json";
        Path path = Path.of("towerPresets", fileName);
        if (!Files.exists(path)) {
            sender.sendMessage("Tower preset does not exist.");
            return;
        }

        JsonArray json;
        try {
            json = GSON.fromJson(Files.newBufferedReader(path), JsonArray.class);
        } catch (Exception e) {
            LOGGER.error("Failed to load tower preset", e);
            sender.sendMessage("Failed to load tower preset.");
            return;
        }

        TowerPreset preset = new TowerPreset(json);
        GameUser user = this.gameHandler.getGameUser(player);

        this.towerManager.removeAllTowers(user.getTeam());
        preset.placeTowers(this.towerStorage, this.towerManager, user);
        sender.sendMessage(Component.text("Loaded preset %s with %s towers.".formatted(presetName, preset.getTowers().size()), NamedTextColor.GREEN));
    }
}
