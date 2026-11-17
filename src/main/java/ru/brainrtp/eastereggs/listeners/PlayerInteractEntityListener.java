package ru.brainrtp.eastereggs.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.data.eggs.EggEntity;
import ru.brainrtp.eastereggs.services.EasterEggService;

public class PlayerInteractEntityListener implements Listener {

  private final EasterEggService service;

  public PlayerInteractEntityListener() {
    service = EasterEggs.getEggService();
  }

  @EventHandler
  public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
    if (event.getRightClicked() instanceof Hanging) {
      return;
    }

    if (event.getRightClicked() instanceof HumanEntity) {
      return;
    }

    boolean result = executeEvent(event);
    if (result) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onInteractEntity(PlayerInteractEntityEvent event) {
    boolean result = executeEvent(event);
    if (result) {
      event.setCancelled(true);
    }
  }

  private boolean executeEvent(PlayerInteractEntityEvent event) {
    Player player = event.getPlayer();
    Entity target = event.getRightClicked();
    if (event.getHand() == EquipmentSlot.OFF_HAND) {
      return false;
    }

    if (player.getGameMode() == GameMode.SPECTATOR) {
      return false;
    }

    if (service.getEditor().isEditMode(player.getUniqueId())) {
      // Right click, create entity egg
      EggEntity egg = new EggEntity();
      egg.setEntityLocation(target.getLocation());
      egg.setEntityUUID(target.getUniqueId());
      service.createEgg(player, service.getEditor().getEditCategory(player.getUniqueId()), egg);
      return true;
    }

    return service.findEgg(player, service.getEasterEgg(target));
  }
}
