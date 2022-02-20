package ru.brainrtp.eastereggs.util.highlighter.blockhighlight;

import lombok.Data;
import org.bukkit.Location;

@Data
public class BlockHighlight {

    private final int x;
    private final int y;
    private final int z;
    private final int color;
    private final String text;
    private final int time;

    public BlockHighlight(Location location, int rgbColor, String text, int timeAlive) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.color = rgbColor;
        this.text = text;
        this.time = timeAlive;
    }
}
