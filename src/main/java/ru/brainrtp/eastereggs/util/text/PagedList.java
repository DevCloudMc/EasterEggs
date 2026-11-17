package ru.brainrtp.eastereggs.util.text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.entity.Player;

public class PagedList {

  private static final String HEADER_LINE = "===============";
  private static final int LIMIT = 10;

  private final Map<Integer, List<Component>> pages = new HashMap<>();
  private final String title;

  public PagedList(String title) {
    this.title = title;
  }

  public void setContent(List<Component> content) {
    for (int i = 0; i * LIMIT < content.size(); i++) {
      pages.put(i + 1, content.subList(i * LIMIT, Math.min(content.size(), (i + 1) * LIMIT)));
    }
  }

  public int getPagesCount() {
    return pages.size();
  }

  public List<Component> getPage(int page) {
    return pages.get(page);
  }

  public void openPage(int page, Player player) {
    if (pages.containsKey(page)) {
      player.sendMessage(HEADER_LINE + " " + title + " " + "(" + page + ") " + HEADER_LINE);
      player.sendMessage(Component.join(JoinConfiguration.newlines(), pages.get(page)));
      player.sendMessage("");
      return;
    }

    // TODO: (21.02 15:14) Add :)
    //        player.sendMessage(EasterEggs.getLang().of("egg.list.empty"));
  }
}
