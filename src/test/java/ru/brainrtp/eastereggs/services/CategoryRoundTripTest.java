package ru.brainrtp.eastereggs.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import ru.brainrtp.eastereggs.TestPlugin;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.NamedLocation;
import ru.brainrtp.eastereggs.data.action.ActionFirework;
import ru.brainrtp.eastereggs.data.action.ActionMessage;
import ru.brainrtp.eastereggs.data.action.ActionMoney;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.data.eggs.EggBlock;
import ru.brainrtp.eastereggs.data.eggs.EggEntity;

class CategoryRoundTripTest {

  @TempDir Path dataFolder;
  private Plugin plugin;
  private Language language;
  private Path eggs;

  @BeforeEach
  void setUp() throws Exception {
    plugin =
        (Plugin)
            Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] {Plugin.class},
                (proxy, method, args) -> dataFolder.toFile());
    TestPlugin.install();
    // a proxy cannot load resources, so Language finds the file already in place
    try (var stream = getClass().getResourceAsStream("/lang_en.conf")) {
      Files.copy(stream, dataFolder.resolve("lang_en.conf"));
    }
    language = new Language("lang_en", dataFolder, plugin);
    eggs = dataFolder.resolve("eggs");
  }

  // getName() and getUniqueId() are the only calls the egg lookup makes
  private static World world(String name) {
    return (World)
        Proxy.newProxyInstance(
            World.class.getClassLoader(), new Class<?>[] {World.class}, (p, m, a) -> name);
  }

  private static Entity entity(UUID id) {
    return (Entity)
        Proxy.newProxyInstance(
            Entity.class.getClassLoader(), new Class<?>[] {Entity.class}, (p, m, a) -> id);
  }

  @Test
  void categorySurvivesSaveAndLoad() throws Exception {
    UUID standId = UUID.randomUUID();
    EasterEggService service = new EasterEggService(plugin, language, null);
    assertTrue(service.createNewCategory("easter"));

    ActionMoney money = money(12.5F);
    ActionMessage message = new ActionMessage();
    message.setMessages(List.of("first", "second"));
    message.setTitle("title");
    message.setSubtitle("subtitle");
    message.setActionBar("bar");
    message.setFadeIn(5);
    message.setStay(40);
    message.setFadeOut(7);
    ActionFirework firework = new ActionFirework();
    firework.setPower(2);
    firework.setEffectList(
        List.of(
            FireworkEffect.builder()
                .with(FireworkEffect.Type.STAR)
                .withColor(Color.RED)
                .withFade(Color.BLUE)
                .trail(true)
                .build()));

    EggBlock block = new EggBlock();
    block.setLocation(new NamedLocation("late", 10, 64, -5));
    block.addAction(money(5));
    block.addAction(money);
    block.addAction(message);
    block.addAction(firework);
    service.createNewEgg("easter", block);
    assertEquals(List.of(money, message, firework), block.getActions().getLocalActions());

    EggEntity entity = new EggEntity();
    entity.setEntityLocation(new Location(world("eggs"), 3.5, 64, 7.5));
    entity.setEntityUUID(standId);
    service.createNewEgg("easter", entity);

    Path file = eggs.resolve("easter.conf");
    Object saved = read(file);
    // the loader accepts both shapes, so only the file shows that money is written as a plain
    // number
    assertEquals(
        12.5,
        HoconConfigurationLoader.builder()
            .path(file)
            .build()
            .load()
            .node("0", "actions", "money")
            .getDouble());

    EasterEggService reloaded = new EasterEggService(plugin, language, null);
    reloaded.reloadAllEggs();

    EasterEgg loadedBlock =
        reloaded.getEasterEgg(new Location(world("late"), 10.5, 64.9, -4.5)).orElseThrow();
    assertEquals(0, loadedBlock.getId());
    List<?> loadedActions = loadedBlock.getActions().getLocalActions();
    assertEquals(3, loadedActions.size());
    assertEquals(Set.of(money, message, firework), Set.copyOf(loadedActions));
    assertEquals(1, reloaded.getEasterEgg(entity(standId)).orElseThrow().getId());

    reloaded.getCategory("easter").orElseThrow().save();
    assertEquals(saved, read(file));
  }

  @Test
  void fileWrittenBy30LoadsAndBrokenFileIsSkipped() throws Exception {
    Files.createDirectories(eggs);
    Files.writeString(
        eggs.resolve("broken.conf"),
        "category = broken\n\"0\" { id = 0, type = BLOCK, location = \"world, 1\" }");
    Files.writeString(
        eggs.resolve("old.conf"),
        """
        title = "Old"
        category = "old"
        "0" {
          id = 0
          type = BLOCK
          location = "world, 1.0, 2.0, 3.0"
          actions {
            money { money = 7.5 }
          }
        }
        """);

    EasterEggService service = new EasterEggService(plugin, language, null);
    service.reloadAllEggs();

    EasterEgg egg = service.getCategory("old").orElseThrow().getEgg(0).orElseThrow();
    assertEquals(List.of(money(7.5F)), egg.getActions().getLocalActions());
    assertTrue(service.getCategory("broken").isEmpty());
  }

  @Test
  void invalidCategoryNameCreatesNoFile() throws Exception {
    EasterEggService service = new EasterEggService(plugin, language, null);

    assertFalse(service.createNewCategory("../outside"));
    assertFalse(service.createNewCategory("a".repeat(33)));
    assertFalse(Files.exists(dataFolder.resolve("outside.conf")));
    assertTrue(service.getAllCategories().isEmpty());
  }

  private static ActionMoney money(float amount) {
    ActionMoney money = new ActionMoney();
    money.setMoney(amount);
    return money;
  }

  private static Object read(Path file) throws Exception {
    return HoconConfigurationLoader.builder().path(file).build().load().raw();
  }
}
