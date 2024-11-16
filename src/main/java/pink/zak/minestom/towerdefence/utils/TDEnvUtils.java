package pink.zak.minestom.towerdefence.utils;

import dev.emortal.minestom.core.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.zak.minestom.towerdefence.TowerDefenceModule;

public class TDEnvUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(TDEnvUtils.class);

    public static final boolean SEND_AGAINST_SELF = TowerDefenceModule.getFlag("TD_SEND_AGAINST_SELF", false);
    public static final boolean DEBUG_PATH = TowerDefenceModule.getFlag("TD_DEBUG_PATH", !Environment.isProduction());

    // Mob Queue
    public static final long QUEUE_MAX_TIME = TowerDefenceModule.getEnv("TD_QUEUE_MAX_TIME", 45_000L, Long::parseLong); // Max time the queue will take to send.
    public static final long QUEUE_MOB_MULTIPLIER = TowerDefenceModule.getEnv("TD_QUEUE_MOB_MULTIPLIER", 1L, Long::parseLong); // if a user adds one mob, multiply it by this.
    public static final long QUEUE_MOB_TICKS_PER_TICK = TowerDefenceModule.getEnv("TD_QUEUE_TICKS_PER_TICK", 1L, Long::parseLong); // number of ticks to process per tick (sounds confusing I know)

    static {
        LOGGER.info("---===--- TD Environment Variables ---===---");
//        LOGGER.info("TD Enable Test Mode: {}", ENABLE_TEST_MODE);
        LOGGER.info("TD Send Against Self: {}", SEND_AGAINST_SELF);
        LOGGER.info("TD Debug Path: {}", DEBUG_PATH);
        LOGGER.info("---===--- TD Environment Variables ---===---");
    }
}
