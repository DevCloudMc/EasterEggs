package ru.brainrtp.eastereggs.commands.main;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.commands.Command;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.services.EasterEggService;
import ru.brainrtp.eastereggs.util.text.Colors;

public class EditCategoryCommand extends Command {

  private final Language language;
  private final EasterEggService eggService;

  public EditCategoryCommand(Language language, EasterEggService eggService) {
    this.language = language;
    this.eggService = eggService;
    setUsage(Colors.of("&e/ee edit <category>"));
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    if (sender instanceof Player player) {
      if (eggService.getEditor().isEditMode(player.getUniqueId())) {
        String currentCategory = eggService.getEditor().getEditCategory(player.getUniqueId());
        eggService.stopEditCategory(player);
        if (args.length >= 1 && !currentCategory.equals(args[0])) {
          eggService.startEditCategory(player, args[0]);
        }
        return;
      }
      if (args.length > 0) {
        eggService.startEditCategory(player, args[0]);
        return;
      }
      player.sendMessage(language.getSingleMessage("errors", "forgot", "name"));

    } else {
      sender.sendMessage(Colors.of("&cOnly for player."));
    }
  }

  @Override
  public List<String> getTabComplete(String[] args) {
    if (args.length == 2) {
      return eggService.getCategoryNames();
    } else return null;
  }
}
