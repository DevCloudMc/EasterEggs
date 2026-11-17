package ru.brainrtp.eastereggs.data.action;

import java.lang.reflect.Type;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.configuration.Configuration;
import ru.brainrtp.eastereggs.data.NamedLocation;
import ru.brainrtp.eastereggs.util.text.Colors;

@Data
public class ActionSound implements Action {

  private static final String ACTION_NAME = "sound";
  // TODO: 26.11.2021 Add another option to make `sound: <Type>`, not just `sound { <...> }`
  private Sound sound;
  private NamedLocation location;
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
    Location newLocation = this.location == null ? null : this.location.toLocation();
    player.playSound(
        newLocation == null ? player.getLocation() : newLocation, sound, volume, pitch);
  }

  public static class Serializer implements TypeSerializer<ActionSound> {
    @Override
    public ActionSound deserialize(Type type, ConfigurationNode node)
        throws SerializationException {
      ActionSound actionSound = new ActionSound();

      ConfigurationNode soundNode = node.isMap() ? node.node(TYPE_NODE) : node;
      if (!node.isMap() || !soundNode.isNull()) {
        try {
          actionSound.setSound(findSound(soundNode.getString()));
        } catch (IllegalArgumentException e) {
          soundNotFound(soundNode, Configuration.path(soundNode));
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
        actionSound.setLocation(node.node(LOCATION_NODE).get(NamedLocation.class));
      }

      return actionSound;
    }

    @Override
    public void serialize(Type type, @Nullable ActionSound actionSound, ConfigurationNode node)
        throws SerializationException {
      if (actionSound.getSound() != null)
        node.node(TYPE_NODE).set(Registry.SOUNDS.getKey(actionSound.getSound()).value());
      if (actionSound.getLocation() != null)
        node.node(LOCATION_NODE).set(NamedLocation.class, actionSound.getLocation());

      node.node(PITCH_NODE).set(actionSound.getPitch());
      node.node(VOLUME_NODE).set(actionSound.getVolume());
    }

    // accepts "entity.player.levelup", "minecraft:entity.player.levelup" and the legacy enum name
    // ENTITY_PLAYER_LEVELUP
    private Sound findSound(String name) {
      NamespacedKey key = name == null ? null : NamespacedKey.fromString(name.toLowerCase());
      Sound sound = key == null ? null : Registry.SOUNDS.get(key);
      if (sound == null && name != null) {
        sound =
            Registry.SOUNDS.stream()
                .filter(
                    s -> Registry.SOUNDS.getKey(s).value().replace('.', '_').equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
      }
      if (sound == null) throw new IllegalArgumentException(name);
      return sound;
    }

    private void paramMustBeAnFloatOrInteger(ConfigurationNode node, String param) {
      if (!(node.node(param).raw() instanceof Number)) {
        String logMessage =
            """
            The &e'{}'&r parameter must be an float or integer. Right now value is - &e'{}'.&r
            Setting the default value to &e1.0&r
            Full path to this param - &e{}&r\
            """;
        EasterEggs.getPlugin()
            .getSLF4JLogger()
            .warn(
                Colors.of(logMessage),
                param,
                node.node(param).raw(),
                Configuration.path(node) + "." + param);
      }
    }

    private void soundNotFound(ConfigurationNode node, String pathToWarnAndParam)
        throws SerializationException {
      throw new SerializationException(
          String.format(
              """
              Can't found sound &e'%s'.&r
              Full path to this param - &e%s&r
              A list of available sounds can be found here:\s
              https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html\
              """,
              node.getString(), pathToWarnAndParam));
    }
  }
}
