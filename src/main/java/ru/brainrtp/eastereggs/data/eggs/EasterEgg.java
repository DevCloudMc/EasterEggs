package ru.brainrtp.eastereggs.data.eggs;

import io.leangen.geantyref.TypeToken;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.configuration.Configuration;
import ru.brainrtp.eastereggs.data.EggTypes;
import ru.brainrtp.eastereggs.data.NamedLocation;
import ru.brainrtp.eastereggs.data.action.Action;
import ru.brainrtp.eastereggs.data.action.Actions;
import ru.brainrtp.eastereggs.services.EasterEggService;
import ru.brainrtp.eastereggs.services.PlayerEggsService;
import ru.brainrtp.eastereggs.util.text.Colors;

// same identity as EggKey in storage
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
public abstract class EasterEgg {

  @EqualsAndHashCode.Include private int id;

  @EqualsAndHashCode.Include private String category;

  private EggTypes type;

  private NamedLocation location;

  private Actions actions;

  private static final String ID_NODE = "id";
  private static final String TYPE_NODE = "type";
  private static final String LOCATION_NODE = "location";
  private static final String ACTIONS_NODE = "actions";

  public void addAction(Action action) {
    if (actions == null) {
      actions = new Actions();
    }
    // actions are saved under their type name, so a second one of a type would overwrite the first
    actions.removeAction(action.getActionTitle());
    actions.addAction(action);
  }

  public void removeAction(String actionName) {
    if (actions == null) {
      actions = new Actions();
    }
    actions.removeAction(actionName);
  }

  public void activate(Player player) {
    if (!player.hasPermission("ee.type." + category)) {
      return;
    }

    EasterEggService service = EasterEggs.getEggService();
    PlayerEggsService players = service.getPlayerService();
    UUID uuid = player.getUniqueId();
    // decided at the click: with several saves pending, only the last missing egg may finish
    boolean last =
        service
            .getCategory(category)
            .filter(c -> players.countFound(uuid, c) == c.getEggs().size() - 1)
            .isPresent();
    // runs on the main thread after the find is saved, never for a failed save
    players.addEggToPlayer(
        uuid,
        this,
        () -> {
          if (!player.isOnline()) {
            return;
          }
          if (actions != null) {
            actions.activate(player);
          }
          if (last) {
            service.finishIfComplete(player, category);
          }
        });
  }

  static void deserializeCommon(EasterEgg egg, ConfigurationNode node)
      throws SerializationException {
    paramMustBeExist(node, ID_NODE);
    egg.setId(node.node(ID_NODE).getInt());
    egg.setLocation(node.node(LOCATION_NODE).require(NamedLocation.class));
    egg.setActions(node.node(ACTIONS_NODE).get(TypeToken.get(Actions.class)));
  }

  static void serializeCommon(EasterEgg egg, ConfigurationNode node) throws SerializationException {
    // TODO: (21.02 15:10) Add additional checks
    node.node(ID_NODE).set(egg.getId());
    node.node(TYPE_NODE).set(egg.getType().toString());
    node.node(LOCATION_NODE).set(NamedLocation.class, egg.getLocation());
    node.node(ACTIONS_NODE).set(TypeToken.get(Actions.class), egg.getActions());
  }

  private static void paramMustBeExist(ConfigurationNode node, String param) {
    if (node.node(param).isNull()) {
      String logMessage =
          """
          &fThe &e'{}'&r parameter must be exist.
          Full path to this param - &e{}&r\
          """;
      EasterEggs.getPlugin()
          .getSLF4JLogger()
          .warn(Colors.of(logMessage), param, Configuration.path(node) + "." + param);
    }
  }
}
