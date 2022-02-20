package ru.brainrtp.eastereggs.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.configuration.Language;

import java.util.List;
import java.util.Stack;

public class MainCommand extends Command {

    public MainCommand(String permission, Language language) {
        super(permission);
        setUsage(
                language.getListMessages("help").toArray(String[]::new)
        );
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(getUsage());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {

        if ((sender instanceof Player) && !sender.hasPermission(getPermission())) {
            return null;
        }

        if (args.length > 0) {
            Stack<Command> stack = new Stack<>();
            Command sub;

            for (String arg : args) {
                if (stack.isEmpty()) {
                    sub = getSub(arg);
                    if (sub != null) {
                        stack.push(sub);
                        continue;
                    }
                    break;
                }

                sub = stack.peek().getSub(arg);
                if (sub != null) {
                    stack.push(sub);
                    break;
                }
            }

            if (!stack.isEmpty()) {
                Command command = stack.pop();
                return command.getTabComplete(args);
            }
        }
        return List.of("action", "category", "clear", "edit", "list", "reload", "tp");
    }
}
