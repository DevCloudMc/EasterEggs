package ru.brainrtp.eastereggs.data.eggs;

import java.lang.reflect.Type;
import java.util.UUID;
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
public class EggEntity extends EasterEgg {

  private UUID entityUUID;

  private static final String ENTITY_UUID_NODE = "entityUUID";

  public EggEntity() {
    setType(EggTypes.ENTITY);
  }

  public void setEntityLocation(Location location) {
    setLocation(NamedLocation.of(location));
  }

  public static class Serializer implements TypeSerializer<EggEntity> {

    @Override
    public EggEntity deserialize(Type type, ConfigurationNode node) throws SerializationException {
      EggEntity eggEntity = new EggEntity();
      deserializeCommon(eggEntity, node);
      eggEntity.setEntityUUID(UUID.fromString(node.node(ENTITY_UUID_NODE).getString()));
      return eggEntity;
    }

    @Override
    public void serialize(Type type, @Nullable EggEntity eggEntity, ConfigurationNode node)
        throws SerializationException {
      serializeCommon(eggEntity, node);
      node.node(ENTITY_UUID_NODE).set(eggEntity.getEntityUUID().toString());
    }
  }
}
