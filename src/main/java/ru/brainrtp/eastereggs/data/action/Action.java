package ru.brainrtp.eastereggs.data.action;

import org.bukkit.entity.Player;

public interface Action {

    String getActionTitle();

    void activate(Player player);

    // TODO: (21.02 15:8) I think this functionality should be added.
//    void activate(Player player, Placeholder placeholder);
}
