package ru.brainrtp.eastereggs.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.BasicConfigurationNode;

class StorageTest {

  private static final UUID ALICE = UUID.randomUUID();
  private static final UUID BOB = UUID.randomUUID();
  private static final EggKey EGG_1 = new EggKey("easter", 1);
  private static final EggKey EGG_2 = new EggKey("easter", 2);

  @TempDir Path dataFolder;
  private Storage storage;

  @AfterEach
  void close() {
    if (storage != null) {
      storage.close();
    }
  }

  @Test
  void repeatedInsertKeepsOneRowAcrossRestarts() throws Exception {
    storage = open("sqlite");
    storage.add(ALICE, EGG_1);
    storage.close();

    storage = open("sqlite");
    storage.add(ALICE, EGG_1);

    assertEquals(List.of(EGG_1), storage.load(ALICE));
  }

  @Test
  void deletingAnEggRemovesItsRowsForAllPlayers() throws Exception {
    storage = open("sqlite");
    storage.add(ALICE, EGG_1);
    storage.add(ALICE, EGG_2);
    storage.add(BOB, EGG_1);

    storage.deleteEgg(EGG_1);

    assertEquals(List.of(EGG_2), storage.load(ALICE));
    assertEquals(List.of(), storage.load(BOB));
  }

  @Test
  void clearingOnePlayersEggKeepsTheRest() throws Exception {
    storage = open("sqlite");
    storage.add(ALICE, EGG_1);
    storage.add(ALICE, EGG_2);
    storage.add(BOB, EGG_1);

    storage.removePlayerEgg(ALICE, EGG_1);

    assertEquals(List.of(EGG_2), storage.load(ALICE));
    assertEquals(List.of(EGG_1), storage.load(BOB));
  }

  @Test
  void unknownTypeIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> open("h2"));
  }

  private Storage open(String type) throws Exception {
    BasicConfigurationNode database = BasicConfigurationNode.root();
    database.node("type").set(type);
    return Storage.open(database, dataFolder);
  }
}
