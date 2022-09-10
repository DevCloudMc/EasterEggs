package ru.brainrtp.eastereggs.util.text;

import org.bukkit.ChatColor;

import java.util.List;

public class Colors {

    public static String of(String line) {
        return ChatColor.translateAlternateColorCodes('&', line);
    }

    public static List<String> ofList(List<String> list) {
        list.replaceAll(Colors::of);

        return list;
    }

    public static String[] ofArr(String[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = of(array[i]);
        }
        return array;
    }

}
