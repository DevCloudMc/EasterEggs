package ru.brainrtp.eastereggs.data.action;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

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
        strings.forEach(
            command -> Bukkit.dispatchCommand(player, prepare(command, player.getName())));
      } else {
        strings.forEach(command ->
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), prepare(command, player.getName())));
      }
    });
  }

  static String prepare(String command, String playerName) {
    String line = command.replace("%player%", playerName);
    return line.startsWith("/") ? line.substring(1) : line;
  }

  public static class Serializer implements TypeSerializer<ActionCommand> {
    @Override
    public ActionCommand deserialize(Type type, ConfigurationNode node)
        throws SerializationException {
      ActionCommand actionCommand = new ActionCommand();
      put(actionCommand, CommandType.PLAYER, node.node(PLAYER_NODE));
      put(actionCommand, CommandType.CONSOLE, node.node(CONSOLE_NODE));
      return actionCommand;
    }

    private static void put(ActionCommand actionCommand, CommandType type, ConfigurationNode value)
        throws SerializationException {
      if (value.isNull()) {
        return;
      }
      actionCommand
          .getCommands()
          .put(
              type,
              value.isList()
                  ? value.getList(String.class)
                  : Collections.singletonList(value.getString()));
    }

    @Override
    public void serialize(Type type, @Nullable ActionCommand actionCommand, ConfigurationNode node)
        throws SerializationException {
      node.node(CONSOLE_NODE)
          .setList(String.class, actionCommand.getCommands().get(CommandType.CONSOLE));
      node.node(PLAYER_NODE)
          .setList(String.class, actionCommand.getCommands().get(CommandType.PLAYER));
    }
  }

  private enum CommandType {
    PLAYER,
    CONSOLE
  }
}
