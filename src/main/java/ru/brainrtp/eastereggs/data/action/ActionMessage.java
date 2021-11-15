package ru.brainrtp.eastereggs.data.action;

import lombok.Data;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.providers.HandlersProvider;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class ActionMessage implements Action {

    private List<String> messages = new ArrayList<>();
    private String title;
    private String subtitle;
    private String actionbar;

    private int fadeIn = 10;
    private int stay = 30;
    private int fadeOut = 10;

    @Override
    public void activate(Player player) {
        if (player != null) {
            for (String str : messages) {
                String line = HandlersProvider.getPlaceholderHandler().replace(player, str);
                player.sendMessage(line);
            }

            if (actionbar != null) {
                String actionBarReplaced = HandlersProvider.getPlaceholderHandler().replace(player, actionbar);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarReplaced));
            }

            String title = HandlersProvider.getPlaceholderHandler().replace(player, this.title);
            String subtitle = HandlersProvider.getPlaceholderHandler().replace(player, this.subtitle);

            player.sendTitle(title, subtitle, this.fadeIn, this.stay, this.fadeOut);
        }
    }

    @Override
    public void test() {
        System.out.println("messages = " + messages);
        System.out.println("title = " + title);
        System.out.println("subtitle = " + subtitle);
        System.out.println("actionbar = " + actionbar);
    }

    public static class Serializer implements TypeSerializer<ActionMessage> {
        @Override
        public ActionMessage deserialize(Type type, ConfigurationNode node) throws SerializationException {
            ActionMessage actionMessage = new ActionMessage();
            if (!node.node("messages").isNull()) {
                actionMessage.setMessages(null);
            } else if (node.node("messages").isList())
                // TODO: 15.11.2021 Под вопросом т.к должно быть что-то вроде messages.add или .addAll.
                actionMessage.setMessages(node.node("messages").getList(String.class));
            else {
                actionMessage.setMessages(Arrays.asList(node.node("messages").getString()));
            }

            if (!node.node("title").isNull())
                actionMessage.setTitle(node.node("title").getString());

            if (!node.node("subTitle").isNull())
                actionMessage.setSubtitle(node.node("subTitle").getString());

            if (!node.node("actionBar").isNull())
                actionMessage.setActionbar(node.node("actionBar").getString());

            if (!node.node("fadeIn").isNull())
                actionMessage.setFadeIn(node.node("fadeIn").getInt());

            if (!node.node("stay").isNull())
                actionMessage.setStay(node.node("stay").getInt());

            if (!node.node("fadeOut").isNull())
                actionMessage.setFadeOut(node.node("fadeOut").getInt());

            return actionMessage;
        }

        @Override
        public void serialize(Type type, @Nullable ActionMessage obj, ConfigurationNode node) throws SerializationException {

        }
    }
}
