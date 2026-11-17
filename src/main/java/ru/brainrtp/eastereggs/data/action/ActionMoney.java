package ru.brainrtp.eastereggs.data.action;

import api.logging.Logger;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.Data;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.util.text.Colors;

@Data
public class ActionMoney implements Action {

  private static final String ACTION_NAME = "money";
  private static boolean noEconomyWarned;
  private float money = 0.0F;
  private static final String MONEY_NODE = "money";

  @Override
  public String getActionTitle() {
    return ACTION_NAME;
  }

  @Override
  public void activate(Player player) {
    if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
      warnNoEconomy();
      return;
    }
    RegisteredServiceProvider<Economy> registration =
        Bukkit.getServicesManager().getRegistration(Economy.class);
    if (registration == null) {
      warnNoEconomy();
      return;
    }
    registration.getProvider().depositPlayer(player, money);
  }

  private static void warnNoEconomy() {
    if (!noEconomyWarned) {
      noEconomyWarned = true;
      Logger.warn("Economy provider not found, money actions are skipped");
    }
  }

  public static class Serializer implements TypeSerializer<ActionMoney> {
    @Override
    public ActionMoney deserialize(Type type, ConfigurationNode node)
        throws SerializationException {
      ActionMoney actionMoney = new ActionMoney();
      // 3.0 saved the amount as money { money = N }
      ConfigurationNode amount = node.isMap() ? node.node(MONEY_NODE) : node;
      if (!amount.isNull()) {
        paramMustBeAnFloat(amount, MONEY_NODE);
        actionMoney.setMoney(amount.getFloat());
      }
      return actionMoney;
    }

    @Override
    public void serialize(Type type, @Nullable ActionMoney actionMoney, ConfigurationNode node)
        throws SerializationException {
      node.set(actionMoney.getMoney());
    }

    private void paramMustBeAnFloat(ConfigurationNode node, String param) {
      try {
        Float.parseFloat(String.valueOf(node.raw()));
      } catch (NumberFormatException exception) {
        String pathToWarn =
            Arrays.stream(node.path().array())
                .map(Object::toString)
                .collect(Collectors.joining("."))
                .concat(".");

        String logMessage =
            """
            &fThe &e''{0}''&r parameter must be an float. Right now value is - &e''{1}''. &r
            Setting the default value to &e0.0&r
            Full path to this param - &e{2}&r\
            """;
        Logger.warn(Colors.of(logMessage), param, node.raw(), pathToWarn);
      }
    }
  }
}
