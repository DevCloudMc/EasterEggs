package ru.brainrtp.eastereggs.data.action;

import api.logging.Logger;
import io.leangen.geantyref.TypeToken;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.util.text.Colors;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

@Data
public class ActionSound implements Action {

    private static final String ACTION_NAME = "sound";
    // TODO: 26.11.2021 Add another option to make `sound: <Type>`, not just `sound { <...> }`
    private Sound sound;
    private Location location;
    private float pitch = 1.0F;
    private float volume = 1.0F;

    private static final String TYPE_NODE = "type";
    private static final String PITCH_NODE = "pitch";
    private static final String VOLUME_NODE = "volume";
    private static final String LOCATION_NODE = "location";

    @Override
    public String getActionTitle() {
        return ACTION_NAME;
    }

    @Override
    public void activate(Player player) {
        Location newLocation = this.location == null ? player.getLocation() : this.location;
        player.playSound(newLocation, sound, volume, pitch);
    }

    public static class Serializer implements TypeSerializer<ActionSound> {
        @Override
        public ActionSound deserialize(Type type, ConfigurationNode node) throws SerializationException {
            ActionSound actionSound = new ActionSound();

            if (!node.isMap()) {
                try {
                    actionSound.setSound(Sound.valueOf(node.getString()));
                } catch (IllegalArgumentException e) {
                    String pathToWarn = Arrays.stream(node.path().array())
                            .map(Object::toString)
                            .collect(Collectors.joining("."));

                    soundNotFound(node, pathToWarn);
                }

            } else if (!node.node(TYPE_NODE).isNull()) {
                try {
                    actionSound.setSound(Sound.valueOf(node.node(TYPE_NODE).getString()));
                } catch (IllegalArgumentException e) {
                    String pathToWarn = Arrays.stream(node.path().array())
                            .map(Object::toString)
                            .collect(Collectors.joining("."));
                    soundNotFound(node.node(TYPE_NODE), pathToWarn + ".type");
                }
            }

            if (!node.node(PITCH_NODE).isNull()) {
                paramMustBeAnFloatOrInteger(node, PITCH_NODE);
                actionSound.setPitch(node.node(PITCH_NODE).getFloat());
            }

            if (!node.node(VOLUME_NODE).isNull()) {
                paramMustBeAnFloatOrInteger(node, VOLUME_NODE);
                actionSound.setVolume(node.node(VOLUME_NODE).getFloat());
            }

            if (!node.node(LOCATION_NODE).isNull()) {
                actionSound.setLocation(node.node(LOCATION_NODE).get(Location.class));
            }

            return actionSound;
        }

        @Override
        public void serialize(Type type, @Nullable ActionSound actionSound, ConfigurationNode node) throws SerializationException {
            if (actionSound.getSound() != null)
                node.node(TYPE_NODE).set(actionSound.getSound().toString());
            if (actionSound.getLocation() != null)
                node.node(LOCATION_NODE).set(TypeToken.get(Location.class), actionSound.getLocation());

            node.node(PITCH_NODE).set(actionSound.getPitch());
            node.node(VOLUME_NODE).set(actionSound.getVolume());
        }

        private void paramMustBeAnFloatOrInteger(ConfigurationNode node, String param) {
            if (!(node.node(param).raw() instanceof Float) || !(node.node(param).raw() instanceof Integer)) {
                String pathToWarn = Arrays.stream(node.path().array())
                        .map(Object::toString)
                        .collect(Collectors.joining("."))
                        .concat(".");

                String logMessage = """
                        The &e''{0}''&r parameter must be an float or integer. Right now value is - &e''{1}''.&r
                        Setting the default value to &e1.0&r
                        Full path to this param - &e{2}&r""";
                Logger.warn(Colors.of(logMessage), param, node.node(param).raw(), pathToWarn + param);
            }
        }

        private void soundNotFound(ConfigurationNode node, String pathToWarnAndParam) throws SerializationException {
            throw new SerializationException(String.format("""
                            Can't found sound &e'%s'.&r
                            Full path to this param - &e%s&r
                            A list of available sounds can be found here:\s
                            https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html""",
                    node.getString(), pathToWarnAndParam));
        }
    }

}
