package ru.brainrtp.eastereggs.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.BasicConfigurationNode;
import ru.brainrtp.eastereggs.TestPlugin;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.data.eggs.EggBlock;
import ru.brainrtp.eastereggs.storage.Storage;

class PlayerEggsServiceTest {

  private static final UUID PLAYER = UUID.randomUUID();

  @TempDir Path dataFolder;

  @BeforeAll
  static void logger() throws Exception {
    TestPlugin.install();
  }

  @Test
  void queuedFindsAreSavedBeforeShutdownFinishes() throws Exception {
    PlayerEggsService service = new PlayerEggsService(open(), Runnable::run);
    service.loadPlayerData(PLAYER);

    for (int id = 0; id < 200; id++) {
      assertTrue(service.addEggToPlayer(PLAYER, egg(id), () -> {}));
    }
    service.shutdown();

    try (Storage storage = open()) {
      assertEquals(200, storage.load(PLAYER).size());
    }
  }

  @Test
  void failedSaveGivesNoRewardAndForgetsTheFind() throws Exception {
    Storage storage = open();
    PlayerEggsService service = new PlayerEggsService(storage, Runnable::run);
    service.loadPlayerData(PLAYER);
    storage.close();
    AtomicInteger rewards = new AtomicInteger();

    assertTrue(service.addEggToPlayer(PLAYER, egg(1), rewards::incrementAndGet));
    service.shutdown();

    assertEquals(0, rewards.get());
    assertFalse(service.isCompletedEgg(PLAYER, egg(1)));
  }

  @Test
  void savedFindIsRewardedOnce() throws Exception {
    PlayerEggsService service = new PlayerEggsService(open(), Runnable::run);
    service.loadPlayerData(PLAYER);
    AtomicInteger rewards = new AtomicInteger();

    assertTrue(service.addEggToPlayer(PLAYER, egg(1), rewards::incrementAndGet));
    assertFalse(service.addEggToPlayer(PLAYER, egg(1), rewards::incrementAndGet));
    service.shutdown();

    assertEquals(1, rewards.get());
  }

  @Test
  void dataStaysUntilTheLastConnectionCloses() throws Exception {
    PlayerEggsService service = new PlayerEggsService(open(), Runnable::run);
    for (int connection = 0; connection < 2; connection++) {
      service.addConnection(PLAYER);
      service.loadPlayerData(PLAYER);
    }

    service.removePlayerData(PLAYER);
    assertNotNull(service.getPlayerData(PLAYER));
    service.removePlayerData(PLAYER);
    assertNull(service.getPlayerData(PLAYER));
    service.shutdown();
  }

  @Test
  void nothingIsRecordedWhileDataIsNotLoaded() throws Exception {
    PlayerEggsService service = new PlayerEggsService(open(), Runnable::run);

    assertFalse(service.addEggToPlayer(PLAYER, egg(1), () -> {}));
    service.shutdown();
  }

  private Storage open() throws Exception {
    BasicConfigurationNode database = BasicConfigurationNode.root();
    database.node("type").set("sqlite");
    return Storage.open(database, dataFolder);
  }

  private static EasterEgg egg(int id) {
    EasterEgg egg = new EggBlock();
    egg.setCategory("easter");
    egg.setId(id);
    return egg;
  }
}
