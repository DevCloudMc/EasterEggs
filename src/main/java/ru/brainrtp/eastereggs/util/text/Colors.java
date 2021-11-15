package ru.brainrtp.eastereggs.util.text;

import org.bukkit.ChatColor;

import java.util.List;

public class Colors {

    public static String of(String line){
        return ChatColor.translateAlternateColorCodes('&', line);
    }

    public static List<String> ofList(List<String> list){
        for(int i = 0; i < list.size(); i++){
            list.set(i, of(list.get(i)));
        }

        return list;
    }

    public static String[] ofArr(String[] array){
        for (int i = 0; i < array.length; i++){
            array[i] = of(array[i]);
        }
        return array;
    }

}
