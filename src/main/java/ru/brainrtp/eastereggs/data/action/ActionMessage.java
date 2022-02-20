package ru.brainrtp.eastereggs.data.action;

import api.logging.Logger;
import lombok.Data;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.util.text.Colors;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ActionMessage implements Action {

    private static final String ACTION_NAME = "message";
    private List<String> messages = new ArrayList<>();
    private String title;
    private String subtitle;
    private String actionBar;

    private int fadeIn = 10;
    private int stay = 30;
    private int fadeOut = 10;

    private static final String MESSAGES_NODE = "messages";
    private static final String TITLE_NODE = "title";
    private static final String SUBTITLE_NODE = "subTitle";
    private static final String ACTIONBAR_NODE = "actionBar";
    private static final String FADEIN_NODE = "fadeIn";
    private static final String STAY_NODE = "stay";
    private static final String FADEOUT_NODE = "fadeOut";

    @Override
    public String getActionTitle() {
        return ACTION_NAME;
    }

    @Override
    public void activate(Player player) {
        if (player != null) {
            if (messages != null) {
                for (String str : messages) {
                    // TODO: 28.11.2021 ЧОт хуита с плейсхолдерами какая-то
//                String line = HandlersProvider.getPlaceholderHandler().replace(player, str);
//                player.sendMessage(line);
                    player.sendMessage(str);
                }
            }

            if (actionBar != null) {
//                String actionBarReplaced = HandlersProvider.getPlaceholderHandler().replace(player, actionbar);
                String actionBarReplaced = actionBar;
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Colors.of(actionBarReplaced)));
            }

//            String title = HandlersProvider.getPlaceholderHandler().replace(player, this.title);
//            String subtitle = HandlersProvider.getPlaceholderHandler().replace(player, this.subtitle);

            if (title != null || subtitle != null)
                player.sendTitle(Colors.of(title), Colors.of(subtitle), this.fadeIn, this.stay, this.fadeOut);
        }
    }

    public static class Serializer implements TypeSerializer<ActionMessage> {
        @Override
        public ActionMessage deserialize(Type type, ConfigurationNode node) throws SerializationException {
            ActionMessage actionMessage = new ActionMessage();
//            String asv = Arrays.stream(node.path().array()).map(Object::toString).collect(Collectors.joining("."));

            if (node.node(MESSAGES_NODE).isNull()) {
                actionMessage.setMessages(null);
            } else if (node.node(MESSAGES_NODE).isList()) {
                // TODO: 15.11.2021 Под вопросом т.к должно быть что-то вроде messages.add или .addAll.
                actionMessage.setMessages(node.node(MESSAGES_NODE).getList(String.class));
            } else {
                actionMessage.setMessages(List.of(node.node(MESSAGES_NODE).getString()));
            }

            if (!node.node(TITLE_NODE).isNull())
                actionMessage.setTitle(node.node(TITLE_NODE).getString());

            if (!node.node(SUBTITLE_NODE).isNull())
                actionMessage.setSubtitle(node.node(SUBTITLE_NODE).getString());

            if (!node.node(ACTIONBAR_NODE).isNull())
                actionMessage.setActionBar(node.node(ACTIONBAR_NODE).getString());

            if (!node.node(FADEIN_NODE).isNull()) {
                paramMustBeAnInteger(node, FADEIN_NODE);
                actionMessage.setFadeIn(node.node(FADEIN_NODE).getInt());
            }

            if (!node.node(STAY_NODE).isNull()) {
                paramMustBeAnInteger(node, STAY_NODE);
                actionMessage.setStay(node.node(STAY_NODE).getInt());
            }

            if (!node.node(FADEOUT_NODE).isNull()) {
                paramMustBeAnInteger(node, FADEOUT_NODE);
                actionMessage.setFadeOut(node.node(FADEOUT_NODE).getInt());
            }

            return actionMessage;
        }

        @Override
        public void serialize(Type type, @Nullable ActionMessage actionMessage, ConfigurationNode node) throws SerializationException {
            if (actionMessage.getMessages() != null) {
                if (actionMessage.getMessages().size() > 1)
                    node.node(MESSAGES_NODE).setList(String.class, actionMessage.getMessages());
                else if (actionMessage.getMessages().size() != 0) {
                    node.node(MESSAGES_NODE).set(actionMessage.getMessages().get(0));
                }
            }
            if (actionMessage.getTitle() != null) {
                node.node(TITLE_NODE).set(actionMessage.getTitle());
            }
            if (actionMessage.getSubtitle() != null) {
                node.node(SUBTITLE_NODE).set(actionMessage.getSubtitle());
            }
            if (actionMessage.getActionBar() != null) {
                node.node(ACTIONBAR_NODE).set(actionMessage.getActionBar());
            }
            if (actionMessage.getTitle() != null || actionMessage.getSubtitle() != null) {
                node.node(FADEIN_NODE).set(actionMessage.getFadeIn());
                node.node(FADEOUT_NODE).set(actionMessage.getFadeOut());
                node.node(STAY_NODE).set(actionMessage.getStay());
            }
        }

        private void paramMustBeAnInteger(ConfigurationNode node, String param) {
            if (!(node.node(param).raw() instanceof Integer)) {
                String pathToWarn = Arrays.stream(node.path().array())
                        .map(Object::toString)
                        .collect(Collectors.joining("."))
                        .concat(".");

                String logMessage = """
                        &fThe &e''{0}''&r parameter must be an integer. Right now value is - &e''{1}''. &r
                        Setting the default value to &e0&r
                        Full path to this param - &e{2}&r""";
                Logger.warn(Colors.of(logMessage), param, node.node(param).raw(), pathToWarn + param);
            }
        }
    }
}
