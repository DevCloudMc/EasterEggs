package ru.brainrtp.eastereggs.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;

import java.util.*;

public abstract class Command implements TabExecutor {

    private String permission;
    private String[] usage;
    private final Map<String, Command> subCommands = new HashMap<>();

    public List<String> getTabComplete(String[] args) {
        return List.of("");
    }

    public Command() {
    }

    public Command(String permission) {
        this.permission = permission;
    }

    public String[] getUsage() {
        return usage;
    }

    public String getPermission() {
        return permission;
    }

    public Command setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public Command addSub(String arg, Command command) {
        subCommands.put(arg, command);
        return this;
    }

    public Command getSub(String arg) {
        return subCommands.get(arg);
    }

    public Command setUsage(String... usage) {
        this.usage = usage;
        return this;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (!checkPermission(sender, this)) {
            // TODO: (17.02 19:38) Kludge :(
            if (sender instanceof Player && sender.hasPermission("ee.user"))
                showStats((Player) sender);
            return false;
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
                    continue;
                }
                break;
            }

            if (!stack.isEmpty()) {
                Command command = stack.pop();
                if (!checkPermission(sender, command)) {
                    // TODO: (17.02 19:38) Kludge :(
                    if (sender instanceof Player && sender.hasPermission("ee.user"))
                        showStats((Player) sender);
                    return false;
                }
                command.execute(sender, Arrays.copyOfRange(args, stack.size() + 1, args.length));
                return true;
            }

            if (usage != null) {
                sender.sendMessage(usage);
            }
            return false;
        }

        execute(sender, args);
        return true;
    }

    private boolean checkPermission(CommandSender sender, Command command) {
        if (command.getPermission() != null) {
            return sender.hasPermission(command.getPermission());
        }
        return true;
    }

    public abstract void execute(CommandSender sender, String[] args);

    private void showStats(Player player) {
        List<EasterEgg> playerData = EasterEggs.getEggService().getPlayerService().getPlayerData(player.getUniqueId());

        List<EasterEggCategory> categories = EasterEggs.getEggService().getAllCategories().stream()
                .filter(easterEggCategory -> !easterEggCategory.isHidePlayerData())
                .toList();

        if (categories.isEmpty() || playerData == null) {
            player.sendMessage(EasterEggs.getLanguage().getSingleMessage("player", "stats", "empty"));
            return;
        }

        for (EasterEggCategory category : categories) {
            if (category.isHidePlayerData()) continue;

//            output="§eНайдено §c%found%§e/§c%count%§e пасхалок (§6%category%§e)"
            long playerCategoryEggCount = playerData.stream()
                    .filter(playerEgg -> category.getShortCategoryName().equals(playerEgg.getCategory()))
                    .count();
            int categoryEggSize = category.getEggs().size();
            String categoryTitle = category.getTitle();
            player.sendMessage(category.getOutput()
                    .replace("%found%", String.valueOf(playerCategoryEggCount))
                    .replace("%count%", String.valueOf(categoryEggSize))
                    .replace("%category%", categoryTitle)
            );

        }
//        int i = 0;
//
//        for(EasterEggCategory category : categories){
//            if(category.isHidePlayerData()){
//                continue;
//            }
//
//            if(playerData != null){
//                if(playerData.containsKey(category.getName())){
//                    EasterEggCategory playerCateg = playerData.get(category.getName());
//                    Placeholder ph = new Placeholder();
//                    ph.addData(Placeholder.CATEGORY, playerCateg.getName());
//                    ph.addData(Placeholder.FOUNDED_EGG, playerCateg.getEggs().size());
//                    ph.addData(Placeholder.EGG_COUNT, category.getEggs().size());
//
//                    player.sendMessage(ph.replace(Colors.of(category.getOutput())));
//                    i++;
//                    continue;
//                }
//            }
//
//            Placeholder ph = new Placeholder();
//            ph.addData(Placeholder.CATEGORY, category.getName());
//            ph.addData(Placeholder.FOUNDED_EGG, 0);
//            ph.addData(Placeholder.EGG_COUNT, category.getEggs().size());
//
//            player.sendMessage(ph.replace(Colors.of(category.getOutput())));
//            i++;
//        }

//        if(i == 0){
//            player.sendMessage(lang.of("player.stats.empty"));
//        }
    }

}
