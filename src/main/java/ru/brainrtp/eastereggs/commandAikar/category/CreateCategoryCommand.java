package ru.brainrtp.eastereggs.commandAikar.category;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.services.EasterEggService;

import javax.annotation.Syntax;


@CommandAlias("eastereggs|ee")
@Subcommand("category")
public class CreateCategoryCommand extends BaseCommand {

    @Dependency
    private Language lang;

    @Dependency
    private EasterEggService eggService;

    @Subcommand("create")
    @CommandPermission("eastereggs.admin")
    @Syntax("<category>")
    @Description("Create new category")
    @CommandCompletion("<category>")
    public void onCreateCategory(Player player, @Conditions("categoryNotExist") @Single String categoryName) {
        eggService.createNewCategory(categoryName);

        player.sendMessage(lang.getSingleMessage("category", "create", "success")
                .replace("{category}", categoryName));
    }

}
