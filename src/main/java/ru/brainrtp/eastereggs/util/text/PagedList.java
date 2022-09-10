package ru.brainrtp.eastereggs.util.text;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.configuration.Language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PagedList {

    private static final String HEADER_LINE = "===============";
    private static final float LIMIT = 10.0f;

    private final Map<Integer, List<BaseComponent>> pages = new HashMap<>();
    private final String title;

    public PagedList(Language language) {
        this.title = language.getSingleMessageWithoutPrefix("egg", "list", "title");
    }

    public void setContent(List<BaseComponent> content) {
        int pageSize = (int) Math.ceil((float) content.size() / LIMIT);
        int counter = 0;

        for (int currentPage = 1; currentPage < pageSize + 1; currentPage++) {
            List<BaseComponent> list = new ArrayList<>();
            list.add(new TextComponent("\n"));

            for (int currentItem = counter; currentItem < counter + LIMIT; currentItem++) {
                try {
                    BaseComponent component = content.get(currentItem);
                    list.add(component);
                } catch (IndexOutOfBoundsException e) {
                    break;
                }
            }

            counter += LIMIT;
            this.pages.put(currentPage, list);
        }
    }

    public int getPagesCount() {
        return pages.size();
    }

    public List<BaseComponent> getPage(int page) {
        return pages.get(page);
    }

    public void openPage(int page, Player player) {
        pages.keySet().forEach(System.out::println);
        if (pages.containsKey(page)) {
            player.sendMessage(String.format("%s %s (%s) %s", HEADER_LINE, title, page, HEADER_LINE));
            ComponentBuilder append = new ComponentBuilder().append(getAsArray(pages.get(page)));
            append.getParts().forEach(baseComponent -> player.spigot().sendMessage(baseComponent));
            player.sendMessage("");
            return;
        }
        player.sendMessage(EasterEggs.getLanguage().getSingleMessage("egg", "list", "empty"));
    }

    private BaseComponent[] getAsArray(List<BaseComponent> list) {
        return list.toArray(new BaseComponent[list.size()]);
    }
}
