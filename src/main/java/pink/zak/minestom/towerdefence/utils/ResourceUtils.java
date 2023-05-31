package pink.zak.minestom.towerdefence.utils;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ResourceUtils {

    public static @NotNull List<String> listResources(@NotNull String path) throws IOException {
        List<String> resources = new ArrayList<>();
        try (InputStream in = ResourceUtils.class.getClassLoader().getResourceAsStream(path);
             BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

            String resource;
            while ((resource = br.readLine()) != null) {
                resources.add(resource);
            }
        }

        return resources;
    }
}
