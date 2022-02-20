package ru.brainrtp.eastereggs.protocol.hologram;


import api.logging.Logger;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.protocol.npcs.packets.DestroyEntityPacket;
import ru.brainrtp.eastereggs.protocol.npcs.packets.EntityMetadataPacket;
import ru.brainrtp.eastereggs.protocol.npcs.packets.SpawnEntityPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Hologram implements IHologram {

    private final int id;
    private String text;
    private final List<PacketContainer> packetContainerList = new ArrayList<>();
    private static final ProtocolManager connection = ProtocolLibrary.getProtocolManager();


    @Override
    public int getEntityID() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    public Hologram(Location location) {
        SpawnEntityPacket framePacket = new SpawnEntityPacket();
        id = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        WrappedDataWatcher.Registry.get(Byte.class);
        framePacket.setId(id)
                .setEntityType(EntityType.ARMOR_STAND)
                .setPosition(location.getX(), location.getY() - 2, location.getZ())
                .setRotation(0, 0)
                .setData(3);
        packetContainerList.add(framePacket);
    }

    @Override
    public void setText(String text) {


        this.text = text;
        EntityMetadataPacket entityMetadataPacket = new EntityMetadataPacket()
                .setId(id)
                .setInvisible(true)
                .setName(text)
                .build();
        packetContainerList.add(entityMetadataPacket);
    }

    @Override
    public void show(Player player) {
        packetContainerList.forEach(packetContainer -> tryToSendPacket(player, packetContainer));
    }

    @Override
    public void destroy() {
        DestroyEntityPacket destroyEntityPacket = new DestroyEntityPacket();
        destroyEntityPacket.setId(id);
        for (Player player : Bukkit.getOnlinePlayers()) {
            tryToSendPacket(player, destroyEntityPacket);
        }
    }

    protected static void tryToSendPacket(Player player, PacketContainer packet) {
        try {
            connection.sendServerPacket(player, packet);
        } catch (IllegalStateException e) {
        } catch (Exception e) {
            Logger.error("Failed to send FakeEntity packet {0}", e);
        }
    }

}