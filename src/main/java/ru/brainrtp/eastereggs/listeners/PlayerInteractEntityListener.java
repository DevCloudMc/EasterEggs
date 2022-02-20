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
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.data.eggs.EggEntity;
import ru.brainrtp.eastereggs.services.EasterEggService;

import java.util.Optional;

public class PlayerInteractEntityListener implements Listener {

    private final Language language;
    private final EasterEggService service;

    public PlayerInteractEntityListener() {
        language = EasterEggs.getLanguage();
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
        try {
            if (event.getHand() == EquipmentSlot.OFF_HAND) {
                return false;
            }
        } catch (NoSuchMethodError e) {

        }

        if (player.getGameMode() == GameMode.SPECTATOR) {
            return false;
        }

        if (service.getEditor().isEditMode(player.getUniqueId())) {
            // Right click, create entity egg
            String category = service.getEditor().getEditCategory(player.getUniqueId());
            Optional<EasterEgg> eggOpt = service.getEasterEgg(target.getLocation());

            if (!eggOpt.isPresent()) {
                EggEntity egg = new EggEntity();
                egg.setEntityLocation(target.getLocation());
                egg.setEntityType(target.getType());
                egg.setEntityUUID(target.getUniqueId());

                service.createNewEgg(category, egg);

                player.sendMessage(language.getSingleMessage("egg", "create", "success")
                        .replace("{egg}", String.valueOf(egg.getId())));
                return true;
            }

            player.sendMessage(language.getSingleMessage("egg", "create", "already"));
            return true;
        }

        // Activate egg
        Optional<EasterEgg> egg = service.getEasterEgg(target.getLocation());

        if (egg.isPresent()) {
            if (!service.getPlayerService().isCompletedEgg(player.getUniqueId(), egg.get())) {
                egg.get().activate(player);
                return true;
            }

            player.sendMessage(language.getSingleMessage("egg", "find", "already"));
            return true;
        }

        return false;
    }
}
