package ru.brainrtp.eastereggs.protocol.npcs.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Rotation;
import ru.brainrtp.eastereggs.util.text.Colors;

import java.util.Optional;

public class EntityMetadataPacket extends PacketContainer {
    private static final int ITEM_INDEX = 8;
    private static final int ROTATION_INDEX;
    private final WrappedDataWatcher dataWatcher = new WrappedDataWatcher();

    static {
        ROTATION_INDEX = ITEM_INDEX + 1;
    }

    public EntityMetadataPacket() {
        super(PacketType.Play.Server.ENTITY_METADATA);
    }

    public EntityMetadataPacket setId(int id) {
        getIntegers().write(0, id);
        return this;
    }

    public EntityMetadataPacket setFlags(byte flags) {
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
        dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, serializer), flags);
        return this;
    }

    public EntityMetadataPacket setInvisible(boolean invisible) {
        int flags = invisible ? 0x20 : 0x00;
        return setFlags((byte) flags);
    }

    public EntityMetadataPacket setName(String name) {

        WrappedDataWatcher.Serializer chatSerializer = WrappedDataWatcher.Registry.getChatComponentSerializer(true);
        WrappedDataWatcher.Serializer booleanSerializer = WrappedDataWatcher.Registry.get(Boolean.class);
//        Class<?> chatComponentClass = MinecraftReflection.getChatComponentTextClass();
        Optional<?> opt = Optional
                .of(WrappedChatComponent
                        .fromChatMessage(Colors.of(name))[0].getHandle());
//        Object chatComponent = null;
//        try {
//            chatComponent = chatComponentClass.getConstructor(String.class).newInstance("asd");
//        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//            e.printStackTrace();
//        }
        dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, chatSerializer), opt);
        dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, booleanSerializer), true);
        return this;
    }

    public EntityMetadataPacket setRotation(Rotation rotation) {
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Integer.class);
        dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(ROTATION_INDEX, serializer), rotation.ordinal());
        return this;
    }

    public EntityMetadataPacket build() {
        getWatchableCollectionModifier().write(0, dataWatcher.getWatchableObjects());
        return this;
    }

//    private WrappedChatComponent getChatComponent(String text) {
//        return WrappedChatComponent.fromJson("{\"text\": \"" + text + "\"}");
//    }
}