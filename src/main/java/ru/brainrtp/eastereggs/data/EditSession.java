package ru.brainrtp.eastereggs.data;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.protocol.hologram.Hologram;
import ru.brainrtp.eastereggs.protocol.hologram.Holograms;
import ru.brainrtp.eastereggs.util.highlighter.Highlighter;
import ru.brainrtp.eastereggs.util.highlighter.Highlighters;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EditSession {

    private final Language language;

    private final Player player;
    private final Map<Integer, Hologram> holograms = new HashMap<>();
    private final HashMap<Integer, Highlighter> highlighterMap = new HashMap<>();
    private final String category;

    public EditSession(Language language, Player player, String category) {
        this.language = language;
        this.player = player;
        this.category = category;
    }

    public String getEditableCategory() {
        return category;
    }

    public Player getPlayer() {
        return player;
    }

    public void start() {
        Optional<EasterEggCategory> category = EasterEggs.getEggService().getCategory(this.category);
        Collection<EasterEgg> eggs = category.get().getEggs().values();
        eggs.forEach(easterEgg -> highlighterMap.put(easterEgg.getId(), Highlighters.create(easterEgg, player)));
    }

    public void end() {
        highlighterMap.forEach((key, highlighter) -> highlighter.clear(player));
    }

    public void addNewEgg(EasterEgg egg) {
        highlighterMap.put(egg.getId(), Highlighters.create(egg, player));
    }

    // TODO: (19.02 20:37) удалить т.к нигде не вызывается?)
    public void addHologram(EasterEgg egg) {
        Location loc = egg.getLocation().clone();

        if (EggTypes.BLOCK.equals(egg.getType())) {
            loc = loc.add(0.5, 1.7, 0.5);
        } else {
            loc = loc.add(0, 2.2, 0);
        }

        Hologram hologram = Holograms.create(loc);
//        hologram.setText(String.format(language.getSingleMessage("edit", "hologram"), egg.getId()));
        // TODO: (13.02 18:44) сделать нормальный формат, а не через replace
        hologram.setText(language.getSingleMessageWithoutPrefix("edit", "hologram").replace("{number}", String.valueOf(egg.getId())));
        hologram.show(player);

        holograms.put(egg.getId(), hologram);
    }

    // TODO: (19.02 20:38) изменить название метода т.к он отвечает за другое? Например, removeHighlighter?
    public void removeHologram(int eggId) {
        highlighterMap.get(eggId).clear(player);
//        if (holograms.containsKey(eggId)) {
//            holograms.get(eggId).destroy();
//            holograms.remove(eggId);
//        }
    }

    // TODO: (19.02 20:38) А почему нигде не вызывается? Из-за того, что есть #end() чуть выше?
    public void removeAllHolograms() {
        for (Hologram hologram : holograms.values()) {
            hologram.destroy();
        }

        holograms.clear();
    }

}
