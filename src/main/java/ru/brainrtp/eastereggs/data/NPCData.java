package ru.brainrtp.eastereggs.data;

import api.logging.Logger;
import io.leangen.geantyref.TypeToken;
import lombok.Data;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.util.text.Colors;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

@Data
public class NPCData {
    // TODO: (12.02 18:40) Добавить всякие проверки для переменных как в аналогичных сериалайзерах

    private String name = "Steve";
    private Integer entityId;
    private final Location location;
    private Skin skin = new Skin();

    private static final String NAME_NODE = "name";
    private static final String LOCATION_NODE = "location";
    private static final String SKIN_NODE = "skin";
    private static final String ENTITY_ID_NODE = "entityId";

    public static class Serializer implements TypeSerializer<NPCData> {

        @Override
        public NPCData deserialize(Type type, ConfigurationNode node) throws SerializationException {

            NPCData npcData = new NPCData(node.node(LOCATION_NODE).get(Location.class));

            if (!node.node(NAME_NODE).isNull()) {
                npcData.setName(node.node(NAME_NODE).get(String.class));
            }

            if (!node.node(SKIN_NODE).isNull()) {
                npcData.setSkin(node.node(SKIN_NODE).get(TypeToken.get(Skin.class)));
//                или даже так:
//                npcData.setSkin(node.node(SKIN_NODE).get(Skin.class));
            }
            if (!node.node(ENTITY_ID_NODE).isNull()) {
                paramMustBeAnInteger(node, ENTITY_ID_NODE);
                npcData.setEntityId(node.node(ENTITY_ID_NODE).get(Integer.class));
            }

            return npcData;
        }

        @Override
        public void serialize(Type type, @Nullable NPCData npcData, ConfigurationNode node) throws SerializationException {
            node.node(NAME_NODE).set(npcData.getName());
            node.node(ENTITY_ID_NODE).set(npcData.getEntityId());
            node.node(LOCATION_NODE).set(TypeToken.get(Location.class), npcData.getLocation());
            node.node(SKIN_NODE).set(TypeToken.get(Skin.class), npcData.getSkin());
        }

        private void paramMustBeAnInteger(ConfigurationNode node, String param) {
            if (!(node.node(param).raw() instanceof Integer)) {
                String pathToWarn = Arrays.stream(node.path().array())
                        .map(Object::toString)
                        .collect(Collectors.joining("."))
                        .concat(".");

                String logMessage = """
                        &fThe &e''{0}''&r parameter must be an integer. Right now value is - &e''{1}''. &r
                        Setting the default value to &e0&r
                        Full path to this param - &e{2}&r""";
                Logger.warn(Colors.of(logMessage), param, node.node(param).raw(), pathToWarn + param);
            }
        }
    }

}
