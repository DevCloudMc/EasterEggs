package ru.brainrtp.eastereggs.commandAikar;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.services.EasterEggService;

import javax.annotation.Syntax;
import java.util.List;


@CommandAlias("eastereggs|ee")
public class StatsCommand extends BaseCommand {

    @Dependency
    private Language lang;

    @Dependency
    private EasterEggService eggService;

    @CommandAlias("stats")
    @CommandPermission("eastereggs.player")
    @Description("Get EasterEggs stats")
    public void onGetEggStats(Player player) {
        List<EasterEgg> playerData = eggService.getPlayerService().getPlayerData(player.getUniqueId());

        List<EasterEggCategory> categories = eggService.getAllCategoriesValue().stream()
                .filter(easterEggCategory -> !easterEggCategory.isHidePlayerData())
                .toList();

        if (categories.isEmpty() || playerData == null) {
            player.sendMessage(lang.getSingleMessage("player", "stats", "empty"));
            return;
        }

        for (EasterEggCategory category : categories) {
            if (category.isHidePlayerData()) continue;

            long playerCategoryEggCount = playerData.stream()
                    .filter(playerEgg -> category.getShortCategoryName().equals(playerEgg.getCategory()))
                    .count();
            int categoryEggSize = category.getEggs().size();
            String categoryTitle = category.getTitle();
            player.sendMessage(category.getOutput()
                    .replace("{found}", String.valueOf(playerCategoryEggCount))
                    .replace("{count}", String.valueOf(categoryEggSize))
                    .replace("{category}", categoryTitle)
            );

        }
    }
}
