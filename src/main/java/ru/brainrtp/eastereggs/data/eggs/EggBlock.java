package ru.brainrtp.eastereggs.data.eggs;

import java.lang.reflect.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.data.EggTypes;
import ru.brainrtp.eastereggs.data.NamedLocation;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Data
public class EggBlock extends EasterEgg {

  public EggBlock() {
    setType(EggTypes.BLOCK);
  }

  public void setBlockLocation(Location location) {
    setLocation(NamedLocation.of(location));
  }

  public static class Serializer implements TypeSerializer<EggBlock> {

    @Override
    public EggBlock deserialize(Type type, ConfigurationNode node) throws SerializationException {
      EggBlock eggBlock = new EggBlock();
      deserializeCommon(eggBlock, node);
      return eggBlock;
    }

    @Override
    public void serialize(Type type, @Nullable EggBlock eggBlock, ConfigurationNode node)
        throws SerializationException {
      serializeCommon(eggBlock, node);
    }
  }
}
