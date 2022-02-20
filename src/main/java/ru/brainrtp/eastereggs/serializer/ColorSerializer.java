package ru.brainrtp.eastereggs.serializer;

import org.bukkit.Color;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

public class ColorSerializer implements TypeSerializer<Color> {

    @Override
    public Color deserialize(Type type, ConfigurationNode node) throws SerializationException {
        return Color.fromRGB(Integer.parseInt(Objects.requireNonNull(node.getString()), 16));
    }

    @Override
    public void serialize(Type type, Color color, ConfigurationNode node) throws SerializationException {
        node.set(Integer.toHexString(Objects.requireNonNull(color).asRGB()));
    }
}
