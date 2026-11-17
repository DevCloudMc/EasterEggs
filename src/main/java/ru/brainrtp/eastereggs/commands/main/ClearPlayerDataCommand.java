package ru.brainrtp.eastereggs.commands.main;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.commands.Command;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.services.EasterEggService;
import ru.brainrtp.eastereggs.services.PlayerEggsService;
import ru.brainrtp.eastereggs.util.text.Colors;

public class ClearPlayerDataCommand extends Command {

  private final Language language;
  private final PlayerEggsService playerEggsService;
  private final EasterEggService eggService;

  public ClearPlayerDataCommand(Language language, EasterEggService eggService) {
    this.language = language;
    this.eggService = eggService;
    this.playerEggsService = eggService.getPlayerService();
    setUsage(Colors.of("&e/ee clear <player> [category] [id]"));
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    if (args.length == 0) {
      sender.sendMessage(getUsage());
      return;
    }
    if (args.length > 3) {
      return;
    }

    String playerName = args[0];
    Optional<? extends OfflinePlayer> player = findPlayer(playerName);
    if (player.isEmpty()) {
      sender.sendMessage(
          language.getSingleMessage("player", "not_found").replace("{player}", playerName));
      return;
    }
    UUID uuid = player.get().getUniqueId();
    if (args.length == 1) {
      playerEggsService.removeFullPlayerData(uuid);
      sender.sendMessage(
          language
              .getSingleMessage("player", "data", "full", "removed")
              .replace("{player}", playerName));
      return;
    }

    Optional<EasterEggCategory> category = eggService.getCategory(args[1]);
    if (category.isEmpty()) {
      sender.sendMessage(language.getSingleMessage("category", "not_exist"));
      return;
    }
    String categoryName = category.get().getShortCategoryName();
    if (args.length == 2) {
      playerEggsService.removePlayerCategory(uuid, category.get());
      sender.sendMessage(
          language
              .getSingleMessage("player", "data", "category", "removed")
              .replace("{player}", playerName)
              .replace("{category}", categoryName));
      return;
    }

    Optional<EasterEgg> egg = category.get().getEgg(parseInt(args[2]));
    if (egg.isEmpty()) {
      sender.sendMessage(language.getSingleMessage("egg", "not_exist"));
      return;
    }
    playerEggsService.removePlayerEgg(uuid, egg.get());
    sender.sendMessage(
        language
            .getSingleMessage("player", "data", "egg", "removed")
            .replace("{player}", playerName)
            .replace("{category}", categoryName)
            .replace("{egg}", String.valueOf(egg.get().getId())));
  }

  private Optional<? extends OfflinePlayer> findPlayer(String playerName) {

    Optional<? extends OfflinePlayer> optionalPlayer;
    boolean isPlayerOnline =
        Bukkit.getOnlinePlayers().stream().anyMatch(player -> playerName.equals(player.getName()));
    if (isPlayerOnline)
      optionalPlayer =
          Bukkit.getOnlinePlayers().stream()
              .filter(player -> playerName.equals(player.getName()))
              .findFirst();
    else {
      optionalPlayer =
          Arrays.stream(Bukkit.getOfflinePlayers())
              .filter(player -> playerName.equals(player.getName()))
              .findFirst();
    }
    return optionalPlayer;
  }

  @Override
  public List<String> getTabComplete(String[] args) {
    if (args.length == 2) {
      return Bukkit.getOnlinePlayers().stream().map((Player::getName)).toList();
    } else if (args.length == 3) {
      return eggService.getCategoryNames();
    } else return null;
  }
}
