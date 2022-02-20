package ru.brainrtp.eastereggs.configuration;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.spongepowered.configurate.serialize.SerializationException;
import ru.brainrtp.eastereggs.util.text.Colors;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Language {

    @Getter
    private final Configuration configuration;

    @Getter
    private final String prefix;

    public Language(String langCode, Path path, Plugin plugin) throws IOException {
        configuration = new Configuration(langCode + ".conf", path, plugin);
        prefix = getSingleMessageWithoutPrefix("prefix");
    }

    public String getSingleMessage(Object... key) {
        return Colors.of(prefix + configuration.get().node(key).getString("&cMissing language key - &e" + key));
    }

    public String getSingleMessageWithoutPrefix(Object... key) {
        return Colors.of(configuration.get().node(key).getString("&cMissing language key - &e" + key));
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
