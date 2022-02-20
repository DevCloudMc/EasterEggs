package ru.brainrtp.eastereggs.listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.services.EasterEggService;

import java.util.Optional;

public class DamageListener implements Listener {

    private final Language language;
    private final EasterEggService service;

    public DamageListener() {
        language = EasterEggs.getLanguage();
        service = EasterEggs.getEggService();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Optional<EasterEgg> egg = service.getEasterEgg(event.getEntity().getLocation());

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
        if (damager instanceof Player) {
            Player player = (Player) damager;

            if (player.getGameMode().equals(GameMode.SPECTATOR)) {
                return false;
            }

            if (service.getEditor().isEditMode(player.getUniqueId())) {
                // Delete egg
                String category = service.getEditor().getEditCategory(player.getUniqueId());
                Location location = target.getLocation();
                Optional<EasterEgg> egg = service.getCategory(category).get().getEggByLocation(location);

                if (egg.isPresent()) {
                    service.deleteEgg(category, egg.get());
                    player.sendMessage(language.getSingleMessage("egg", "delete", "success")
                            .replace("{egg}", String.valueOf(egg.get().getId()))
                            .replace("{category}", egg.get().getCategory()));
                }

                return true;
            }

            // Activate egg
            Optional<EasterEgg> egg = service.getEasterEgg(target.getLocation());

            if (egg.isPresent()) {
//                if(!service.getPlayerService().isCompletedEgg(player.getUniqueId(), egg.get().getCategory(), egg.get().getId())){
                if (!service.getPlayerService().isCompletedEgg(player.getUniqueId(), egg.get())) {
                    egg.get().activate(player);
                    return true;
                }

                player.sendMessage(language.getSingleMessage("egg", "find", "already"));
                return true;
            }
        }

        return false;
    }

}
