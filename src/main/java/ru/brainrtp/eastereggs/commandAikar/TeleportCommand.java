package ru.brainrtp.eastereggs.commandAikar;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.services.EasterEggService;
import ru.brainrtp.eastereggs.util.BukkitTasks;
import ru.brainrtp.eastereggs.util.highlighter.Highlighter;
import ru.brainrtp.eastereggs.util.highlighter.Highlighters;

import javax.annotation.Syntax;
import java.util.Optional;


@CommandAlias("eastereggs|ee")
public class TeleportCommand extends BaseCommand {

    @Dependency
    private Language lang;

    @Dependency
    private EasterEggService eggService;

    @Subcommand("teleport")
    @CommandPermission("eastereggs.admin")
    @Syntax("<category> <egg_id>")
    @Description("Teleport to easter egg")
    @CommandCompletion("@categoryList <egg_id>")
    public void onTeleport(Player player, String categoryName, @Single Integer eggId) {
        Optional<EasterEggCategory> category = eggService.getCategory(categoryName);

        if (category.isEmpty()) {
            player.sendMessage(lang.getSingleMessage("category", "not_exist"));
            return;
        }

        Optional<EasterEgg> egg = category.get().getEgg(eggId);

        if (egg.isEmpty()) {
            player.sendMessage(lang.getSingleMessage("egg", "not_exist"));
            return;
        }

        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(egg.get().getLocation());
        createTempHighlight(Highlighters.create(egg.get(), player), player);
    }

    private void createTempHighlight(Highlighter highlighter, Player player) {
        BukkitTasks.runTaskLater(() -> highlighter.clear(player), 20 * 5);
    }
}
