package ru.brainrtp.eastereggs.data.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.services.EasterEggService;

public class EggsPlaceholder extends PlaceholderExpansion {

  private final EasterEggService eggService;

  public EggsPlaceholder() {
    //        super(plugin, "ee");

    eggService = EasterEggs.getEggService();
  }

  @Override
  public String onRequest(OfflinePlayer player, String identifier) {
    String[] parts = identifier.split("_", 2);
    if (parts.length != 2) {
      return null;
    }

    if (parts[0].equals("found")) {
      if (player == null) {
        return null;
      }
      return String.valueOf(
          eggService
              .getCategory(parts[1])
              .map(category ->
                  eggService.getPlayerService().countFound(player.getUniqueId(), category))
              .orElse(0L));
    }

    if (parts[0].equals("count")) {
      return String.valueOf(
          eggService.getCategory(parts[1]).map(category -> category.getEggs().size()).orElse(0));
    }

    return null;
  }

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  public String getRequiredPlugin() {
    return "EasterEggs";
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
    return EasterEggs.getPlugin().getPluginMeta().getVersion();
  }
}
