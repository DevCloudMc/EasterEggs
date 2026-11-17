package ru.brainrtp.eastereggs.commands.main.category;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.commands.Command;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.services.EasterEggService;

public class CreateCategoryCommand extends Command {

  private final Language language;
  private final EasterEggService eggService;

  public CreateCategoryCommand(Language language, EasterEggService eggService) {
    this.language = language;
    this.eggService = eggService;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    if (sender instanceof Player player) {
      if (args.length != 1) {
        player.sendMessage(language.getSingleMessage("errors", "forgot", "name"));
        return;
      }

      if (!EasterEggService.isValidCategoryName(args[0])) {
        player.sendMessage(language.getSingleMessage("category", "create", "invalid_name"));
        return;
      }

      if (!eggService.createNewCategory(args[0])) {
        player.sendMessage(language.getSingleMessage("category", "create", "taken"));
        return;
      }

      player.sendMessage(
          language
              .getSingleMessage("category", "create", "success")
              .replace("{category}", args[0]));
    }
  }
}
