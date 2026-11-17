package ru.brainrtp.eastereggs.commands;

import java.util.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.services.PlayerEggsService;
import ru.brainrtp.eastereggs.storage.EggKey;
import ru.brainrtp.eastereggs.util.text.Colors;

public abstract class Command implements TabExecutor {

  private String permission;
  private String[] usage;
  private final Map<String, Command> subCommands = new HashMap<>();

  public List<String> getTabComplete(String[] args) {
    return List.of("");
  }

  public Command() {}

  public Command(String permission) {
    this.permission = permission;
  }

  public String[] getUsage() {
    return usage;
  }

  public String getPermission() {
    return permission;
  }

  public Command setPermission(String permission) {
    this.permission = permission;
    return this;
  }

  public Command addSub(String arg, Command command) {
    subCommands.put(arg, command);
    return this;
  }

  public Command getSub(String arg) {
    return subCommands.get(arg);
  }

  public Command setUsage(String... usage) {
    this.usage = usage;
    return this;
  }

  public record Match(Command command, int depth) {}

  // the deepest subcommand named by the leading args; execution and tab completion both use it
  public Match resolve(String[] args) {
    Command current = this;
    int depth = 0;
    while (depth < args.length) {
      Command sub = current.getSub(args[depth]);
      if (sub == null) break;
      current = sub;
      depth++;
    }
    return new Match(current, depth);
  }

  @Override
  public boolean onCommand(
      CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
    if (!checkPermission(sender, this)) {
      deny(sender);
      return false;
    }

    Match match = resolve(args);
    if (match.depth() > 0) {
      if (!checkPermission(sender, match.command())) {
        deny(sender);
        return false;
      }
      match.command().execute(sender, Arrays.copyOfRange(args, match.depth(), args.length));
      return true;
    }

    if (args.length > 0) {
      if (getUsage() != null) {
        sender.sendMessage(getUsage());
      }
      return false;
    }

    execute(sender, args);
    return true;
  }

  private void deny(CommandSender sender) {
    // TODO: (17.02 19:38) Kludge :(
    if (sender instanceof Player && sender.hasPermission("ee.user")) showStats((Player) sender);
    else sender.sendMessage(EasterEggs.getLanguage().getSingleMessage("permission", "deny"));
  }

  private boolean checkPermission(CommandSender sender, Command command) {
    if (command.getPermission() != null) {
      return sender.hasPermission(command.getPermission());
    }
    return true;
  }

  public abstract void execute(CommandSender sender, String[] args);

  private void showStats(Player player) {
    PlayerEggsService playerService = EasterEggs.getEggService().getPlayerService();
    Set<EggKey> playerData = playerService.getPlayerData(player.getUniqueId());

    List<EasterEggCategory> categories =
        EasterEggs.getEggService().getAllCategories().stream()
            .filter(easterEggCategory -> !easterEggCategory.isHidePlayerData())
            .toList();

    if (categories.isEmpty() || playerData == null) {
      player.sendMessage(EasterEggs.getLanguage().getSingleMessage("player", "stats", "empty"));
      return;
    }

    for (EasterEggCategory category : categories) {
      if (category.isHidePlayerData()) continue;

      //            output="§eНайдено §c%found%§e/§c%count%§e пасхалок (§6%category%§e)"
      long playerCategoryEggCount = playerService.countFound(player.getUniqueId(), category);
      int categoryEggSize = category.getEggs().size();
      String categoryTitle = category.getTitle();
      String output =
          category.getOutput() != null
              ? category.getOutput()
              : EasterEggs.getLanguage().getSingleMessageWithoutPrefix("player", "stats", "info");
      player.sendMessage(
          Colors.component(
              output
                  .replace("%found%", String.valueOf(playerCategoryEggCount))
                  .replace("%count%", String.valueOf(categoryEggSize))
                  .replace("%category%", categoryTitle)));
    }
    //        int i = 0;
    //
    //        for(EasterEggCategory category : categories){
    //            if(category.isHidePlayerData()){
    //                continue;
    //            }
    //
    //            if(playerData != null){
    //                if(playerData.containsKey(category.getName())){
    //                    EasterEggCategory playerCateg = playerData.get(category.getName());
    //                    Placeholder ph = new Placeholder();
    //                    ph.addData(Placeholder.CATEGORY, playerCateg.getName());
    //                    ph.addData(Placeholder.FOUNDED_EGG, playerCateg.getEggs().size());
    //                    ph.addData(Placeholder.EGG_COUNT, category.getEggs().size());
    //
    //                    player.sendMessage(ph.replace(Colors.of(category.getOutput())));
    //                    i++;
    //                    continue;
    //                }
    //            }
    //
    //            Placeholder ph = new Placeholder();
    //            ph.addData(Placeholder.CATEGORY, category.getName());
    //            ph.addData(Placeholder.FOUNDED_EGG, 0);
    //            ph.addData(Placeholder.EGG_COUNT, category.getEggs().size());
    //
    //            player.sendMessage(ph.replace(Colors.of(category.getOutput())));
    //            i++;
    //        }

    //        if(i == 0){
    //            player.sendMessage(lang.of("player.stats.empty"));
    //        }
  }
}
