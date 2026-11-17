package ru.brainrtp.eastereggs.listeners;

import java.util.Optional;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.services.EasterEggService;

public class DamageListener implements Listener {

  private final EasterEggService service;

  public DamageListener() {
    service = EasterEggs.getEggService();
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    Optional<EasterEgg> egg = service.getEasterEgg(event.getEntity());

    if (egg.isPresent()) {
      event.setDamage(0);
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onFrameBreak(HangingBreakByEntityEvent event) {
    boolean cancel = execute(event.getRemover(), event.getEntity());
    if (cancel) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onDamageEntityByPlayer(EntityDamageByEntityEvent event) {
    boolean cancel = execute(event.getDamager(), event.getEntity());
    if (cancel) {
      event.setCancelled(true);
    }
  }

  private boolean execute(Entity damager, Entity target) {
    if (!(damager instanceof Player player) || player.getGameMode().equals(GameMode.SPECTATOR)) {
      return false;
    }

    Optional<EasterEgg> egg = service.getEasterEgg(target);
    if (service.getEditor().isEditMode(player.getUniqueId())) {
      service.deleteEggAt(player, egg);
      return true;
    }
    return service.findEgg(player, egg);
  }
}
