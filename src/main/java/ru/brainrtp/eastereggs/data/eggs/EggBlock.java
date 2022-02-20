package ru.brainrtp.eastereggs.data.eggs;

import api.logging.Logger;
import io.leangen.geantyref.TypeToken;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.data.EggTypes;
import ru.brainrtp.eastereggs.data.action.Actions;
import ru.brainrtp.eastereggs.util.text.Colors;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

// TODO: (20.02 0:20) check this annotation work
@EqualsAndHashCode(callSuper = false)
@Data
public class EggBlock extends EasterEgg {


    private static final String ID_NODE = "id";
    private static final String TYPE_NODE = "type";
    private static final String LOCATION_NODE = "location";
    private static final String ACTIONS_NODE = "actions";

    public EggBlock() {
        setType(EggTypes.BLOCK);
    }

    public void setBlockLocation(Location location) {
        setLocation(location);
    }

    public static class Serializer implements TypeSerializer<EggBlock> {

        @Override
        public EggBlock deserialize(Type type, ConfigurationNode node) throws SerializationException {
            EggBlock eggBlock = new EggBlock();
            paramMustBeExist(node, ID_NODE);
            eggBlock.setId(node.node(ID_NODE).getInt());

            paramMustBeExist(node, LOCATION_NODE);
            eggBlock.setLocation(node.node(LOCATION_NODE).get(TypeToken.get(Location.class)));
            eggBlock.setActions(node.node(ACTIONS_NODE).get(TypeToken.get(Actions.class)));

            return eggBlock;
        }

        @Override
        public void serialize(Type type, @Nullable EggBlock eggBlock, ConfigurationNode node) throws SerializationException {
            // TODO: (13.02 17:24) Дописать.

            node.node(ID_NODE).set(eggBlock.getId());
            node.node(TYPE_NODE).set(eggBlock.getType().toString());
            node.node(LOCATION_NODE).set(TypeToken.get(Location.class), eggBlock.getLocation());
            node.node(ACTIONS_NODE).set(TypeToken.get(Actions.class), eggBlock.getActions());
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
}