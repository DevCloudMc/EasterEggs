package ru.brainrtp.eastereggs.data.placeholders;

import java.util.HashMap;
import java.util.Map;

public class Placeholder {

    public static final String PLAYER = "%player%";
    public static final String CATEGORY = "%category%";
    public static final String FOUNDED_EGG = "%found%";
    public static final String EGG_COUNT = "%count%";

    private final Map<String, Object> placeholders = new HashMap<>();

    public void addData(String placeholder, Object data) {
        placeholders.put(placeholder, data);
    }

    public Object get(String placeholder) {
        return placeholders.get(placeholder);
    }

    public String replace(String str) {
        String result = str;

        if (placeholders.get(PLAYER) != null) {
            result = result.replace(PLAYER, placeholders.get(PLAYER).toString());
        }

        if (placeholders.get(CATEGORY) != null) {
            result = result.replace(CATEGORY, placeholders.get(CATEGORY).toString());
        }

        if (placeholders.get(FOUNDED_EGG) != null) {
            result = result.replace(FOUNDED_EGG, placeholders.get(FOUNDED_EGG).toString());
        }

        if (placeholders.get(EGG_COUNT) != null) {
            result = result.replace(EGG_COUNT, placeholders.get(EGG_COUNT).toString());
        }

        return result;
    }
}
