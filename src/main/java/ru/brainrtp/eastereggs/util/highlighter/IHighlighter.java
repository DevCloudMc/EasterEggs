package ru.brainrtp.eastereggs.util.highlighter;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.data.eggs.EggEntity;

public interface IHighlighter {

    void startBlock(Player player);

    void startEntity(Location location, EggEntity easterEgg, Player player);

    void clear(Player player);
}
