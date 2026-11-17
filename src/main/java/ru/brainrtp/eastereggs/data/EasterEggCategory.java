package ru.brainrtp.eastereggs.data;

import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.configuration.Configuration;
import ru.brainrtp.eastereggs.data.action.Actions;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.data.eggs.EggBlock;
import ru.brainrtp.eastereggs.data.eggs.EggEntity;
import ru.brainrtp.eastereggs.providers.TokensProvider;

@Data
public class EasterEggCategory {

  private String title;
  private String shortCategoryName;
  private String output;
  private Configuration config;
  private boolean hidePlayerData = false;

  // PlaceholderAPI reads it from its own thread
  private final Map<Integer, EasterEgg> eggs = new ConcurrentHashMap<>();
  private Actions finishAction;

  private static final String TITLE_NODE = "title";
  private static final String SHORT_CATEGORY_NAME_NODE = "category";
  private static final String CATEGORY_NODE = "category";
  private static final String TYPE_NODE = "type";
  // TODO: (19.02 20:34) The name of the node as an action does not say much
  //  This node is responsible for the text that will be displayed to the player when /ee is
  // executed (show eggs list)
  private static final String OUTPUT_NODE = "output";
  private static final String HIDE_PLAYER_DATA_NODE = "hidePlayerData";
  private static final String FINISH_ACTION_NODE = "finishAction";

  public EasterEggCategory(String name) {
    this.title = name;
    this.shortCategoryName = name;
  }

  public Optional<EasterEgg> getEgg(int id) {
    return Optional.ofNullable(eggs.get(id));
  }

  public void addEgg(EasterEgg egg) {
    egg.setCategory(shortCategoryName);
    // TODO: (19.02 20:32) Perhaps the default message should be set?
    eggs.put(egg.getId(), egg);
  }

  public void removeEgg(EasterEgg egg) {
    eggs.remove(egg.getId());
    save();
  }

  public void finish(Player player) {
    finishAction.activate(player);
    // TODO: (19.02 20:33) Finish it (for PAPI)
    // TODO: (21.02 15:10) I think this functionality (PAPI) should be added.
  }

  public void save() {
    try {
      // cleared first, otherwise removed eggs and actions stay in the node
      config.get().set(null);
      config.get().set(this);
    } catch (SerializationException e) {
      EasterEggs.getPlugin()
          .getSLF4JLogger()
          .error("Could not save category {}, the file is left as it was", shortCategoryName, e);
      return;
    }
    config.save();
  }

  public static class Serializer implements TypeSerializer<EasterEggCategory> {

    @Override
    public EasterEggCategory deserialize(Type type, ConfigurationNode node)
        throws SerializationException {
      EasterEggCategory category = new EasterEggCategory(node.node(TITLE_NODE).getString());

      category.setHidePlayerData(node.node(HIDE_PLAYER_DATA_NODE).getBoolean());
      category.setOutput(node.node(OUTPUT_NODE).getString());
      category.setShortCategoryName(node.node(SHORT_CATEGORY_NAME_NODE).getString());

      for (ConfigurationNode child : node.childrenMap().values()) {
        if (child.childrenMap().isEmpty() || child.key().equals(FINISH_ACTION_NODE)) {
          continue;
        }

        String typeStr = child.node(TYPE_NODE).getString();
        TypeToken<? extends EasterEgg> eggType = TokensProvider.getEggType(typeStr);
        if (eggType == null) {
          EasterEggs.getPlugin()
              .getSLF4JLogger()
              .warn("Unknown egg type {} skipped, path {}", typeStr, child.path());
          continue;
        }

        EasterEgg egg = child.get(eggType);
        egg.setCategory(node.node(CATEGORY_NODE).getString());

        category.addEgg(egg);
      }

      category.setTitle(node.node(TITLE_NODE).getString());
      category.setFinishAction(node.node(FINISH_ACTION_NODE).get(Actions.class, new Actions()));

      return category;
    }

    @Override
    public void serialize(
        Type type, @Nullable EasterEggCategory easterEggCategory, ConfigurationNode node)
        throws SerializationException {

      node.node(TITLE_NODE).set(easterEggCategory.getTitle());
      node.node(SHORT_CATEGORY_NAME_NODE).set(easterEggCategory.getShortCategoryName());
      node.node(OUTPUT_NODE).set(easterEggCategory.getOutput());
      node.node(HIDE_PLAYER_DATA_NODE).set(easterEggCategory.isHidePlayerData());

      for (EasterEgg easterEgg : easterEggCategory.getEggs().values()) {
        if (EggTypes.BLOCK.equals(easterEgg.getType())) {
          // TODO: (21.02 15:11) Should I add actions when creating a new Easter egg?
          node.node(easterEgg.getId()).set(TypeToken.get(EggBlock.class), (EggBlock) easterEgg);
        } else {
          node.node(easterEgg.getId()).set(TypeToken.get(EggEntity.class), (EggEntity) easterEgg);
        }
      }
      node.node(FINISH_ACTION_NODE)
          .set(TypeToken.get(Actions.class), easterEggCategory.getFinishAction());
    }
  }
}
