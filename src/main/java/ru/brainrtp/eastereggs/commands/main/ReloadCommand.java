package ru.brainrtp.eastereggs.commands.main;

import api.logging.Logger;
import org.bukkit.command.CommandSender;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.commands.Command;
import ru.brainrtp.eastereggs.configuration.Configuration;
import ru.brainrtp.eastereggs.configuration.Language;

import java.util.List;

public class ReloadCommand extends Command {

    private final Language language;
    private final Configuration mainConfiguration;

    public ReloadCommand(Language language, Configuration mainConfiguration) {
        this.language = language;
        this.mainConfiguration = mainConfiguration;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            EasterEggs.getEggService().reloadAllEggs();
            language.getConfiguration().load();
            mainConfiguration.load();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("Error while reloading plugin: " + e.getMessage());
        }
        sender.sendMessage(language.getSingleMessage("reload", "success"));
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
        return null;
    }
}
