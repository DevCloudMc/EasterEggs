package ru.brainrtp.eastereggs.commands.main;

import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.brainrtp.eastereggs.commands.Command;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.services.EasterEggService;
import ru.brainrtp.eastereggs.util.highlighter.Highlighter;
import ru.brainrtp.eastereggs.util.highlighter.Highlighters;
import ru.brainrtp.eastereggs.util.text.Colors;

import java.util.List;
import java.util.Optional;

public class TeleportCommand extends Command {

    private final Language language;
    private final EasterEggService eggService;
    private final Plugin plugin;

    public TeleportCommand(Language language, EasterEggService eggService, Plugin plugin) {
        this.language = language;
        this.eggService = eggService;
        this.plugin = plugin;
        setUsage(Colors.of("&e/ee tp <category> <id>"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {

            if (args.length == 0)
                player.sendMessage(language.getSingleMessage("errors", "forgot", "name"));
            else if (args.length == 1)
                player.sendMessage(language.getSingleMessage("errors", "forgot", "id"));

            teleportToEgg(player, args[0], args[1]);

        }
    }

    private void teleportToEgg(Player player, String categoryName, String idStr) {
        Optional<EasterEggCategory> category = eggService.getCategory(categoryName);

        if (category.isPresent()) {
            int id;

            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                player.sendMessage(language.getSingleMessage("egg", "not_exist"));
                return;
            }

            Optional<EasterEgg> egg = category.get().getEgg(id);

            if (egg.isPresent()) {
                player.setGameMode(GameMode.SPECTATOR);
                player.teleport(egg.get().getLocation());
                createTempHighlight(Highlighters.create(egg.get(), player), player);
                return;
            }

            player.sendMessage(language.getSingleMessage("egg", "not_exist"));
            return;
        }

        player.sendMessage(language.getSingleMessage("category", "not_exist"));
    }

    private void createTempHighlight(Highlighter highlighter, Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                highlighter.clear(player);
            }
        }.runTaskLater(plugin, (long) 20 * 5);
    }

    @Override
    public List<String> getTabComplete(String[] args) {
        if (args.length == 2) {
            return eggService.getAllCategories().stream()
                    .map(EasterEggCategory::getShortCategoryName)
                    .toList();
        } else
            return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
        return null;
    }
}
