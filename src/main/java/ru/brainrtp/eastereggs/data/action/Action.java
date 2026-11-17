package ru.brainrtp.eastereggs.data.action;

import org.bukkit.entity.Player;

public interface Action {

  String getActionTitle();

  void activate(Player player);
}
