package ru.brainrtp.eastereggs.util.text;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.protocol.Messages;
import ru.brainrtp.eastereggs.protocol.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PagedList {

    private static final String HEADER_LINE = "===============";
    private static final float LIMIT = 10.0f;

    private final Map<Integer, List<BaseComponent>> pages = new HashMap<>();
    private final String title;

    public PagedList(String title) {
        this.title = title;
    }

    public void setContent(List<BaseComponent> content) {
        int pages = (int) Math.ceil((float) content.size() / LIMIT);
        int counter = 0;

        for (int i = 1; i < pages + 1; i++) {
            List<BaseComponent> list = new ArrayList<>();
            list.add(new TextComponent("\n"));

            for (int j = counter; j < counter + LIMIT; j++) {
                try {
                    BaseComponent component = content.get(j);
                    component.addExtra("\n");
                    list.add(component);
                } catch (IndexOutOfBoundsException e) {
                    break;
                }
            }

            counter += LIMIT;
            this.pages.put(i, list);
        }
    }

    public int getPagesCount() {
        return pages.size();
    }

    public List<BaseComponent> getPage(int page) {
        return pages.get(page);
    }

    public void openPage(int page, Player player) {
        if (pages.containsKey(page)) {
            player.sendMessage(HEADER_LINE + " " + title + " " + "(" + page + ") " + HEADER_LINE);
            Messages.send(player, new Text("", getAsArray(pages.get(page))));
            player.sendMessage("");
            return;
        }

        // TODO: (11.02 16:8) дописать.
//        player.sendMessage(EasterEggs.getLang().of("egg.list.empty"));
    }

    private BaseComponent[] getAsArray(List<BaseComponent> list) {
        return list.toArray(new BaseComponent[list.size()]);
    }
}
