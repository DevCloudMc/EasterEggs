package ru.brainrtp.eastereggs.commands.main.action;

import io.leangen.geantyref.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import ru.brainrtp.eastereggs.commands.Command;
import ru.brainrtp.eastereggs.configuration.Configuration;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.action.Action;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.providers.TokensProvider;
import ru.brainrtp.eastereggs.services.EasterEggService;
import ru.brainrtp.eastereggs.util.text.Colors;

public class AddActionCommand extends Command {

  private static final String[] PARAMS = {"<id>", "<action>", "<data>"};

  private final Language language;
  private final EasterEggService eggService;

  public AddActionCommand(Language language, EasterEggService eggService) {
    this.language = language;
    this.eggService = eggService;
    setUsage(Colors.of("&e/ee action add <id> <action> <data>"));
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    if (sender instanceof Player player) {
      if (!eggService.getEditor().isEditMode(player.getUniqueId())) {
        player.sendMessage(language.getSingleMessage("edit", "not_edit"));
        return;
      } else if (args.length < 3) {
        player.sendMessage(
            language
                .getSingleMessage("errors", "forgot", "param")
                .replace("{param}", PARAMS[args.length]));
        return;
      }

      addAction(
          player, args[0], args[1], String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
    }
  }

  @Override
  public List<String> getTabComplete(String[] args) {
    if (args.length == 4) return TokensProvider.getActionTokens().keySet().stream().toList();
    else return null;
  }

  private void addAction(Player player, String id, String actionName, String jsonData) {
    int eggId = parseInt(id);
    if (eggId == -1) {
      player.sendMessage(
          language.getSingleMessage("errors", "number", "parse").replace("{param}", "<id>"));
      return;
    }

    String categoryName = eggService.getEditor().getEditCategory(player.getUniqueId());
    TypeToken<? extends Action> actionType = TokensProvider.getActionType(actionName);

    if (actionType != null) {
      try {
        Action action = parseAction(jsonData, actionType);

        EasterEggCategory category = eggService.getCategory(categoryName).get();
        Optional<EasterEgg> egg = category.getEgg(eggId);

        if (egg.isPresent()) {
          egg.get().addAction(action);
          category.save();

          player.sendMessage(
              language
                  .getSingleMessage("action", "add", "success")
                  .replace("{action}", actionName)
                  .replace("{egg}", String.valueOf(egg.get().getId())));
          return;
        }

        player.sendMessage(language.getSingleMessage("egg", "not_exist"));
      } catch (Exception e) {
        player.sendMessage(language.getSingleMessage("action", "parse", "error"));
      }
      return;
    }

    player.sendMessage(language.getSingleMessage("action", "not_exist"));
  }

  // same HOCON parser and serializers as the category files, so the README examples work
  static Action parseAction(String data, TypeToken<? extends Action> actionType)
      throws IOException {
    ConfigurationNode node =
        HoconConfigurationLoader.builder()
            .source(() -> new BufferedReader(new StringReader(data)))
            .defaultOptions(
                ConfigurationOptions.defaults().serializers(Configuration.getSerializers()))
            .build()
            .load();
    return node.get(actionType);
  }
}
