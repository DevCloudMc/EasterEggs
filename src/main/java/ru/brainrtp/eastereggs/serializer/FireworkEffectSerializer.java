package ru.brainrtp.eastereggs.serializer;

import api.logging.Logger;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.util.text.Colors;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FireworkEffectSerializer implements TypeSerializer<FireworkEffect> {

    private static final String COLORS_NODE = "colors";
    private static final String FADE_COLORS_NODE = "fadeColors";
    private static final String TYPE_NODE = "type";
    private static final String TRAIL_NODE = "trail";

    @Override
    public FireworkEffect deserialize(Type type, ConfigurationNode node) throws SerializationException {
        FireworkEffect.Builder builder = FireworkEffect.builder();

        if (!node.node(COLORS_NODE).isNull()) {
            List<Color> colors = getColorList(node.node(COLORS_NODE));
            builder.withColor(colors);
        }
        if (!node.node(FADE_COLORS_NODE).isNull()) {
            List<Color> fadeColors = getColorList(node.node(FADE_COLORS_NODE));
            builder.withFade(fadeColors);
        }

        FireworkEffect.Type fireworkEffectType;

        try {
            fireworkEffectType = FireworkEffect.Type.valueOf(node.node(TYPE_NODE).getString());
        } catch (IllegalArgumentException e) {
            fireworkEffectType = FireworkEffect.Type.BALL;
            enumValueNotFound(node, TYPE_NODE);
        }
        builder.with(fireworkEffectType);

        builder.trail(node.node(TYPE_NODE).getBoolean());

        return builder.build();
    }

    @Override
    public void serialize(Type type, @Nullable FireworkEffect fireworkEffect, ConfigurationNode node) throws SerializationException {
        if (fireworkEffect != null) {
            node.node(TYPE_NODE).set(fireworkEffect.getType().toString());
            node.node(COLORS_NODE).setList(Color.class, fireworkEffect.getColors());
            node.node(FADE_COLORS_NODE).setList(Color.class, fireworkEffect.getFadeColors());
            node.node(TRAIL_NODE).set(fireworkEffect.hasTrail());
        } else {
            node.node(TYPE_NODE).set(FireworkEffect.Type.BALL.toString());
            node.node(COLORS_NODE).setList(Color.class, Arrays.asList(Color.AQUA, Color.WHITE));
            node.node(FADE_COLORS_NODE).setList(Color.class, Arrays.asList(Color.AQUA, Color.WHITE));
            node.node(TRAIL_NODE).set(true);
        }
    }

    public List<Color> getColorList(ConfigurationNode node) {
        List<Color> result = new ArrayList<>();
        for (ConfigurationNode configurationNode : node.childrenList()) {
            Color checkpoint;
            try {
                checkpoint = configurationNode.get(Color.class);
            } catch (SerializationException | NumberFormatException e) {
                checkpoint = Color.WHITE;
                paramMustBeAnHex(node, configurationNode.getString());
            }
            result.add(checkpoint);
        }
        return result;
    }


    private void paramMustBeAnHex(ConfigurationNode node, String param) {
        String pathToWarn = Arrays.stream(node.path().array())
                .map(Object::toString)
                .collect(Collectors.joining("."))
                .concat(".");

        String logMessage = """
                &fThe &e''{0}''&r color must be an HEX format. Setting the default value to &eFFF &r(white color).
                Check more HEX colors here: https://www.color-hex.com/
                Full path to this param - &e{1}&r""";
        Logger.warn(Colors.of(logMessage), param, pathToWarn + param);
    }

    private void enumValueNotFound(ConfigurationNode node, String param) {
        String pathToWarn = Arrays.stream(node.path().array())
                .map(Object::toString)
                .collect(Collectors.joining("."))
                .concat(".");

        String logMessage = """
                &fThe &e{0}&r firework type not found. Setting the default value to &eBALL&r.
                Check more firework types here:
                https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/FireworkEffect.Type.html
                Full path to this param - &e{1}&r""";
        Logger.warn(Colors.of(logMessage), param, pathToWarn + param);
    }
}
