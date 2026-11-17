package ru.brainrtp.eastereggs;

import java.lang.reflect.Proxy;
import org.bukkit.plugin.Plugin;
import org.slf4j.LoggerFactory;

public final class TestPlugin {

  private TestPlugin() {}

  // the plugin classes log through EasterEggs.getPlugin(), which is null outside the server
  public static void install() throws ReflectiveOperationException {
    Plugin plugin =
        (Plugin)
            Proxy.newProxyInstance(
                TestPlugin.class.getClassLoader(),
                new Class<?>[] {Plugin.class},
                (proxy, method, args) -> LoggerFactory.getLogger("test"));
    var field = EasterEggs.class.getDeclaredField("plugin");
    field.setAccessible(true);
    field.set(null, plugin);
  }
}
