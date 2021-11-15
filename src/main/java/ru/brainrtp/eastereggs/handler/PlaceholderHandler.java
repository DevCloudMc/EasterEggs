package ru.brainrtp.eastereggs.handler;

import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.util.text.PlaceholderHook;

import java.util.List;

public interface PlaceholderHandler {

    String replace(Player player, String str);

    List<String> replace(Player player, List<String> list);

    void registerHook(PlaceholderHook hook);

}
