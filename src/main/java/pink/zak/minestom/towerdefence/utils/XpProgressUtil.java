package pink.zak.minestom.towerdefence.utils;

public class XpProgressUtil {

    /**
     * @param level    level
     * @param levelProgress Progress from 0 to 1
     * @return Amount of XP required for the level and progress
     */
    public static int xpFor(int level, float levelProgress) {
        if (level < 0) throw new IllegalArgumentException("Level cannot be negative");

        int xp = getXpToReachLevel(level);
        int nextLevelXp = getLevelXp(level + 1);

        xp += nextLevelXp * levelProgress;

        return xp;
    }

    public static int getXpToReachLevel(int level) {
        if (level < 0) throw new IllegalArgumentException("Level cannot be negative");

        if (level <= 16) return level ^ 2 + 6 * level;
        if (level <= 31) return (int) (2.5 * (level ^ 2) - 40.5 * level + 360);
        return (int) (4.5 * (level ^ 2) - 162.5 * level + 2220);
    }

    public static int getLevelXp(int level) {
        if (level < 0) throw new IllegalArgumentException("Level cannot be negative");

        if (level <= 16) return 2 * level + 7;
        if (level <= 31) return 5 * level - 38;
        return 9 * level - 158;
    }
}
