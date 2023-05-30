package pink.zak.minestom.towerdefence.utils;

import dev.emortal.minestom.core.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TDEnvUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(TDEnvUtils.class);

    public static final boolean ENABLE_TEST_MODE;

    static {
        String envValue = System.getenv("ENABLE_TEST_MODE");
        if (envValue != null) ENABLE_TEST_MODE = Boolean.parseBoolean(envValue);
        else ENABLE_TEST_MODE = !Environment.isProduction();

        LOGGER.info("TD Test Mode: {} (env: {}, production: {})", ENABLE_TEST_MODE ? "Enabled" : "Disabled", envValue, Environment.isProduction());
    }
}
