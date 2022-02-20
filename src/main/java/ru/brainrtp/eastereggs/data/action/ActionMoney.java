package ru.brainrtp.eastereggs.data.action;

import api.logging.Logger;
import lombok.Data;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.util.text.Colors;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

@Data
public class ActionMoney implements Action {

    private static final String ACTION_NAME = "money";
    private float money = 0.0F;
    private static final String MONEY_NODE = "money";


    @Override
    public String getActionTitle() {
        return ACTION_NAME;
    }

    @Override
    public void activate(Player player) {
        // TODO: 28.11.2021 Добавить действие
        EasterEggs.getEconomy().depositPlayer(player, money);
    }

    public static class Serializer implements TypeSerializer<ActionMoney> {
        @Override
        public ActionMoney deserialize(Type type, ConfigurationNode node) throws SerializationException {
            ActionMoney actionMoney = new ActionMoney();
            if (!node.isNull()) {
                paramMustBeAnFloat(node, MONEY_NODE);
                actionMoney.setMoney(node.getFloat());
            }
            return actionMoney;
        }

        @Override
        public void serialize(Type type, @Nullable ActionMoney actionMoney, ConfigurationNode node) throws SerializationException {
            node.node(MONEY_NODE).set(actionMoney.getMoney());
        }

        private void paramMustBeAnFloat(ConfigurationNode node, String param) {
            try {
                Float.parseFloat(String.valueOf(node.raw()));
            } catch (NumberFormatException exception) {
                String pathToWarn = Arrays.stream(node.path().array())
                        .map(Object::toString)
                        .collect(Collectors.joining("."))
                        .concat(".");

                String logMessage = """
                        &fThe &e''{0}''&r parameter must be an float. Right now value is - &e''{1}''. &r
                        Setting the default value to &e0.0&r
                        Full path to this param - &e{2}&r""";
                Logger.warn(Colors.of(logMessage), param, node.raw(), pathToWarn);
            }
        }
    }

}
