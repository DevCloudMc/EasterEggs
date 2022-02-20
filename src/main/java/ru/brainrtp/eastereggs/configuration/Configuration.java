package ru.brainrtp.eastereggs.configuration;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.NPCData;
import ru.brainrtp.eastereggs.data.Skin;
import ru.brainrtp.eastereggs.data.action.*;
import ru.brainrtp.eastereggs.data.eggs.EggBlock;
import ru.brainrtp.eastereggs.data.eggs.EggEntity;
import ru.brainrtp.eastereggs.serializer.ColorSerializer;
import ru.brainrtp.eastereggs.serializer.FireworkEffectSerializer;
import ru.brainrtp.eastereggs.serializer.LocationSerializer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

public final class Configuration {

    private CommentedConfigurationNode node;
    private final HoconConfigurationLoader loader;
    @Getter
    @Setter
    private String fileName;

    /**
     * Create empty .conf file in directory, or load if file already exist
     *
     * @param fileName  Name of config file with extension .conf
     * @param directory Directory to file create
     */
    public Configuration(String fileName, Path directory) throws IOException {
        setFileName(fileName);
        ConfigurationOptions options = ConfigurationOptions.defaults().serializers(getSerializers());
        File file = Paths.get(directory.toString() + File.separator + fileName).toFile();
        loader = HoconConfigurationLoader.builder().file(file)
                .defaultOptions(options)
                .build();


        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        if (!Files.exists(file.toPath())) {
            Files.createFile(file.toPath());
        }

        load();
    }


    /**
     * Copy .conf file from jar resources if file not exist
     *
     * @param fileName  Name of config file with extension .conf
     * @param directory Directory to copy file
     * @param plugin    Plugin instance
     */
    public Configuration(String fileName, Path directory, Plugin plugin) throws IOException {
        setFileName(fileName);
        File file = Paths.get(directory.toString() + File.separator + fileName).toFile();
        loader = HoconConfigurationLoader.builder().file(file).build();

        if (Files.notExists(directory)) {
            Files.createDirectory(directory);
        }

        if (Files.notExists(file.toPath())) {
            InputStream stream = plugin.getClass().getResourceAsStream("/" + fileName);
            if (stream == null) {
                throw new FileNotFoundException("Cannot find settings resource file");
            }
            Files.copy(stream, file.toPath());
        }

        load();
    }

    /**
     * Reload config in memory from file
     */
    public void load() {
        try {
            node = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save all changes in file
     */
    public void save() {
        try {
            loader.save(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CommentedConfigurationNode get() {
        return this.node;
    }

    private @Nullable Callable<BufferedReader> getReader(String root, String fileName) throws IOException {
        Path filePath = Paths.get(root, fileName);

        if (!Files.exists(filePath)) {
            InputStream stream = getClass().getResourceAsStream("/" + fileName);

            if (stream == null)
                throw new FileNotFoundException("Cannot find settings resource file");

            Files.copy(stream, filePath);
        }

        return (Callable<BufferedReader>) Files.newBufferedReader(filePath);
    }

    public static TypeSerializerCollection getSerializers() {
        return TypeSerializerCollection.defaults().childBuilder()
                .register(ActionMessage.class, new ActionMessage.Serializer())
                .register(ActionSound.class, new ActionSound.Serializer())
                .register(ActionCommand.class, new ActionCommand.Serializer())
                .register(ActionMoney.class, new ActionMoney.Serializer())
                .register(ActionFirework.class, new ActionFirework.Serializer())
                .register(Actions.class, new Actions.Serializer())
                .register(EasterEggCategory.class, new EasterEggCategory.Serializer())
                .register(EggEntity.class, new EggEntity.Serializer())
                .register(EggBlock.class, new EggBlock.Serializer())
                .register(Skin.class, new Skin.Serializer())
                .register(NPCData.class, new NPCData.Serializer())
                .register(FireworkEffect.class, new FireworkEffectSerializer())
                .register(Color.class, new ColorSerializer())
                .register(Location.class, new LocationSerializer())
                .build();
    }
}