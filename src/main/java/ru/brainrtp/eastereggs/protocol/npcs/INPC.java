package ru.brainrtp.eastereggs.protocol.npcs;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.data.Skin;

import java.util.UUID;

public interface INPC {

    Location getLocation();

    String getName();

    UUID getUUID();

    Skin getSkin();

    int getEntityId();

    void setSkin(Skin skin);

    void setLocation(Location location);

    void spawn();

    void spawn(Player player);

    void destroy();

}