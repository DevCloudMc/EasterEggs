package ru.brainrtp.eastereggs.commandAikar.category;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.services.EasterEggService;

import javax.annotation.Syntax;


@CommandAlias("eastereggs|ee")
@Subcommand("category")
public class DeleteCategoryCommand extends BaseCommand {

    @Dependency
    private Language lang;

    @Dependency
    private EasterEggService eggService;

    @Subcommand("delete")
    @CommandPermission("eastereggs.admin")
    @Syntax("<category>")
    @Description("Delete category")
    @CommandCompletion("@categoryList")
    public void onDeleteCategory(Player player, @Conditions("categoryExist") @Single String categoryName) {
        eggService.deleteCategory(categoryName);

        player.sendMessage(lang.getSingleMessage("category", "delete", "success")
                .replace("{category}", categoryName));
    }

}
