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
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class TowerPresetCommand extends Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(TowerPresetCommand.class);
    private static final Gson GSON = new Gson();

    private static final List<SuggestionEntry> RESOURCE_ENTRIES = TowerPresetCommand.loadResourceEntries();

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
        ArgumentWord presetIdLoadArg = new ArgumentWord("presetId");
        presetIdLoadArg.setSuggestionCallback((sender, context, suggestion) -> {
            String input = context.get("presetId");

            this.getFileSystemEntries().forEach(suggestion::addEntry);
            RESOURCE_ENTRIES.forEach(suggestion::addEntry);
        });

        this.addSyntax(this::executeSave, saveArg, presetIdArg);
        this.addSyntax(this::executeLoad, loadArg, presetIdLoadArg);
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
        boolean isResource = RESOURCE_ENTRIES.stream().anyMatch(entry -> entry.getEntry().equals(presetName));
        String fileName = presetName + ".json";

        JsonArray json;
        try {
            if (isResource) {
                try (Reader reader = new InputStreamReader(TowerPresetCommand.class.getClassLoader().getResourceAsStream("towerPresets/" + fileName))) {
                    json = GSON.fromJson(reader, JsonArray.class);
                }
            } else {
                Path path = Path.of("towerPresets", fileName);
                if (!Files.exists(path)) {
                    sender.sendMessage("Tower preset does not exist.");
                    return;
                }

                try (Reader reader = Files.newBufferedReader(path)) {
                    json = GSON.fromJson(reader, JsonArray.class);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
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

    private static List<SuggestionEntry> loadResourceEntries() {
        URL resource = TowerPresetCommand.class.getClassLoader().getResource("towerPresets");
        if (resource == null) return List.of();

        try (var stream = Files.walk(Path.of(resource.toURI()))) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(fileName -> fileName.substring(0, fileName.lastIndexOf('.')))
                    .map(SuggestionEntry::new)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<SuggestionEntry> getFileSystemEntries() {
        try (var stream = Files.walk(Path.of("towerPresets"))) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(fileName -> fileName.substring(0, fileName.lastIndexOf('.')))
                    .map(SuggestionEntry::new)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
