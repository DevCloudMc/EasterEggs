package ru.brainrtp.eastereggs.commandAikar;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.services.EasterEggService;

import javax.annotation.Syntax;


@CommandAlias("eastereggs|ee")
public class EditCategoryCommand extends BaseCommand {

    @Dependency
    private Language lang;

    @Dependency
    private EasterEggService eggService;

    @Subcommand("edit")
    @CommandPermission("eastereggs.admin")
    @Syntax("<category>")
    @Description("Edit of category")
    @CommandCompletion("@categoryList")
    public void onEditCategory(Player player, @Single String categoryName) {
        if (eggService.getEditor().isEditMode(player.getUniqueId())) {
            eggService.stopEditCategory(player);
        } else {
            eggService.startEditCategory(player, categoryName);
        }
    }

}
