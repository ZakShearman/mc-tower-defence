package pink.zak.minestom.towerdefence.game.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class UserSettingsMenuHandler {

    private final ItemStack settingsItem = ItemStack.builder(Material.PAPER)
        .displayName(Component.text("Settings", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
        .build();
}
