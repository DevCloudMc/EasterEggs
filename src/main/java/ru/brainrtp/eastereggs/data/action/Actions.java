package ru.brainrtp.eastereggs.data.action;

import api.logging.Logger;
import io.leangen.geantyref.TypeToken;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.providers.TokensProvider;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;

public class Actions implements Iterable<Action> {

    public static final String FIELD_ACTIONS = "actions";

    private List<Action> localActions;
    private Actions actions;

    public void add(Action action) {
        if (localActions == null) {
            localActions = new ArrayList<>();
        }
        localActions.add(action);
    }

    @Override
    public Iterator<Action> iterator() {
        return localActions.iterator();
    }

    @Override
    public void forEach(Consumer<? super Action> consumer) {
        localActions.forEach(consumer);
    }

    @Override
    public Spliterator<Action> spliterator() {
        return localActions.spliterator();
    }

    public void setActions(Actions actions) {
        this.actions = actions;
    }

    //todo: под вопросом надобность сие метода
    public static boolean isReserved(String key) {
        return key.equals(FIELD_ACTIONS);
    }

    public void activate(Player player) {
        if (localActions != null) {
            for (Action action : localActions) {
                action.activate(player);
            }
        }

        if (actions != null) actions.activate(player);
    }

    public static class Serializer implements TypeSerializer<Actions> {
//        @Override
//        public BossBar deserialize(Type type, ConfigurationNode node) throws SerializationException {
//            BossBar bossBar = new BossBar();
//
//            bossBar.setText(Colors.of(node.node("text").getString("")));
//            bossBar.setHealth(node.node("health").getFloat());
//
//            if (bossBar.getHealth() < 0 || bossBar.getHealth() > 1)
//                throw new SerializationException("BossBar health value must be between 0.0 and 1.0");
//
//            try {
//                bossBar.setColor(Color.valueOf(node.node("color").getString("").toUpperCase()));
//            } catch (IllegalArgumentException e) {
//                throw new SerializationException("Invalid bossbar color");
//            }
//
//            try {
//                bossBar.setDivision(Division.valueOf(node.node("division").getString("").toUpperCase()));
//            } catch (IllegalArgumentException e) {
//                throw new SerializationException("Invalid bossbar division");
//            }
//
//            return bossBar;
//        }

        @Override
        public Actions deserialize(Type type, ConfigurationNode node) throws SerializationException {

            Map<Object, ? extends ConfigurationNode> activators = node.childrenMap();
            Actions actions = new Actions();

            for (Map.Entry<Object, ? extends ConfigurationNode> entry : activators.entrySet()) {
                String key = entry.getKey().toString();

                if (Actions.isReserved(key)) continue;

                TypeToken<? extends Action> type2 = TokensProvider.getActionType(key);

                if (type != null) {
                    actions.add(entry.getValue().get(type2));
                } else {
                    Logger.error("Error while serializing action " + key);
                }
            }

            if (!node.node(FIELD_ACTIONS).isNull()) {
                actions.setActions(node.node(FIELD_ACTIONS).get(TypeToken.get(Actions.class)));
//                actions.setActions((Actions) node.node(FIELD_ACTIONS).get((Type) TypeToken.get(Actions.class)));
            }

            return actions;
        }

        @Override
        public void serialize(Type type, @Nullable Actions obj, ConfigurationNode node) throws SerializationException {

        }
    }
}
