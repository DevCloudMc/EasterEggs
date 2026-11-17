package ru.brainrtp.eastereggs.listeners;

import api.logging.Logger;
import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.services.PlayerEggsService;

public class PlayerJoinListener implements Listener {

  private final PlayerEggsService playerService = EasterEggs.getEggService().getPlayerService();

  // blocks this async login thread until the finds are loaded, before the player can click an egg
  @EventHandler(priority = EventPriority.MONITOR)
  public void onLogin(AsyncPlayerPreLoginEvent e) {
    // counted before the result check: Paper fires the close event for denied logins too
    playerService.addConnection(e.getUniqueId());
    if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
      return;
    }
    try {
      playerService.loadPlayerData(e.getUniqueId());
    } catch (Exception ex) {
      Logger.error("Could not load eggs of {0}, login denied", ex, e.getName());
      e.disallow(
          AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
          Component.text("Could not load your EasterEggs data, please try again later"));
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    EasterEggs.getEggService().getEditor().endEditSession(player.getUniqueId());
  }

  // also fires for logins refused after pre-login (ban, whitelist, full server), unlike quit
  @EventHandler
  public void onConnectionClose(PlayerConnectionCloseEvent event) {
    playerService.removePlayerData(event.getPlayerUniqueId());
  }
}
