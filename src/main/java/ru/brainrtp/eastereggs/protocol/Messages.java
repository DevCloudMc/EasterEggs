package ru.brainrtp.eastereggs.protocol;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.entity.Player;
import packetwrapper.WrapperPlayServerChat;
import ru.brainrtp.eastereggs.protocol.text.Text;

public class Messages {

    public static void send(Player player, Text text) {
        WrapperPlayServerChat chat = new WrapperPlayServerChat();
        chat.setChatType(EnumWrappers.ChatType.CHAT);
        chat.setMessage(WrappedChatComponent.fromJson(text.toString()));
        chat.sendPacket(player);
    }

    public static void sendAction(Player player, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }

        WrapperPlayServerChat chat = new WrapperPlayServerChat();
        chat.setChatType(EnumWrappers.ChatType.GAME_INFO);
        chat.setMessage(WrappedChatComponent.fromText(message));
        chat.sendPacket(player);
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

}
