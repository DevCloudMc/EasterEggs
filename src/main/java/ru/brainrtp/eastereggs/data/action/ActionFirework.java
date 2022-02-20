package ru.brainrtp.eastereggs.data.action;

import io.leangen.geantyref.TypeToken;
import lombok.Data;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.util.text.RandomColor;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
public class ActionFirework implements Action {

    private int power = 0;

    private static final String ACTION_NAME = "firework";
    private List<FireworkEffect> effectList;
    private static final String EFFECTS_NODE = "effects";
    private static final String POWER_NODE = "power";

    @Override
    public String getActionTitle() {
        return ACTION_NAME;
    }

    @Override
    public void activate(Player player) {
        Location location = player.getLocation().add(new Vector(0, 1, 0));

        List<FireworkEffect> localEff = effectList;

        if (effectList == null) {
            localEff = getRandomEffects();
        }

        Firework firework = (Firework) player.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta meta = firework.getFireworkMeta();

        meta.addEffects(localEff);
        meta.setPower(power);
        firework.setFireworkMeta(meta);

    }

    private List<FireworkEffect> getRandomEffects() {
        Random random = new Random();
        List<FireworkEffect> list = new ArrayList<>();

        int count = 1 + random.nextInt(3);

        for (int i = 0; i < count; i++) {
            list.add(FireworkEffect.builder()
                    .withColor(RandomColor.getColors())
                    .withFade(RandomColor.getColors())
                    .build()
            );
        }

        return list;
    }

    public static class Serializer implements TypeSerializer<ActionFirework> {
        @Override
        public ActionFirework deserialize(Type type, ConfigurationNode node) throws SerializationException {
            ActionFirework actionFirework = new ActionFirework();
            if (!node.node(EFFECTS_NODE).isNull()) {
                actionFirework.setEffectList(getFireworkEffects(node.node(EFFECTS_NODE)));
            }
            actionFirework.setPower(node.node(POWER_NODE).getInt());
            return actionFirework;
        }

        // TODO: 28.11.2021 This is a bad implementation, but the developer hasn't given me an answer on how to fix it.
        public List<FireworkEffect> getFireworkEffects(ConfigurationNode node) {
            List<FireworkEffect> result = new ArrayList<>();
            for (ConfigurationNode configurationNode : node.childrenList()) {
                try {
                    FireworkEffect checkpoint = configurationNode.get(FireworkEffect.class);
                    result.add(checkpoint);
                } catch (SerializationException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        @Override
        public void serialize(Type type, @Nullable ActionFirework actionFirework, ConfigurationNode node) throws SerializationException {
            node.node(POWER_NODE).set(actionFirework.getPower());
            node.node(EFFECTS_NODE).setList(TypeToken.get(FireworkEffect.class), actionFirework.getEffectList());
        }
    }

}
