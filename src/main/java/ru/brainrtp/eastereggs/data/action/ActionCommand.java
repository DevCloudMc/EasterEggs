package ru.brainrtp.eastereggs.data.action;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

@Data
public class ActionCommand implements Action {

    private static final String ACTION_NAME = "commands";
    private EnumMap<CommandType, List<String>> commands = new EnumMap<>(CommandType.class);
    private static final String PLAYER_NODE = "player";
    private static final String CONSOLE_NODE = "console";

    @Override
    public String getActionTitle() {
        return ACTION_NAME;
    }

    @Override
    public void activate(Player player) {
        commands.forEach((commandType, strings) -> {
            if (CommandType.PLAYER.equals(commandType)) {
                strings.forEach(command -> {
                    System.out.println("command = " + command);
                    Bukkit.dispatchCommand(player, command);
                });
            } else {
                strings.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
            }
        });
    }

    public static class Serializer implements TypeSerializer<ActionCommand> {
        @Override
        public ActionCommand deserialize(Type type, ConfigurationNode node) throws SerializationException {
            ActionCommand actionCommand = new ActionCommand();
            if (!node.node(PLAYER_NODE).isNull()) {
                if (node.node(PLAYER_NODE).isList()) {
                    EnumMap<CommandType, List<String>> playerCommands = actionCommand.getCommands();
                    playerCommands.put(CommandType.PLAYER, node.node(PLAYER_NODE).getList(String.class));
                    actionCommand.setCommands(playerCommands);
                } else {
                    EnumMap<CommandType, List<String>> playerCommands = actionCommand.getCommands();
                    List<String> singleCommand = Collections.singletonList(node.node(PLAYER_NODE).getString());
                    playerCommands.put(CommandType.PLAYER, singleCommand);
                    actionCommand.setCommands(playerCommands);
                }
            }
            if (!node.node(CONSOLE_NODE).isNull()) {
                if (node.node(CONSOLE_NODE).isList()) {
                    EnumMap<CommandType, List<String>> consoleCommands = actionCommand.getCommands();
                    consoleCommands.put(CommandType.CONSOLE, node.node(CONSOLE_NODE).getList(String.class));
                    actionCommand.setCommands(consoleCommands);
                } else {
                    EnumMap<CommandType, List<String>> consoleCommands = actionCommand.getCommands();
                    List<String> singleCommand = Collections.singletonList(node.node(CONSOLE_NODE).getString());
                    consoleCommands.put(CommandType.CONSOLE, singleCommand);
                    actionCommand.setCommands(consoleCommands);
                }
            }
            return actionCommand;
        }

        @Override
        public void serialize(Type type, @Nullable ActionCommand actionCommand, ConfigurationNode node) throws SerializationException {
            node.node(CONSOLE_NODE).setList(String.class, actionCommand.getCommands().get(CommandType.CONSOLE));
            node.node(PLAYER_NODE).setList(String.class, actionCommand.getCommands().get(CommandType.PLAYER));
        }
    }


    private enum CommandType {
        PLAYER,
        CONSOLE
    }

}
