package ru.brainrtp.eastereggs.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.data.eggs.EggBlock;
import ru.brainrtp.eastereggs.services.EasterEggService;

public class PlayerInteractListener implements Listener {

  private final EasterEggService service;

  public PlayerInteractListener() {
    service = EasterEggs.getEggService();
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {

    Player player = event.getPlayer();

    if (player.getGameMode().equals(GameMode.SPECTATOR)) {
      return;
    }

    boolean editMode = service.getEditor().isEditMode(player.getUniqueId());
    if (editMode) {
      event.setCancelled(true);
    }

    Action action = event.getAction();
    if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
      return;
    }
    if (event.getHand() == EquipmentSlot.OFF_HAND) {
      return;
    }

    if (!editMode) {
      // Activate egg
      if (service.findEgg(player, service.getEasterEgg(event.getClickedBlock().getLocation()))) {
        event.setCancelled(true);
      }
    } else if (action == Action.RIGHT_CLICK_BLOCK) {
      // Create block egg
      EggBlock egg = new EggBlock();
      egg.setBlockLocation(event.getClickedBlock().getLocation());
      service.createEgg(player, service.getEditor().getEditCategory(player.getUniqueId()), egg);
    } else {
      // Remove block egg
      service.deleteEggAt(player, service.getEasterEgg(event.getClickedBlock().getLocation()));
    }
  }
}
