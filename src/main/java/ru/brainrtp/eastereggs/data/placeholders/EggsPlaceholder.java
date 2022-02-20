package ru.brainrtp.eastereggs.data.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.services.EasterEggService;

import java.util.List;
import java.util.Optional;

public class EggsPlaceholder extends PlaceholderExpansion {

    private final EasterEggService eggService;

    public EggsPlaceholder() {
//        super(plugin, "ee");

        eggService = EasterEggs.getEggService();
    }


    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
//        System.out.println();
        if (identifier.startsWith("found")) {
            String[] catagoryTitle = identifier.split("_");
            int founded = 0;

            if (catagoryTitle.length == 2) {
                List<EasterEgg> easterEggList = eggService.getPlayerService().getPlayerData(player.getUniqueId());
                if (easterEggList != null) {
                    int foundEasterEggs = easterEggList.stream()
                            .filter(egg -> catagoryTitle[1].contains(egg.getCategory()))
                            .toList()
                            .size();
                    founded = foundEasterEggs;
//                    if (map.containsKey(catagoryTitle[1])) {
//                        founded = map.get(catagoryTitle[1]).getEggs().size();
//                    }
                }
            }

            return String.valueOf(founded);
        }

        if (identifier.startsWith("count")) {
            String[] arr = identifier.split("_");
            int count = 0;

            if (arr.length == 2) {
                Optional<EasterEggCategory> categ = eggService.getCategory(arr[1]);
                if (categ.isPresent()) {
                    count = categ.get().getEggs().size();
                }
            }

            return String.valueOf(count);
        }

        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "ee";
    }

    @Override
    public String getAuthor() {
        return "BrainRTP";
    }

    @Override
    public String getVersion() {
        return "3.0";
    }
}
