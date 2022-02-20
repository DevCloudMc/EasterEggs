package ru.brainrtp.eastereggs.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.brainrtp.eastereggs.EasterEggs;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        EasterEggs.getEggService().getPlayerService().loadPlayerData(e.getUniqueId());
    }
    // TODO: (19.02 21:11) NPC never use :)
//
//    @EventHandler
//    public void onJoin(PlayerJoinEvent event){
//        Player player = event.getPlayer();
//        player.setRemoveWhenFarAway(false);
//
//        Collection<INPC> npcs = EasterEggs.getNpcService().getAllNPC();
//
//        for(INPC npc : npcs){
//            npc.spawn(player);
//        }
//    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        EasterEggs.getEggService().getEditor().endEditSession(player.getUniqueId());
        EasterEggs.getEggService().getPlayerService().removePlayerData(player.getUniqueId());
    }
}
