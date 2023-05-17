package pink.zak.minestom.towerdefence.utils;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class NumberUtils {
    private static final Map<String, Integer> ROMAN_NUMERALS = new LinkedHashMap<>() {{
        put("M", 1000);
        put("CM", 900);
        put("D", 500);
        put("CD", 400);
        put("C", 100);
        put("XC", 90);
        put("L", 50);
        put("XL", 40);
        put("X", 10);
        put("IX", 9);
        put("V", 5);
        put("IV", 4);
        put("I", 1);
    }};

    public static @NotNull String toRomanNumerals(int value) {
        if (value < 1 || value > 3999) throw new IllegalArgumentException("Value must be between 1 and 3999");

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : ROMAN_NUMERALS.entrySet()) {
            while (value >= entry.getValue()) {
                builder.append(entry.getKey());
                value -= entry.getValue();
            }
        }

        return builder.toString();
    }
}
