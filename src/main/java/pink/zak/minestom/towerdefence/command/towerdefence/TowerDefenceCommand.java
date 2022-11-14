package pink.zak.minestom.towerdefence.command.towerdefence;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;
import pink.zak.minestom.towerdefence.TowerDefenceModule;
import pink.zak.minestom.towerdefence.game.GameHandler;
import pink.zak.minestom.towerdefence.model.map.TowerMap;
import pink.zak.minestom.towerdefence.world.TowerDefenceInstance;

public class TowerDefenceCommand extends Command {

    public TowerDefenceCommand(TowerDefenceModule module) {
        super("towerdefence", "td");
        EditorSubCommand editorSubCommand = new EditorSubCommand(module);

        TowerDefenceInstance instance = module.getInstance();
        TowerMap map = instance.getTowerMap();

        this.setCondition((sender, commandString) -> { // todo don't just use my username - use a proper permission system
            boolean accessRequest = commandString == null;
            if (sender instanceof Player player) {
                if (!player.getUsername().equals("Expectational")) {
                    if (!accessRequest)
                        player.sendMessage(Component.text("No permission"));
                    return false;
                }
            } else {
                if (!accessRequest)
                    sender.sendMessage(Component.text("You must be a player to execute this command"));
                return false;
            }
            return true;
        });

        ArgumentLiteral editorArg = ArgumentType.Literal("editor");
        ArgumentLiteral setArg = ArgumentType.Literal("set");
        ArgumentLiteral spawnArg = ArgumentType.Literal("spawn");
        ArgumentWord teamArg = ArgumentType.Word("team").from("red", "blue", "spectator");

        ArgumentLiteral forceStartArg = ArgumentType.Literal("forcestart");


        this.addSyntax(editorSubCommand, editorArg);

        this.addSyntax((sender, context) -> {
            Player player = (Player) sender;
            String teamId = context.get(teamArg);
            switch (teamId) {
                case "red" -> map.setRedSpawn(player.getPosition());
                case "blue" -> map.setBlueSpawn(player.getPosition());
                case "spectator" -> map.setSpectatorSpawn(player.getPosition());
            }
            player.sendMessage(Component.text("Set the " + teamId + " spawn to your position", NamedTextColor.GREEN));
            instance.saveTowerMapData();
        }, setArg, spawnArg, teamArg);

        GameHandler gameHandler = module.getGameHandler();
        this.addSyntax((sender, context) -> {
            gameHandler.start();
            sender.sendMessage(Component.text("Force starting the game", NamedTextColor.RED));
        }, forceStartArg);
    }
}
