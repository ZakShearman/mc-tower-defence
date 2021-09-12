package pink.zak.minestom.towerdefence.utils;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.StringJoiner;

public class StringUtils {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.get();

    public static Component parseMessage(String message) {
        return MINI_MESSAGE.parse(message).decoration(TextDecoration.ITALIC, false);
    }

    public static List<Component> parseMessages(List<String> messages) {
        List<Component> components = Lists.newArrayList();
        for (String message : messages)
            components.add(parseMessage(message));

        return components;
    }

    public static String integerToCardinal(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> "N/A";
        };
    }

    public static String namespaceToName(String namespace) {
        String name = namespace.split(":")[1];
        name = name.replace("_", " ");

        if (name.contains(" ")) {
            String[] words = name.split(" ");
            StringJoiner stringJoiner = new StringJoiner(" ");
            for (String word : words)
                stringJoiner.add(capitalise(word));
            name = stringJoiner.toString();
        }
        return capitalise(name);
    }

    public static String capitalise(String word) {
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }
}
