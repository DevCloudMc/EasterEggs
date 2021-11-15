package ru.brainrtp.eastereggs.data.eggs;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.data.action.Action;
import ru.brainrtp.eastereggs.data.action.Actions;
//import ru.nanit.eastereggs.EasterEggs;
//import ru.nanit.eastereggs.data.actions.Action;
//import ru.nanit.eastereggs.data.placeholders.Placeholder;

import java.util.*;

public abstract class EasterEgg {

    private int id;

    private String category;

    // TODO: 15.11.2021 заменить на Enum
    private String type;

    private Location location;

    private Actions actions;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

//    public void setAction(List<Actions> action){
//        actions.addAll((Collection<? extends Action>) action);
//    }

    public void addAction(Action action){
        actions.add(action);
    }

//    public void removeAction(String name){
//        actions.remove(name);
//    }

//    public void activate(Player player){
//        if(!player.hasPermission("ee.type." + category)){
//            return;
//        }
//
//        EasterEggs.getEasterEggService().addEggToPlayer(player, this);
//
//        int founded = EasterEggs.getEasterEggService().getPlayerService().getPlayerData(player.getUniqueId())
//                .get(category).getEggs().size();
//        int count = EasterEggs.getEasterEggService().getCategory(category).get().getEggs().size();
//
//        Placeholder ph = new Placeholder();
//        ph.addData(Placeholder.PLAYER, player.getName());
//        ph.addData(Placeholder.CATEGORY, category);
//        ph.addData(Placeholder.FOUNDED_EGG, founded);
//        ph.addData(Placeholder.EGG_COUNT, count);
//
//        for(Action p : actions.values()){
//            p.activate(player, ph);
//        }
//    }
}
