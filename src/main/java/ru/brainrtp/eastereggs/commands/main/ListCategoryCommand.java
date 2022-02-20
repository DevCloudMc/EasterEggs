package ru.brainrtp.eastereggs.commands.main;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.commands.Command;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.protocol.text.Text;
import ru.brainrtp.eastereggs.protocol.text.actions.ClickActions;
import ru.brainrtp.eastereggs.protocol.text.actions.HoverActions;
import ru.brainrtp.eastereggs.services.EasterEggService;
import ru.brainrtp.eastereggs.util.text.Colors;
import ru.brainrtp.eastereggs.util.text.PagedList;

import java.util.ArrayList;
import java.util.List;

public class ListCategoryCommand extends Command {

    private final Language language;
    private final EasterEggService eggService;

    public ListCategoryCommand(Language language, EasterEggService eggService) {
        this.language = language;
        this.eggService = eggService;
        setUsage(Colors.of("&e/ee list <page>"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            if (args.length > 0) {
                showEggList(args[0], player);
                return;
            }
            showEggList("1", player);
        } else {
            sender.sendMessage(Colors.of("&cOnly for player."));
        }
    }

    private void showEggList(String pageStr, Player player) {
        int page;

        try {
            page = Integer.parseInt(pageStr);
        } catch (NumberFormatException e) {
            player.sendMessage(language.getSingleMessage("errors", "number", "parse")
                    .replace("{page}", pageStr));
            return;
        }

        PagedList pagedList = new PagedList(language.getSingleMessage("egg", "list", "title"));

        List<BaseComponent> content = new ArrayList<>();
        eggService.getAllCategories().forEach(easterEggCategory -> {
            String title = language.getSingleMessage("egg", "list", "category")
                    .replace("{category}", easterEggCategory.getTitle());
            content.add(new Text(title).toBaseComponent()[0]);
            String subTitle;

            for (EasterEgg egg : easterEggCategory.getEggs().values()) {
                subTitle = language.getSingleMessage("egg", "list", "template")
                        .replace("{type}", egg.getType().getStringType())
                        .replace("{id}", String.valueOf(egg.getId()));

                Text text = new Text(subTitle);
                text.onClick(ClickActions.runCommand("/ee tp " + egg.getCategory() + " " + egg.getId()));
                text.onHover(HoverActions.showText(Text.of(language.getSingleMessage("egg", "list", "hover"))));

                content.add(text.toBaseComponent()[0]);
            }
        });

        pagedList.setContent(content);
        pagedList.openPage(page, player);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
        return null;
    }
}
