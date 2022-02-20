package ru.brainrtp.eastereggs.protocol.npcs.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

import java.util.Collections;

public class DestroyEntityPacket extends PacketContainer {

    public DestroyEntityPacket() {
        super(PacketType.Play.Server.ENTITY_DESTROY);
    }

    public DestroyEntityPacket setId(int id) {
        getIntLists().write(0, Collections.singletonList(id));
        return this;
    }
}