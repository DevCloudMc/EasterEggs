package ru.brainrtp.eastereggs.commands.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.brainrtp.eastereggs.commands.Command;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.services.EasterEggService;
import ru.brainrtp.eastereggs.util.highlighter.Highlighter;
import ru.brainrtp.eastereggs.util.highlighter.Highlighters;
import ru.brainrtp.eastereggs.util.text.Colors;

public class TeleportCommand extends Command implements Listener {

  private final Language language;
  private final EasterEggService eggService;
  private final Plugin plugin;
  private final Map<UUID, GameMode> previousModes = new HashMap<>();

  public TeleportCommand(Language language, EasterEggService eggService, Plugin plugin) {
    this.language = language;
    this.eggService = eggService;
    this.plugin = plugin;
    setUsage(Colors.of("&e/ee tp <category> <id>"));
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    if (sender instanceof Player player) {

      if (args.length == 0) {
        player.sendMessage(language.getSingleMessage("errors", "forgot", "name"));
        return;
      }
      if (args.length == 1) {
        player.sendMessage(language.getSingleMessage("errors", "forgot", "id"));
        return;
      }

      teleportToEgg(player, args[0], args[1]);
    }
  }

  private void teleportToEgg(Player player, String categoryName, String idStr) {
    Optional<EasterEggCategory> category = eggService.getCategory(categoryName);

    if (category.isPresent()) {
      int id;

      try {
        id = Integer.parseInt(idStr);
      } catch (NumberFormatException e) {
        player.sendMessage(language.getSingleMessage("egg", "not_exist"));
        return;
      }

      Optional<EasterEgg> egg = category.get().getEgg(id);

      if (egg.isPresent()) {
        Location location = egg.get().getLocation().toLocation();
        if (location == null) {
          player.sendMessage(
              language
                  .getSingleMessage("egg", "world_not_loaded")
                  .replace("{world}", egg.get().getLocation().world()));
          return;
        }
        // a second /ee tp during the 5 seconds keeps the first remembered mode
        if (player.getGameMode() != GameMode.SPECTATOR) {
          previousModes.put(player.getUniqueId(), player.getGameMode());
        }
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(location);
        createTempHighlight(Highlighters.create(egg.get(), player), player);
        return;
      }

      player.sendMessage(language.getSingleMessage("egg", "not_exist"));
      return;
    }

    player.sendMessage(language.getSingleMessage("category", "not_exist"));
  }

  private void createTempHighlight(Highlighter highlighter, Player player) {
    new BukkitRunnable() {
      @Override
      public void run() {
        highlighter.clear();
        GameMode previous = previousModes.remove(player.getUniqueId());
        if (previous != null && player.isOnline()) player.setGameMode(previous);
      }
    }.runTaskLater(plugin, (long) 20 * 5);
  }

  // the mode is saved with the player data at quit, so it is restored here and not after the delay
  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    GameMode previous = previousModes.remove(event.getPlayer().getUniqueId());
    if (previous != null) {
      event.getPlayer().setGameMode(previous);
    }
  }

  @Override
  public List<String> getTabComplete(String[] args) {
    if (args.length == 2) {
      return eggService.getCategoryNames();
    } else return null;
  }
}
