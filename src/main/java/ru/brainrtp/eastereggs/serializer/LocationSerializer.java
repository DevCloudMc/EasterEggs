package ru.brainrtp.eastereggs.serializer;

import java.lang.reflect.Type;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.data.NamedLocation;

public class LocationSerializer implements TypeSerializer<NamedLocation> {

  @Override
  public NamedLocation deserialize(Type type, ConfigurationNode node)
      throws SerializationException {
    String value = node.getString("");
    String[] args = value.split(",");
    // older files may carry yaw and pitch after z, they are ignored
    if (args.length < 4) {
      throw badValue(node, type, value);
    }
    try {
      return new NamedLocation(
          args[0].trim(),
          Double.parseDouble(args[1].trim()),
          Double.parseDouble(args[2].trim()),
          Double.parseDouble(args[3].trim()));
    } catch (NumberFormatException e) {
      throw badValue(node, type, value);
    }
  }

  @Override
  public void serialize(Type type, @Nullable NamedLocation location, ConfigurationNode node)
      throws SerializationException {
    if (location == null) {
      node.raw(null);
      return;
    }
    node.set(location.world() + ", " + location.x() + ", " + location.y() + ", " + location.z());
  }

  private static SerializationException badValue(ConfigurationNode node, Type type, String value) {
    return new SerializationException(
        node, type, "expected \"world, x, y, z\", got \"" + value + "\"");
  }
}
