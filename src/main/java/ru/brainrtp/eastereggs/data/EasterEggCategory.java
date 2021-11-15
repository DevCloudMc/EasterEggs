package ru.brainrtp.eastereggs.data;

import io.leangen.geantyref.TypeToken;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.configuration.Configuration;
import ru.brainrtp.eastereggs.data.action.Action;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;

import java.lang.reflect.Type;
import java.util.*;

public class EasterEggCategory {

    private String title;
    private String output;
    private Configuration config;
    private boolean hidePlayerData = false;

    private final Map<Integer, EasterEgg> eggs = new HashMap<>();
    private List<Action> finishAction = new ArrayList<>();

    public EasterEggCategory(String name) {
        this.title = name;
    }

    public String getTitle() {
        return title;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getOutput() {
        return output;
    }

    public Optional<EasterEgg> getEgg(int id) {
        return Optional.ofNullable(eggs.get(id));
    }

    public List<Action> getFinishAction() {
        return finishAction;
    }

    public void setFinishAction(List<Action> list) {
        finishAction = list;
    }

    public boolean isHidePlayerData() {
        return hidePlayerData;
    }

    public void setHidePlayerData(boolean value) {
        hidePlayerData = value;
    }

    public Optional<EasterEgg> getEggByLocation(Location loc) {
        for (EasterEgg egg : eggs.values()) {
            if (egg.getLocation().equals(loc)) {
                return Optional.of(egg);
            }
        }

        return Optional.empty();
    }

    public Map<Integer, EasterEgg> getEggs() {
        return eggs;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    public void addEgg(EasterEgg egg) {
        egg.setCategory(title);
        eggs.put(egg.getId(), egg);
    }

    public void removeEgg(int id) {
        eggs.remove(id);

        if (config != null) {
            // todo: под вопросом.
            config.get().node(id).removeChild(id);
            config.save();
        }
    }

//    public void finish(Player player) {
//        int founded = EasterEggs.getEasterEggService().getPlayerService().getPlayerData(player.getUniqueId())
//                .get(name).getEggs().size();
//        int count = eggs.size();
//
//        Placeholder ph = new Placeholder();
//        ph.addData(Placeholder.PLAYER, player.getName());
//        ph.addData(Placeholder.CATEGORY, name);
//        ph.addData(Placeholder.FOUNDED_EGG, founded);
//        ph.addData(Placeholder.EGG_COUNT, count);
//
//        for (Action action : finishAction) {
//            action.activate(player, ph);
//        }
//    }

    public void clear() {
        eggs.clear();
    }

    public void save() {
        try {
            config.get().set(TypeToken.get(EasterEggCategory.class), this);
            config.save();
        } catch (SerializationException e) {
            e.printStackTrace();
        }
    }


    public static class Serializer implements TypeSerializer<EasterEggCategory> {

        @Override
        public EasterEggCategory deserialize(Type type, ConfigurationNode node) throws SerializationException {
            return null;
        }

        @Override
        public void serialize(Type type, @Nullable EasterEggCategory obj, ConfigurationNode node) throws SerializationException {

        }
    }
}
