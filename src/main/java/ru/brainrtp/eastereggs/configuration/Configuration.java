package ru.brainrtp.eastereggs.configuration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.plugin.Plugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.NamedLocation;
import ru.brainrtp.eastereggs.data.action.*;
import ru.brainrtp.eastereggs.data.eggs.EggBlock;
import ru.brainrtp.eastereggs.data.eggs.EggEntity;
import ru.brainrtp.eastereggs.serializer.ColorSerializer;
import ru.brainrtp.eastereggs.serializer.FireworkEffectSerializer;
import ru.brainrtp.eastereggs.serializer.LocationSerializer;

public final class Configuration {

  private CommentedConfigurationNode node;
  private final HoconConfigurationLoader loader;
  @Getter @Setter private String fileName;
  @Getter private final Path path;

  /**
   * Create empty .conf file in directory, or load if file already exist
   *
   * @param fileName Name of config file with extension .conf
   * @param directory Directory to file create
   */
  public Configuration(String fileName, Path directory) throws IOException {
    setFileName(fileName);
    ConfigurationOptions options = ConfigurationOptions.defaults().serializers(getSerializers());
    path = directory.resolve(fileName);
    File file = path.toFile();
    loader = HoconConfigurationLoader.builder().file(file).defaultOptions(options).build();

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
   * @param fileName Name of config file with extension .conf
   * @param directory Directory to copy file
   * @param plugin Plugin instance
   */
  public Configuration(String fileName, Path directory, Plugin plugin) throws IOException {
    setFileName(fileName);
    path = directory.resolve(fileName);
    File file = path.toFile();
    loader = HoconConfigurationLoader.builder().file(file).build();

    if (Files.notExists(directory)) {
      Files.createDirectory(directory);
    }

    if (Files.notExists(file.toPath())) {
      try (InputStream stream = plugin.getClass().getResourceAsStream("/" + fileName)) {
        if (stream == null) {
          throw new FileNotFoundException("Cannot find settings resource file");
        }
        Files.copy(stream, file.toPath());
      }
    }

    load();
  }

  /** Reload config in memory from file */
  public void load() {
    try {
      node = loader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Save all changes in file */
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

  public static String path(ConfigurationNode node) {
    return Arrays.stream(node.path().array())
        .map(Object::toString)
        .collect(Collectors.joining("."));
  }

  public static TypeSerializerCollection getSerializers() {
    return TypeSerializerCollection.defaults()
        .childBuilder()
        .register(ActionMessage.class, new ActionMessage.Serializer())
        .register(ActionSound.class, new ActionSound.Serializer())
        .register(ActionCommand.class, new ActionCommand.Serializer())
        .register(ActionMoney.class, new ActionMoney.Serializer())
        .register(ActionFirework.class, new ActionFirework.Serializer())
        .register(Actions.class, new Actions.Serializer())
        .register(EasterEggCategory.class, new EasterEggCategory.Serializer())
        .register(EggEntity.class, new EggEntity.Serializer())
        .register(EggBlock.class, new EggBlock.Serializer())
        .register(FireworkEffect.class, new FireworkEffectSerializer())
        .register(Color.class, new ColorSerializer())
        .register(NamedLocation.class, new LocationSerializer())
        .build();
  }
}
