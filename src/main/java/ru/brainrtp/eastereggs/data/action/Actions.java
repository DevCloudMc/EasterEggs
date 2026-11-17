package ru.brainrtp.eastereggs.data.action;

import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.providers.TokensProvider;

@Data
public class Actions {

  private final List<Action> localActions = new ArrayList<>();

  public void addAction(Action action) {
    localActions.add(action);
  }

  public void removeAction(String actionName) {
    Optional<Action> removeAction =
        localActions.stream()
            .filter(action -> actionName.equals(action.getActionTitle()))
            .findFirst();
    removeAction.ifPresent(localActions::remove);
  }

  public void activate(Player player) {
    for (Action action : localActions) {
      try {
        action.activate(player);
      } catch (Exception e) {
        EasterEggs.getPlugin()
            .getSLF4JLogger()
            .error("Egg action failed, the next actions still run", e);
      }
    }
  }

  public static class Serializer implements TypeSerializer<Actions> {

    @Override
    public Actions deserialize(Type type, ConfigurationNode node) throws SerializationException {

      Actions actions = new Actions();

      for (ConfigurationNode value : node.childrenMap().values()) {
        TypeToken<? extends Action> typeToken =
            TokensProvider.getActionType(String.valueOf(value.key()));
        if (typeToken == null) {
          EasterEggs.getPlugin()
              .getSLF4JLogger()
              .warn("Unknown action {} skipped, path {}", value.key(), value.path());
          continue;
        }
        actions.addAction(value.get(typeToken));
      }

      return actions;
    }

    @Override
    public void serialize(Type type, @Nullable Actions actions, ConfigurationNode node)
        throws SerializationException {
      for (Action action : actions.getLocalActions()) {
        node.node(action.getActionTitle()).set(action);
      }
    }
  }
}
