package ru.brainrtp.eastereggs.services;

import api.logging.Logger;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.storage.EggKey;
import ru.brainrtp.eastereggs.storage.Storage;

public class PlayerEggsService {

  private static final long TIMEOUT_SECONDS = 10;

  // an entry exists only while the player's finds are loaded; without it no egg is activated
  private final Map<UUID, Set<EggKey>> found = new ConcurrentHashMap<>();

  // every storage call runs here, so writes keep their order and a load sees all earlier writes
  private final ExecutorService storageThread =
      Executors.newSingleThreadExecutor(task -> new Thread(task, "EasterEggs-Storage"));

  private volatile Storage storage;

  public PlayerEggsService(Storage storage) {
    this.storage = storage;
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
  public boolean addEggToPlayer(UUID uuid, EasterEgg egg) {
    Set<EggKey> eggs = found.get(uuid);
    EggKey key = EggKey.of(egg);
    if (eggs == null || !eggs.add(key)) {
      return false;
    }
    write("save " + key + " of " + uuid, s -> s.add(uuid, key), () -> {}, () -> eggs.remove(key));
    return true;
  }

  public void loadPlayerData(UUID uuid) throws Exception {
    Set<EggKey> eggs =
        storageThread.submit(() -> read(storage, uuid)).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    found.put(uuid, eggs);
  }

  public void removePlayerData(UUID uuid) {
    found.remove(uuid);
  }

  // main thread: finds happen only there, so none can slip in between the load and the swap
  public void reload(Storage next, Collection<UUID> online) throws Exception {
    Map<UUID, Set<EggKey>> loaded;
    try {
      loaded =
          storageThread
              .submit(() -> {
                Map<UUID, Set<EggKey>> result = new HashMap<>();
                for (UUID uuid : online) {
                  result.put(uuid, read(next, uuid));
                }
                return result;
              })
              .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (Exception e) {
      next.close();
      throw e;
    }
    Storage old = storage;
    storage = next;
    found.putAll(loaded);
    storageThread.execute(old::close);
  }

  public void removePlayerEgg(UUID uuid, EasterEgg egg) {
    EggKey key = EggKey.of(egg);
    write(
        "remove " + key + " of " + uuid,
        s -> s.removePlayerEgg(uuid, key),
        () -> forget(uuid, List.of(key)),
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
    found.values().forEach(eggs -> eggs.remove(key));
    write("delete finds of " + key, s -> s.deleteEgg(key), () -> {}, () -> {});
  }

  public void deleteCategory(String category) {
    found.values().forEach(eggs -> eggs.removeIf(key -> key.category().equals(category)));
    write(
        "delete finds of category " + category,
        s -> s.deleteCategory(category),
        () -> {},
        () -> {});
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

  // keys captured at submit time, so a find made while the delete is queued stays cached
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
