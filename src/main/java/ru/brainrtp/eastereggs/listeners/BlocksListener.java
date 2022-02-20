package ru.brainrtp.eastereggs.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.services.EasterEggService;

public class BlocksListener implements Listener {

    private final EasterEggService service;

    public BlocksListener() {
        service = EasterEggs.getEggService();
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (service.getEditor().isEditMode(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBeak(BlockBreakEvent event) {
        if (service.getEditor().isEditMode(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
