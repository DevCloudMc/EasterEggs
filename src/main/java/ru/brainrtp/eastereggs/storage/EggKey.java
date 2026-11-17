package ru.brainrtp.eastereggs.storage;

import ru.brainrtp.eastereggs.data.eggs.EasterEgg;

public record EggKey(String category, int id) {

  public static EggKey of(EasterEgg egg) {
    return new EggKey(egg.getCategory(), egg.getId());
  }
}
