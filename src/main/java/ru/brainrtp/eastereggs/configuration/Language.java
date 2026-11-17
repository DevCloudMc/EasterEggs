package ru.brainrtp.eastereggs.configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.plugin.Plugin;
import org.spongepowered.configurate.serialize.SerializationException;
import ru.brainrtp.eastereggs.util.text.Colors;

public class Language {

  private final Path path;
  private final Plugin plugin;
  private Configuration configuration;

  private String prefix;

  public Language(String langCode, Path path, Plugin plugin) throws IOException {
    this.path = path;
    this.plugin = plugin;
    reload(langCode);
  }

  // in place, because every command and service holds this instance
  public void reload(String langCode) throws IOException {
    configuration = new Configuration(langCode + ".conf", path, plugin);
    prefix = getSingleMessageWithoutPrefix("prefix");
  }

  private static String missing(Object[] key) {
    return "&cMissing language key - &e"
        + Arrays.stream(key).map(String::valueOf).collect(Collectors.joining("."));
  }

  public String getSingleMessage(Object... key) {
    return Colors.of(prefix + configuration.get().node(key).getString(missing(key)));
  }

  public String getSingleMessageWithoutPrefix(Object... key) {
    return Colors.of(configuration.get().node(key).getString(missing(key)));
  }

  public List<String> getListMessages(Object... key) {
    try {
      return Colors.ofList(configuration.get().node(key).getList(String.class));
    } catch (SerializationException e) {
      e.printStackTrace();
      return null;
    }
  }
}
