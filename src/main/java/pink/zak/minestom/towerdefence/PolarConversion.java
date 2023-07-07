package pink.zak.minestom.towerdefence;

import net.hollowcube.polar.AnvilPolar;
import net.hollowcube.polar.PolarWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PolarConversion {

    public static void main(String... args) throws IOException {
        var polarWorld = AnvilPolar.anvilToPolar(Path.of("run/fullworld"));
        var polarWorldBytes = PolarWriter.write(polarWorld);

        Path filePath = Path.of("run/maps/world/world.polar");
        Files.createFile(filePath);
        Files.write(filePath, polarWorldBytes);
    }
}
