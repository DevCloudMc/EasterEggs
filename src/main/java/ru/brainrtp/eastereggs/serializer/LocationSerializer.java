package ru.brainrtp.eastereggs.serializer;

import api.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import ru.brainrtp.eastereggs.util.text.Colors;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class LocationSerializer implements TypeSerializer<Location> {

    @Override
    public Location deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String world = "";
        double x = 0.0d;
        double y = 0.0d;
        double z = 0.0d;


        String[] args = Objects.requireNonNull(node.getString()).split(",");

        if (args[0] != null) {
            if (Bukkit.getWorld(args[0]) == null)
                paramWorldIsNull(node, args[0].trim(), " 'world' value");
            world = args[0].trim();
        }

        if (args[1] != null) {
            try {
                x = parseDouble(args[1].trim());
            } catch (NumberFormatException e) {
                paramMustBeAnDouble(node, "'x' coordinate");
                x = 0.0;
            }
        }

        if (args[2] != null) {
            try {
                y = parseDouble(args[2].trim());
            } catch (NumberFormatException e) {
                paramMustBeAnDouble(node, "'y' coordinate");
                y = 0.0;
            }
        }

        if (args[3] != null) {
            try {
                z = parseDouble(args[3].trim());
            } catch (NumberFormatException e) {
                paramMustBeAnDouble(node, "'z' coordinate");
                z = 0.0;
            }
        }

        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    @Override
    public void serialize(Type type, @Nullable Location location, ConfigurationNode node) throws SerializationException {
        String format = "%s, %s, %s, %s";
        String loc;
        if (location != null) {
            loc = String.format(format,
                    location.getWorld().getName(),
                    parseDouble(location.getX()),
                    parseDouble(location.getY()),
                    parseDouble(location.getZ()));
        } else {
            loc = String.format(format, "null", 0, 0, 0);
        }

        node.set(loc);
    }


    private double parseDouble(String str) {
        return Double.parseDouble(str);
    }

    private String parseDouble(double num) {
        return String.valueOf(num).replace(",", ".");
    }

    private void paramWorldIsNull(ConfigurationNode node, String world, String param) {

        String pathToWarn = Arrays.stream(node.path().array())
                .map(Object::toString)
                .collect(Collectors.joining("."));

        String logMessage = """
                &fWorld &e''{0}''&r not found. Setting the default value to &e'null'&r
                Full path to this param - &e{1}&r""";
        Logger.warn(Colors.of(logMessage), world, pathToWarn + param);
    }

    private void paramMustBeAnDouble(ConfigurationNode node, String param) {
        String pathToWarn = Arrays.stream(node.path().array())
                .map(Object::toString)
                .collect(Collectors.joining("."))
                .concat(".");

        String logMessage = """
                &fThe &e{0}&r parameter must be an double. Setting the default value to &e0.0&r
                Full path to this param - &e{1}&r""";
        Logger.warn(Colors.of(logMessage), param, pathToWarn + param);
    }
}
