package ru.brainrtp.eastereggs.commandAikar;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.command.CommandSender;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.configuration.Language;


@CommandAlias("eastereggs|ee")
public class ReloadCommand extends BaseCommand {

    @Dependency
    private Language lang;

    @Dependency
    private EasterEggs plugin;

    @Subcommand("reload")
    @CommandPermission("eastereggs.admin")
    public void onReload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(lang.getSingleMessage("reload", "success"));
    }
}
