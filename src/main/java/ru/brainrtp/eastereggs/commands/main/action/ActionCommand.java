package ru.brainrtp.eastereggs.commands.main.action;

import org.bukkit.command.CommandSender;
import ru.brainrtp.eastereggs.commands.Command;
import ru.brainrtp.eastereggs.util.text.Colors;

import java.util.Collections;
import java.util.List;

public class ActionCommand extends Command {

    public ActionCommand() {
        setUsage(
                Colors.of("&e/ee action add <id> <action> <data>"),
                Colors.of("&e/ee action remove <id> <action>")
        );
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(getUsage());
    }


    @Override
    public List<String> getTabComplete(String[] args) {
        return List.of("add", "remove");
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
        return Collections.emptyList();
    }
}
