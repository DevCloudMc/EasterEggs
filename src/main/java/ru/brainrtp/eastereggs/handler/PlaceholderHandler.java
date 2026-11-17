package ru.brainrtp.eastereggs.handler;

import java.util.List;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.util.text.PlaceholderHook;

public interface PlaceholderHandler {

  String replace(Player player, String str);

  List<String> replace(Player player, List<String> list);

  void registerHook(PlaceholderHook hook);
}
