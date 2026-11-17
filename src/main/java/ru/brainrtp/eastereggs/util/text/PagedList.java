package ru.brainrtp.eastereggs.util.text;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.EasterEggs;

public final class PagedList {

  private static final String HEADER_LINE = "===============";
  private static final int LIMIT = 10;

  private PagedList() {}

  public static void open(Player player, String title, List<Component> content, int page) {
    long from = (long) (page - 1) * LIMIT;
    if (page >= 1 && from < content.size()) {
      List<Component> lines =
          content.subList((int) from, (int) Math.min(content.size(), from + LIMIT));
      player.sendMessage(HEADER_LINE + " " + title + " " + "(" + page + ") " + HEADER_LINE);
      player.sendMessage(Component.join(JoinConfiguration.newlines(), lines));
      player.sendMessage("");
      return;
    }

    player.sendMessage(EasterEggs.getLanguage().getSingleMessage("egg", "list", "empty"));
  }
}
