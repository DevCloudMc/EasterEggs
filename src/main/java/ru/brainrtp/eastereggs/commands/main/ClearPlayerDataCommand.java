package ru.brainrtp.eastereggs.commands.main;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.commands.Command;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.services.EasterEggService;
import ru.brainrtp.eastereggs.services.PlayerEggsService;
import ru.brainrtp.eastereggs.util.text.Colors;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClearPlayerDataCommand extends Command {

    private final Language language;
    private final PlayerEggsService playerEggsService;
    private final EasterEggService eggService;

    public ClearPlayerDataCommand(Language language, EasterEggService eggService) {
        this.language = language;
        this.eggService = eggService;
        this.playerEggsService = eggService.getPlayerService();
        setUsage(Colors.of("&e/ee clear <player> [category] [id]"));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // TODO: (19.02 20:05) Kludge :(
        if (args.length == 0) {
            sender.sendMessage(getUsage());
        } else if (args.length == 1) {
            String playerName = args[0];
            Optional<? extends OfflinePlayer> player = findPlayer(args[0]);
            if (player.isPresent()) {
                removeFullPlayerData(player.get().getUniqueId());
                sender.sendMessage(language.getSingleMessage("player", "data", "full", "removed")
                        .replace("{player}", playerName));
            } else {
                sender.sendMessage(language.getSingleMessage("player", "not_found")
                        .replace("{player}", playerName));
            }
        } else if (args.length == 2) {
            String playerName = args[0];
            Optional<? extends OfflinePlayer> player = findPlayer(args[0]);
            if (player.isPresent()) {
                Optional<EasterEggCategory> easterEggCategory = eggService.getCategory(args[1]);
                if (easterEggCategory.isPresent()) {
                    removePlayerCategory(player.get().getUniqueId(), easterEggCategory.get());
                    sender.sendMessage(language.getSingleMessage("player", "data", "category", "removed")
                            .replace("{player}", playerName)
                            .replace("{category}", easterEggCategory.get().getShortCategoryName())
                    );
                } else {
                    sender.sendMessage(language.getSingleMessage("category", "not_exist"));
                }
            } else {
                sender.sendMessage(language.getSingleMessage("player", "not_found")
                        .replace("{player}", playerName));
            }

        } else if (args.length == 3) {
            String playerName = args[0];
            Optional<? extends OfflinePlayer> player = findPlayer(args[0]);
            if (player.isPresent()) {
                Optional<EasterEggCategory> easterEggCategory = eggService.getCategory(args[1]);
                if (easterEggCategory.isPresent()) {
                    int eggId = parseInt(args[2]);
                    Optional<EasterEgg> easterEgg = easterEggCategory.get().getEgg(eggId);
                    if (easterEgg.isPresent()) {
                        removePlayerEgg(player.get().getUniqueId(), easterEgg.get());
                        sender.sendMessage(language.getSingleMessage("player", "data", "egg", "removed")
                                .replace("{player}", playerName)
                                .replace("{category}", easterEggCategory.get().getShortCategoryName())
                                .replace("{egg}", String.valueOf(easterEgg.get().getId()))
                        );
                    } else {
                        sender.sendMessage(language.getSingleMessage("egg", "not_exist"));
                    }
                } else {
                    sender.sendMessage(language.getSingleMessage("category", "not_exist"));
                }
            } else {
                sender.sendMessage(language.getSingleMessage("player", "not_found")
                        .replace("{player}", playerName));
            }

        }
    }

    private Optional<? extends OfflinePlayer> findPlayer(String playerName) {

        Optional<? extends OfflinePlayer> optionalPlayer;
        boolean isPlayerOnline = Bukkit.getOnlinePlayers().stream()
                .anyMatch(player -> playerName.equals(player.getName()));
        if (isPlayerOnline)
            optionalPlayer = Bukkit.getOnlinePlayers().stream()
                    .filter(player -> playerName.equals(player.getName()))
                    .findFirst();
        else {
            optionalPlayer = Arrays.stream(Bukkit.getOfflinePlayers())
                    .filter(player -> playerName.equals(player.getName()))
                    .findFirst();
        }
        return optionalPlayer;
    }


    private void removePlayerEgg(UUID playerUUID, EasterEgg egg) {
        playerEggsService.removePlayerEgg(playerUUID, egg);
    }

    private void removePlayerCategory(UUID playerUUID, EasterEggCategory easterEggCategory) {
        playerEggsService.removePlayerCategory(playerUUID, easterEggCategory);
    }

    private void removeFullPlayerData(UUID playerUUID) {
        playerEggsService.removeFullPlayerData(playerUUID);
    }

    private int parseInt(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public List<String> getTabComplete(String[] args) {
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map((Player::getName))
                    .toList();
        } else if (args.length == 3) {
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
