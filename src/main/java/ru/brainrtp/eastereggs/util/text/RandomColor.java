package ru.brainrtp.eastereggs.util.text;

import org.bukkit.Color;

import java.util.Random;

public class RandomColor {

    private static final Random random = new Random();

    public static Color get() {
        return Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }

    public static Color[] getColors() {
        Color[] colors = new Color[1 + random.nextInt(5)];

        for (int i = 0; i < colors.length; i++) {
            colors[i] = get();
        }

        return colors;
    }
}
