package ru.brainrtp.eastereggs.util.text;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Colors {

  public static String of(String line) {
    return LegacyComponentSerializer.legacySection()
        .serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
  }

  public static Component component(String line) {
    return LegacyComponentSerializer.legacySection().deserialize(of(line));
  }

  public static List<String> ofList(List<String> list) {
    for (int i = 0; i < list.size(); i++) {
      list.set(i, of(list.get(i)));
    }

    return list;
  }

  public static String[] ofArr(String[] array) {
    for (int i = 0; i < array.length; i++) {
      array[i] = of(array[i]);
    }
    return array;
  }
}
