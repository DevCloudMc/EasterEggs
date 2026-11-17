package ru.brainrtp.eastereggs.commands;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.configuration.Language;

public class MainCommand extends Command implements TabCompleter {

  private final Language language;

  public MainCommand(String permission, Language language) {
    super(permission);
    this.language = language;
  }

  // read on every call, so /ee reload shows the new text
  @Override
  public String[] getUsage() {
    return language.getListMessages("adminHelp").toArray(String[]::new);
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    if (sender.hasPermission(getPermission())) {
      sender.sendMessage(getUsage());
    } else {
      sender.sendMessage(language.getListMessages("help").toArray(String[]::new));
    }
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {

    if ((sender instanceof Player) && !sender.hasPermission(getPermission())) {
      return null;
    }

    Match match = resolve(args);
    if (match.depth() > 0) {
      return match.command().getTabComplete(args);
    }
    return List.of("action", "category", "clear", "edit", "list", "reload", "tp");
  }
}
