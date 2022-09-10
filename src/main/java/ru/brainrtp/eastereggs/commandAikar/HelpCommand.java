package ru.brainrtp.eastereggs.commandAikar;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import org.bukkit.command.CommandSender;
import ru.brainrtp.eastereggs.configuration.Language;


@CommandAlias("eastereggs|ee")
public class HelpCommand extends BaseCommand {

    @Dependency
    private Language lang;

    @co.aikar.commands.annotation.HelpCommand
    @CommandPermission("eastereggs.command.help")
    public void onHelp(CommandSender sender, CommandHelp help) {
        sender.sendMessage(lang.getListMessages("help").toArray(String[]::new));
    }
}
