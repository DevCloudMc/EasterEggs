package ru.brainrtp.eastereggs.data.action;

import org.bukkit.entity.Player;

public interface Action {

    String getActionTitle();

    void activate(Player player);

    // TODO: 26.11.2021 реализовать
//    void activate(Player player, Placeholder placeholder);
}
