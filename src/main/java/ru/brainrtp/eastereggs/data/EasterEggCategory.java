package ru.brainrtp.eastereggs.data;

import io.leangen.geantyref.TypeToken;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.configuration.Configuration;
import ru.brainrtp.eastereggs.data.action.Action;
import ru.brainrtp.eastereggs.data.action.Actions;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.data.eggs.EggBlock;
import ru.brainrtp.eastereggs.data.eggs.EggEntity;
import ru.brainrtp.eastereggs.providers.TokensProvider;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
public class EasterEggCategory {

    private String title;
    private String shortCategoryName;
    private String output;
    private Configuration config;
    private boolean hidePlayerData = false;

    private final Map<Integer, EasterEgg> eggs = new HashMap<>();
    private Actions finishAction;

    private static final String TITLE_NODE = "title";
    private static final String SHORT_CATEGORY_NAME_NODE = "category";
    private static final String CATEGORY_NODE = "category";
    private static final String TYPE_NODE = "type";
    // TODO: (19.02 20:34) The name of the node as an action does not say much
    //  This node is responsible for the text that will be displayed to the player when /ee is executed (show eggs list)
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

    public Optional<EasterEgg> getEggByLocation(Location loc) {
        for (EasterEgg egg : eggs.values()) {
            if (egg.getLocation().equals(loc)) {
                return Optional.of(egg);
            }
        }

        return Optional.empty();
    }

    public void addEgg(EasterEgg egg) {
        egg.setCategory(shortCategoryName);
        // TODO: (19.02 20:32) Perhaps the default message should be set?
//        ActionMessage messageAction = new ActionMessage();
//        messageAction.setMessages(List.of("Some message"));
//        egg.addAction(messageAction);
        eggs.put(egg.getId(), egg);

    }

    public void removeEgg(EasterEgg egg) {

        if (config != null) {
            eggs.remove(egg.getId());
            try {
                // TODO: (17.02 14:41) kludge :(
                config.get().set(null);
                config.get().set(this);
            } catch (SerializationException e) {
                e.printStackTrace();
            }
            config.save();
        }
    }

    public void finish(Player player) {
        finishAction.activate(player);
        // TODO: (19.02 20:33) Finish it (for PAPI)
        int founded = EasterEggs.getEggService().getPlayerService().getPlayerData(player.getUniqueId()).size();
        int count = eggs.size();

        // TODO: (1.02 0:24) реализовать
//        Placeholder ph = new Placeholder();
//        ph.addData(Placeholder.PLAYER, player.getName());
//        ph.addData(Placeholder.CATEGORY, name);
//        ph.addData(Placeholder.FOUNDED_EGG, founded);
//        ph.addData(Placeholder.EGG_COUNT, count);
//
//        for (Action action : finishAction) {
//            action.activate(player, ph);
//        }
    }

    public void clear() {
        eggs.clear();
    }

    public void save() {
        try {
            // TODO: (19.02 20:33) I think it's kludge :(
            config.get().set(null);
            config.get().set(this);
            config.save();
        } catch (SerializationException e) {
            e.printStackTrace();
        }
    }

    public static class Serializer implements TypeSerializer<EasterEggCategory> {

        @Override
        public EasterEggCategory deserialize(Type type, ConfigurationNode node) throws SerializationException {
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

                EasterEgg egg = child.get(eggType);
                egg.setCategory(node.node(CATEGORY_NODE).getString());

                category.addEgg(egg);
            }

            Actions actions = new Actions();

            for (ConfigurationNode child : node.node(FINISH_ACTION_NODE).childrenMap().values()) {
                String actionName = child.key().toString();
                TypeToken<? extends Action> actionType = TokensProvider.getActionType(actionName);

                Action action = child.get(actionType);

                actions.addAction(action);
            }

            category.setTitle(node.node(TITLE_NODE).getString());
            category.setFinishAction(actions);

            return category;
        }

        @Override
        public void serialize(Type type, @Nullable EasterEggCategory easterEggCategory, ConfigurationNode node) throws SerializationException {

            node.node(TITLE_NODE).set(easterEggCategory.getTitle());
            node.node(SHORT_CATEGORY_NAME_NODE).set(easterEggCategory.getShortCategoryName());
            node.node(OUTPUT_NODE).set(easterEggCategory.getOutput());
            node.node(HIDE_PLAYER_DATA_NODE).set(easterEggCategory.isHidePlayerData());

            for (EasterEgg easterEgg : easterEggCategory.getEggs().values()) {
                if (EggTypes.BLOCK.equals(easterEgg.getType())) {
                    if (easterEgg.getActions() != null) {
                        // TODO: (15.02 21:25) А хули тут так пусто?)
                        for (Action action : easterEgg.getActions().getLocalActions()) {
                        }
                    }
                    node.node(easterEgg.getId()).set(TypeToken.get(EggBlock.class), (EggBlock) easterEgg);
                } else {
//                    node.node(easterEgg.getId()).set(EggEntity.class, (EggEntity) easterEgg);
                    node.node(easterEgg.getId()).set(TypeToken.get(EggEntity.class), (EggEntity) easterEgg);
                }
            }
//            node.node(EFFECTS_NODE).setList(TypeToken.get(FireworkEffect.class), actionFirework.getEffectList());
            node.node(FINISH_ACTION_NODE).set(TypeToken.get(Actions.class), easterEggCategory.getFinishAction());
        }
    }
}
