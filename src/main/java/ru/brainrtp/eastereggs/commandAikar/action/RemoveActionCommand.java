package ru.brainrtp.eastereggs.commandAikar.action;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.services.EasterEggService;

import javax.annotation.Syntax;
import java.util.Optional;


@CommandAlias("eastereggs|ee")
@Subcommand("action")
public class RemoveActionCommand extends BaseCommand {

    @Dependency
    private Language lang;

    @Dependency
    private EasterEggService eggService;

    @Subcommand("remove")
    @CommandPermission("eastereggs.admin")
    @Syntax("<egg_id> <action>")
    @Description("Remove action from easter egg")
    @CommandCompletion("<egg_id> @actionList")
    @Conditions("editMode")
    public void onRemoveAction(Player player, Integer eggId, @Conditions("action") @Single String actionName) {
        String categoryName = eggService.getEditor().getEditCategory(player.getUniqueId());
        Optional<EasterEggCategory> category = eggService.getCategory(categoryName);
        if (category.isEmpty()) {
            player.sendMessage(lang.getSingleMessage("category", "not_exist"));
            return;
        }

        Optional<EasterEgg> egg = category.get().getEgg(eggId);

        if (egg.isPresent()) {
            egg.get().removeAction(actionName);
            category.get().save();

            player.sendMessage(lang.getSingleMessage("action", "remove", "success")
                    .replace("{action}", actionName)
                    .replace("{egg}", String.valueOf(egg.get().getId())));
        }

    }

}
