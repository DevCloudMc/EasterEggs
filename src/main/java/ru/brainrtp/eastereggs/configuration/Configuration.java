package ru.brainrtp.eastereggs.configuration;

import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

public final class Configuration {

//    private final EasterEggs plugin;
    private CommentedConfigurationNode node;
    private HoconConfigurationLoader loader;

    /**
     * Create empty .conf file in directory, or load if file already exist
     *
     * @param name      Name of config file with extension .conf
     * @param directory Directory to file create
     */
    public Configuration(String name, Path directory) throws IOException {
        File file = Paths.get(directory.toString() + File.separator + name).toFile();
        loader = HoconConfigurationLoader.builder().file(file).build();

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
     * @param name      Name of config file with extension .conf
     * @param directory Directory to copy file
     * @param plugin    Plugin instance
     */
    public Configuration(String name, Path directory, Plugin plugin) throws IOException {
        File file = Paths.get(directory.toString() + File.separator + name).toFile();
        loader = HoconConfigurationLoader.builder().file(file).build();

        if (!Files.exists(directory)) {
            InputStream stream = plugin.getClass().getResourceAsStream("/" + name);
            if (stream == null) {
                throw new FileNotFoundException("Cannot find settings resource file");
            }
            Files.createDirectory(directory);
            Files.copy(stream, file.toPath());
        }

//        if (!Files.exists(file.toPath())) {
//            Files.copy(in,
//                    file.toPath());
//        }

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
//        String fileName = "config.conf";
        Path filePath = Paths.get(root, fileName);

        if (!Files.exists(filePath)) {
            InputStream stream = getClass().getResourceAsStream("/" + fileName);

            if (stream == null)
                throw new FileNotFoundException("Cannot find settings resource file");

            Files.copy(stream, filePath);
        }

        return (Callable<BufferedReader>) Files.newBufferedReader(filePath);
    }

//    private TypeSerializerCollection getSerializers() {
//        return TypeSerializerCollection.builder()
//                .register(ActionMessage.class, new ActionMessage.Serializer())
//                .register(InfoForwarding.class, new InfoForwarding.Serializer())
//                .register(PingData.class, new PingData.Serializer())
//                .register(BossBar.class, new BossBar.Serializer())
//                .register(Title.class, new Title.Serializer())
//                .register(Position.class, new Position.Serializer())
//                .build();
//    }
}