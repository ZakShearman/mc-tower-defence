package pink.zak.minestom.towerdefence.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minestom.server.extensions.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.zak.minestom.towerdefence.TowerDefencePlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.UnaryOperator;

public class FileUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);
    private static final Gson GSON = new Gson();

    public static void createFile(Path path) {
        try {
            path.toFile().createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JsonElement getLocalOrResourceJson(Extension extension, UnaryOperator<Path> pathOperator) {
        Path fullPath = pathOperator.apply(extension.dataDirectory().resolve("TowerDefence"));
        File systemFile = fullPath.toFile();
        if (systemFile.exists()) {
            try {
                return JsonParser.parseReader(new FileReader(systemFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Path hollowPath = pathOperator.apply(Path.of(""));
            try (InputStream inputStream = extension.getPackagedResource(hollowPath)) {
                return JsonParser.parseReader(new InputStreamReader(inputStream));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static JsonObject resourceToJsonObject(Extension extension, String resource) {
        try (InputStream inputStream = extension.getPackagedResource(resource)) {
            return JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JsonObject fileToJsonObject(File file) {
        try {
            return JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveJsonObject(Path path, JsonObject jsonObject) {
        Path parent = path.getParent();
        if (!Files.exists(parent))
            parent.toFile().mkdirs();
        if (!Files.exists(path))
            createFile(path);

        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(jsonObject, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveResource(Path savePath, String resource) {
        Path parent = savePath.getParent();
        if (!parent.toFile().exists())
            parent.toFile().mkdirs();
        else if (savePath.toFile().exists())
            return;

        URL resourceUrl = FileUtils.class.getClassLoader().getResource(resource);
        if (resourceUrl == null) {
            LOGGER.error("Could not locate resource " + resource);
            return;
        }
        try {
            Files.write(savePath, FileUtils.class.getClassLoader().getResourceAsStream(resource).readAllBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
