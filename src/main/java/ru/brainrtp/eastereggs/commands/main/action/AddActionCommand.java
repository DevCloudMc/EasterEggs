package ru.brainrtp.eastereggs.commands.main.action;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.leangen.geantyref.TypeToken;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.commands.Command;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.action.Action;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.providers.TokensProvider;
import ru.brainrtp.eastereggs.services.EasterEggService;
import ru.brainrtp.eastereggs.util.json.AnnotatedDeserializer;
import ru.brainrtp.eastereggs.util.text.Colors;

import java.util.List;
import java.util.Optional;

public class AddActionCommand extends Command {

    private Gson gson;
    private final Language language;
    private final EasterEggService eggService;

    public AddActionCommand(Language language, EasterEggService eggService) {
        this.language = language;
        this.eggService = eggService;
        setUsage(Colors.of("&e/ee action add <id> <action> <data>"));
        registerGsonAdapter();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            if (!eggService.getEditor().isEditMode(player.getUniqueId())) {
                player.sendMessage(language.getSingleMessage("edit", "not_edit"));
                return;
            } else if (args.length != 3) {
                player.sendMessage(this.getUsage());
                return;
            }

            String json = "";

            for (int i = 2; i < args.length; i++) {
                json += args[i] + " ";
            }

            addAction(player, args[0], args[1], json);
        }

    }


    @Override
    public List<String> getTabComplete(String[] args) {
        if (args.length == 4)
            return TokensProvider.getActionTokens().keySet().stream().toList();
        else
            return null;
    }

    private void addAction(Player player, String id, String actionName, String jsonData) {
        int eggId = parseInt(id);
        if (eggId == -1) {
            player.sendMessage(String.format(language.getSingleMessage("errors", "number", "parse"), "<id>"));
            return;
        }

        String categoryName = eggService.getEditor().getEditCategory(player.getUniqueId());
        TypeToken<? extends Action> actionType = TokensProvider.getActionType(actionName);

        if (actionType != null) {
            try {
                Action action = gson.fromJson(jsonData, actionType.getType());
//                action.se(actionName);


                if (!TokensProvider.getActionTokens().containsKey(actionName)) {
                    player.sendMessage(language.getSingleMessage("action", "parse", "error"));
                    return;
                }

                EasterEggCategory category = eggService.getCategory(categoryName).get();
                Optional<EasterEgg> egg = category.getEgg(eggId);


                if (egg.isPresent()) {
                    egg.get().addAction(action);
                    category.save();

                    player.sendMessage(language.getSingleMessage("action", "add", "success")
                            .replace("{action}", actionName)
                            .replace("{egg}", String.valueOf(egg.get().getId())));
                    return;
                }

                player.sendMessage(language.getSingleMessage("egg", "not_exist"));
            } catch (Exception e) {
                player.sendMessage(language.getSingleMessage("action", "parse", "error"));
                return;
            }
            return;
        }

        player.sendMessage(language.getSingleMessage("action", "not_exist"));
        return;
    }

    private int parseInt(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void registerGsonAdapter() {
        GsonBuilder builder = new GsonBuilder();

        TokensProvider.getActionTokens().forEach((s, typeToken) ->
                builder.registerTypeAdapter(typeToken.getType(), getDeserializer(typeToken.getType())));

        gson = builder.create();
    }

    private <T> AnnotatedDeserializer<T> getDeserializer(T type) {
        return new AnnotatedDeserializer<T>();
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
        return null;
    }
}
