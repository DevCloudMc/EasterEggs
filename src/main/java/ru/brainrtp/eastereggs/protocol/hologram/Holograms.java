package ru.brainrtp.eastereggs.protocol.hologram;

import org.bukkit.Location;

public class Holograms {

    public static Hologram create(Location location) {
        return new Hologram(location);
    }

}
