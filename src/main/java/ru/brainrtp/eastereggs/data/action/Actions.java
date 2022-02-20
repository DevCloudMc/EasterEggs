package ru.brainrtp.eastereggs.data.action;

import io.leangen.geantyref.TypeToken;
import lombok.Data;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.providers.TokensProvider;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class Actions implements Action {

    public static final String ACTIONS_NODE = "actions";

    private final List<Action> localActions = new ArrayList<>();

    public void addAction(Action action) {
        localActions.add(action);
    }

    public void removeAction(String actionName) {
        Optional<Action> removeAction = localActions.stream()
                .filter(action -> actionName.equals(action.getActionTitle()))
                .findFirst();
        removeAction.ifPresent(localActions::remove);
    }

    @Override
    public String getActionTitle() {
        return null;
    }

    @Override
    public void activate(Player player) {
        if (!localActions.isEmpty()) {
            localActions.forEach(action -> action.activate(player));
        }
    }

    public static class Serializer implements TypeSerializer<Actions> {

        @Override
        public Actions deserialize(Type type, ConfigurationNode node) throws SerializationException {

            Actions actions = new Actions();

            for (ConfigurationNode value : node.childrenMap().values()) {
                TypeToken<? extends Action> typeToken = TokensProvider.getActionType((String) value.key());
                Action action = value.get(typeToken);
                actions.addAction(action);
            }

            return actions;
        }

        @Override
        public void serialize(Type type, @Nullable Actions actions, ConfigurationNode node) throws SerializationException {
            for (Action action : actions.getLocalActions()) {
                node.node(action.getActionTitle()).set(action);
            }

        }
    }
}
