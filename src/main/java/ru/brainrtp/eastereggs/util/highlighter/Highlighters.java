package ru.brainrtp.eastereggs.util.highlighter;

import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;

public class Highlighters {

  public static Highlighter create(EasterEgg easterEgg, Player player) {
    return new Highlighter(easterEgg, player);
  }
}
