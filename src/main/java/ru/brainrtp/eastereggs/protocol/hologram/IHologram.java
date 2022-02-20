package ru.brainrtp.eastereggs.protocol.hologram;

import org.bukkit.entity.Player;

public interface IHologram {

    int getEntityID();

    String getText();

    void setText(String text);

    void show(Player player);

    void destroy();
}
