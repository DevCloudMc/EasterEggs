package ru.brainrtp.eastereggs.commands.main.category;

import org.bukkit.command.CommandSender;
import ru.brainrtp.eastereggs.commands.Command;
import ru.brainrtp.eastereggs.util.text.Colors;

import java.util.List;

public class CategoryCommand extends Command {

    public CategoryCommand() {
        setUsage(
                Colors.of("&e/ee category create <name>"),
                Colors.of("&e/ee category delete <name>")
        );
    }

    @Override
    public List<String> getTabComplete(String[] args) {
        return List.of("create", "delete");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(getUsage());
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
        return null;
    }

}
