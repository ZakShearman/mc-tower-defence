package pink.zak.minestom.towerdefence.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class StringUtils {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static @NotNull Component parseMessage(@NotNull String message, @Nullable TagResolver tagResolver) {
        Component component;
        if (tagResolver == null) {
            component = MINI_MESSAGE.deserialize(message);
        } else {
            component = MINI_MESSAGE.deserialize(message, tagResolver);
        }
        return component.decoration(TextDecoration.ITALIC, false);
    }

    public static @NotNull List<Component> parseMessages(List<String> messages, @Nullable TagResolver tagResolver) {
        List<Component> components = new ArrayList<>();
        for (String message : messages)
            components.add(parseMessage(message, tagResolver));

        return components;
    }

    public static @NotNull List<Component> parseMessages(@Nullable TagResolver tagResolver, @NotNull String... messages) {
        return parseMessages(List.of(messages), tagResolver);
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
