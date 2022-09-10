package ru.brainrtp.eastereggs.commandAikar.action;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.google.gson.Gson;
import io.leangen.geantyref.TypeToken;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.action.Action;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.providers.TokensProvider;
import ru.brainrtp.eastereggs.services.EasterEggService;

import javax.annotation.Syntax;
import java.util.Optional;


@CommandAlias("eastereggs|ee")
@Subcommand("action")
public class AddActionCommand extends BaseCommand {

    @Dependency
    private Gson gson;

    @Dependency
    private Language lang;

    @Dependency
    private EasterEggService eggService;

    @Subcommand("add")
    @CommandPermission("eastereggs.admin")
    @Syntax("<egg_id> <action> <data>")
    @Description("Add new action for easter egg")
    @CommandCompletion("<egg_id> @actionList <data>")
    @Conditions("editMode")
    public void onAddAction(Player player, Integer eggId, @Conditions("action") String actionName, String jsonData) {
        String categoryName = eggService.getEditor().getEditCategory(player.getUniqueId());
        TypeToken<? extends Action> actionType = TokensProvider.getActionType(actionName);

        try {
            Action action = gson.fromJson(jsonData, actionType.getType());

            if (!TokensProvider.getActionTokens().containsKey(actionName)) {
                player.sendMessage(lang.getSingleMessage("action", "parse", "error"));
                return;
            }

            Optional<EasterEggCategory> categoryOptional = eggService.getCategory(categoryName);
            if (categoryOptional.isEmpty()) {
                player.sendMessage(lang.getSingleMessage("category", "not_exist"));
                return;
            }
            EasterEggCategory category = categoryOptional.get();

            Optional<EasterEgg> egg = category.getEgg(eggId);
            if (egg.isEmpty()) {
                player.sendMessage(lang.getSingleMessage("egg", "not_exist"));
                return;
            }

            egg.get().addAction(action);
            category.save();

            player.sendMessage(lang.getSingleMessage("action", "add", "warn"));
            player.sendMessage(lang.getSingleMessage("action", "add", "success")
                    .replace("{action}", actionName)
                    .replace("{egg}", String.valueOf(egg.get().getId())));

        } catch (Exception e) {
            player.sendMessage(lang.getSingleMessage("action", "parse", "error"));
        }

    }

}
