package ru.brainrtp.eastereggs.data.eggs;

import api.logging.Logger;
import io.leangen.geantyref.TypeToken;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.data.EggTypes;
import ru.brainrtp.eastereggs.data.action.Actions;
import ru.brainrtp.eastereggs.util.text.Colors;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

// TODO: (20.02 0:20) check this annotation work
@EqualsAndHashCode(callSuper = false)
@Data
public class EggEntity extends EasterEgg {

    private EntityType entityType;
    private UUID entityUUID;

    private static final String ID_NODE = "id";
    private static final String TYPE_NODE = "type";
    private static final String LOCATION_NODE = "location";
    private static final String ACTIONS_NODE = "actions";
    private static final String ENTITY_UUID_NODE = "entityUUID";

    public EggEntity() {
        setType(EggTypes.ENTITY);
    }

    public void setEntityLocation(Location location) {
        setLocation(location);
    }

    public static class Serializer implements TypeSerializer<EggEntity> {


        @Override
        public EggEntity deserialize(Type type, ConfigurationNode node) throws SerializationException {
            EggEntity eggEntity = new EggEntity();
            paramMustBeExist(node, ID_NODE);
            eggEntity.setId(node.node(ID_NODE).getInt());

            paramMustBeExist(node, LOCATION_NODE);
            eggEntity.setLocation(node.node(LOCATION_NODE).get(Location.class));

            eggEntity.setActions(node.node(ACTIONS_NODE).get(Actions.class));
            eggEntity.setEntityUUID(UUID.fromString(node.node(ENTITY_UUID_NODE).getString()));
            return eggEntity;
        }

        @Override
        public void serialize(Type type, @Nullable EggEntity eggEntity, ConfigurationNode node) throws SerializationException {
            // TODO: (13.02 23:18) Сделать проверки на всякое) 
            node.node(ID_NODE).set(eggEntity.getId());
            node.node(TYPE_NODE).set(eggEntity.getType().toString());
            node.node(LOCATION_NODE).set(TypeToken.get(Location.class), eggEntity.getLocation());
            node.node(ACTIONS_NODE).set(TypeToken.get(Actions.class), eggEntity.getActions());
            node.node(ENTITY_UUID_NODE).set(eggEntity.getEntityUUID().toString());
        }

        private void paramMustBeExist(ConfigurationNode node, String param) {
            if (node.node(param).isNull()) {
                String pathToWarn = Arrays.stream(node.path().array())
                        .map(Object::toString)
                        .collect(Collectors.joining("."))
                        .concat(".");

                String logMessage = """
                        &fThe &e''{0}''&r parameter must be exist.
                        Full path to this param - &e{1}&r""";
                Logger.warn(Colors.of(logMessage), param, pathToWarn + param);
            }
        }
    }

    // TODO: (19.02 20:31) Нужен ли?
    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }
}
