package ru.brainrtp.eastereggs.commandAikar;

import api.logging.Logger;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.protocol.text.Text;
import ru.brainrtp.eastereggs.protocol.text.actions.ClickActions;
import ru.brainrtp.eastereggs.protocol.text.actions.HoverActions;
import ru.brainrtp.eastereggs.services.EasterEggService;
import ru.brainrtp.eastereggs.util.text.PagedList;

import javax.annotation.Syntax;
import java.util.ArrayList;
import java.util.List;


@CommandAlias("eastereggs|ee")
public class ListCategoryCommand extends BaseCommand {

    @Dependency
    private Language lang;

    @Dependency
    private EasterEggService eggService;

    @Subcommand("list")
    @CommandPermission("eastereggs.admin")
    @Syntax("[page]")
    @Description("List of easter eggs")
    @CommandCompletion("[page]")
    public void onListCategory(Player player, @Optional @Single @Default("1") Integer page) {
        PagedList pagedList = new PagedList(lang);

        List<BaseComponent> content = new ArrayList<>();
        eggService.getAllCategoriesValue().forEach(easterEggCategory -> {
            if (easterEggCategory.getEggs().values().size() == 0){
                return;
            }
            String title = lang.getSingleMessageWithoutPrefix("egg", "list", "category")
                    .replace("{category}", easterEggCategory.getTitle());
            content.add(new Text(title).toBaseComponent()[0]);
            String subTitle;

            for (EasterEgg egg : easterEggCategory.getEggs().values()) {
                subTitle = lang.getSingleMessageWithoutPrefix("egg", "list", "template")
                        .replace("{type}", egg.getType().getStringType())
                        .replace("{id}", String.valueOf(egg.getId()));

                Text text = new Text(subTitle);
                text.onClick(ClickActions.runCommand("/ee tp " + egg.getCategory() + " " + egg.getId()));
                text.onHover(HoverActions.showText(Text.of(lang.getSingleMessageWithoutPrefix("egg", "list", "hover", "teleport"))));

                content.add(text.toBaseComponent()[0]);
            }
        });

        pagedList.setContent(content);
        pagedList.openPage(page, player);
    }

}
