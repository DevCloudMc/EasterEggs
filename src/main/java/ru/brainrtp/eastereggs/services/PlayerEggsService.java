package ru.brainrtp.eastereggs.services;

import api.logging.Logger;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.storage.EggKey;
import ru.brainrtp.eastereggs.storage.Storage;

public class PlayerEggsService {

  private static final long TIMEOUT_SECONDS = 10;

  // an entry exists only while the player's finds are loaded; without it no egg is activated
  private final Map<UUID, Set<EggKey>> found = new ConcurrentHashMap<>();

  // open connections per player: a reconnect opens the new one before the old one closes
  private final Map<UUID, Integer> connections = new ConcurrentHashMap<>();

  // every storage call runs here, so writes keep their order and a load sees all earlier writes
  private final ExecutorService storageThread =
      Executors.newSingleThreadExecutor(task -> new Thread(task, "EasterEggs-Storage"));

  private final Storage storage;
  private final Executor mainThread;

  public PlayerEggsService(Storage storage, Executor mainThread) {
    this.storage = storage;
    this.mainThread = mainThread;
  }

  public Set<EggKey> getPlayerData(UUID uuid) {
    return found.get(uuid);
  }

  public boolean isCompletedEgg(UUID uuid, EasterEgg egg) {
    Set<EggKey> eggs = found.get(uuid);
    return eggs != null && eggs.contains(EggKey.of(egg));
  }

  public long countFound(UUID uuid, EasterEggCategory category) {
    Set<EggKey> eggs = found.getOrDefault(uuid, Set.of());
    return category.getEggs().keySet().stream()
        .filter(id -> eggs.contains(new EggKey(category.getShortCategoryName(), id)))
        .count();
  }

  public boolean isCompleteCategory(UUID uuid, EasterEggCategory category) {
    return countFound(uuid, category) == category.getEggs().size();
  }

  // main thread; false when the player's data is not loaded or the egg is already found
  public boolean addEggToPlayer(UUID uuid, EasterEgg egg, Runnable reward) {
    Set<EggKey> eggs = found.get(uuid);
    EggKey key = EggKey.of(egg);
    if (eggs == null || !eggs.add(key)) {
      return false;
    }
    // reward on the main thread only after the INSERT, so an unsaved find is never rewarded
    write(
        "save " + key + " of " + uuid,
        s -> s.add(uuid, key),
        () -> mainThread.execute(reward),
        () -> eggs.remove(key));
    return true;
  }

  public void addConnection(UUID uuid) {
    connections.merge(uuid, 1, Integer::sum);
  }

  public void loadPlayerData(UUID uuid) throws Exception {
    Future<Set<EggKey>> load = storageThread.submit(() -> read(storage, uuid));
    Set<EggKey> eggs;
    try {
      eggs = load.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (Exception e) {
      // an abandoned load must not wait in the queue behind a dead database
      load.cancel(false);
      throw e;
    }
    // a second session of the player keeps the set of the first, with finds made since the read
    found.merge(
        uuid,
        eggs,
        (old, fresh) -> {
          old.addAll(fresh);
          return old;
        });
  }

  public void removePlayerData(UUID uuid) {
    // atomic with addConnection, so a login racing the last close loads after the entry is gone
    connections.computeIfPresent(
        uuid,
        (id, count) -> {
          if (count > 1) {
            return count - 1;
          }
          found.remove(id);
          return null;
        });
  }

  public void removePlayerEgg(UUID uuid, EasterEgg egg) {
    EggKey key = EggKey.of(egg);
    List<EggKey> cached =
        found.getOrDefault(uuid, Set.of()).contains(key) ? List.of(key) : List.of();
    write(
        "remove " + key + " of " + uuid,
        s -> s.removePlayerEgg(uuid, key),
        () -> forget(uuid, cached),
        () -> {});
  }

  public void removePlayerCategory(UUID uuid, EasterEggCategory easterEggCategory) {
    String category = easterEggCategory.getShortCategoryName();
    List<EggKey> cached =
        found.getOrDefault(uuid, Set.of()).stream()
            .filter(key -> key.category().equals(category))
            .toList();
    write(
        "remove category " + category + " of " + uuid,
        s -> s.removePlayerCategory(uuid, category),
        () -> forget(uuid, cached),
        () -> {});
  }

  public void removeFullPlayerData(UUID uuid) {
    List<EggKey> cached = List.copyOf(found.getOrDefault(uuid, Set.of()));
    write(
        "remove all eggs of " + uuid,
        s -> s.removePlayer(uuid),
        () -> forget(uuid, cached),
        () -> {});
  }

  public void deleteEgg(EasterEgg egg) {
    EggKey key = EggKey.of(egg);
    Runnable uncache = () -> found.values().forEach(eggs -> eggs.remove(key));
    uncache.run();
    // again after the delete: a load that read the rows before it may have cached them since
    write("delete finds of " + key, s -> s.deleteEgg(key), uncache, () -> {});
  }

  public void deleteCategory(String category) {
    Runnable uncache =
        () -> found.values().forEach(eggs -> eggs.removeIf(key -> key.category().equals(category)));
    uncache.run();
    // again after the delete: a load that read the rows before it may have cached them since
    write(
        "delete finds of category " + category, s -> s.deleteCategory(category), uncache, () -> {});
  }

  public void shutdown() {
    storageThread.shutdown();
    try {
      if (!storageThread.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        Logger.error(
            "Storage queue did not drain in {0} s, {1} queued writes are lost",
            TIMEOUT_SECONDS, storageThread.shutdownNow().size());
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    storage.close();
  }

  private static Set<EggKey> read(Storage storage, UUID uuid) throws SQLException {
    Set<EggKey> eggs = ConcurrentHashMap.newKeySet();
    eggs.addAll(storage.load(uuid));
    return eggs;
  }

  // only keys that were cached at submit time, so a find made while the delete is queued stays
  private void forget(UUID uuid, Collection<EggKey> keys) {
    Set<EggKey> eggs = found.get(uuid);
    if (eggs != null) {
      eggs.removeAll(keys);
    }
  }

  private void write(String what, StorageCall call, Runnable onSuccess, Runnable onFailure) {
    storageThread.execute(() -> {
      try {
        call.run(storage);
        onSuccess.run();
      } catch (Exception e) {
        onFailure.run();
        Logger.error("Could not {0}", e, what);
      }
    });
  }

  private interface StorageCall {
    void run(Storage storage) throws SQLException;
  }
}
