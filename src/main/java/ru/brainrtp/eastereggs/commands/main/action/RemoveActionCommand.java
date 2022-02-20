package ru.brainrtp.eastereggs.commands.main.action;

import io.leangen.geantyref.TypeToken;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.commands.Command;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.action.Action;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.providers.TokensProvider;
import ru.brainrtp.eastereggs.services.EasterEggService;
import ru.brainrtp.eastereggs.util.text.Colors;

import java.util.List;
import java.util.Optional;

public class RemoveActionCommand extends Command {

    private final Language language;
    private final EasterEggService eggService;

    public RemoveActionCommand(Language language, EasterEggService eggService) {
        this.language = language;
        this.eggService = eggService;
        setUsage(Colors.of("&e/ee action remove <id> <action>"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {

            if (!eggService.getEditor().isEditMode(player.getUniqueId())) {
                player.sendMessage(language.getSingleMessage("edit", "not_edit"));
                return;
            }

            if (args.length != 2) {
                player.sendMessage(this.getUsage());
                return;
            }

            removeAction(player, args[0], args[1]);
        }

    }


    @Override
    public List<String> getTabComplete(String[] args) {
        if (args.length == 4)
            return TokensProvider.getActionTokens().keySet().stream().toList();
        else
            return null;
    }

    private void removeAction(Player player, String id, String actionName) {
        int eggId = parseInt(id);
        if (eggId == -1) {
            player.sendMessage(String.format(language.getSingleMessage("errors", "number", "parse"), "<id>"));
            return;
        }

        String categoryName = eggService.getEditor().getEditCategory(player.getUniqueId());
        TypeToken<? extends Action> actionType = TokensProvider.getActionType(actionName);

        if (actionType != null) {
            Optional<EasterEggCategory> category = eggService.getCategory(categoryName);
            if (!category.isPresent()) {
                player.sendMessage(language.getSingleMessage("category", "not_exist"));
                return;
            }

            Optional<EasterEgg> egg = category.get().getEgg(eggId);

            if (egg.isPresent()) {
                egg.get().removeAction(actionName);
                category.get().save();

                player.sendMessage(language.getSingleMessage("action", "remove", "success")
                        .replace("{action}", actionName)
                        .replace("{egg}", String.valueOf(egg.get().getId())));
                return;
            }

            player.sendMessage(language.getSingleMessage("egg", "not_exist"));
            return;
        }

        player.sendMessage(language.getSingleMessage("action", "not_exist"));
        return;
    }


    private int parseInt(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return -1;
        }
    }


    @Override
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
        return null;
    }
}
