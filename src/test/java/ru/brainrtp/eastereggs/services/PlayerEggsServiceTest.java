package ru.brainrtp.eastereggs.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.logging.Logger;
import api.logging.handlers.JULHandler;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.BasicConfigurationNode;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.data.eggs.EggBlock;
import ru.brainrtp.eastereggs.storage.Storage;

class PlayerEggsServiceTest {

  private static final UUID PLAYER = UUID.randomUUID();

  @TempDir Path dataFolder;

  @BeforeAll
  static void logger() {
    Logger.init(new JULHandler(java.util.logging.Logger.getLogger("test")));
  }

  @Test
  void queuedFindsAreSavedBeforeShutdownFinishes() throws Exception {
    PlayerEggsService service = new PlayerEggsService(open());
    service.loadPlayerData(PLAYER);

    for (int id = 0; id < 200; id++) {
      assertTrue(service.addEggToPlayer(PLAYER, egg(id)));
    }
    service.shutdown();

    try (Storage storage = open()) {
      assertEquals(200, storage.load(PLAYER).size());
    }
  }

  @Test
  void failedSaveForgetsTheFind() throws Exception {
    Storage storage = open();
    PlayerEggsService service = new PlayerEggsService(storage);
    service.loadPlayerData(PLAYER);
    storage.close();

    assertTrue(service.addEggToPlayer(PLAYER, egg(1)));
    service.shutdown();

    assertFalse(service.isCompletedEgg(PLAYER, egg(1)));
  }

  @Test
  void nothingIsRecordedWhileDataIsNotLoaded() throws Exception {
    PlayerEggsService service = new PlayerEggsService(open());

    assertFalse(service.addEggToPlayer(PLAYER, egg(1)));
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
