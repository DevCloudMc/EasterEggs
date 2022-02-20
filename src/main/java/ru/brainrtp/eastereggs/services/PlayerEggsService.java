package ru.brainrtp.eastereggs.services;

import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.storage.database.Database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerEggsService {

    private final Database database;

    private final HashMap<UUID, List<EasterEgg>> playerEggsList = new HashMap<>();

    public PlayerEggsService(Database database) {
        this.database = database;
    }

    public List<EasterEgg> getPlayerData(UUID uuid) {
        return playerEggsList.get(uuid);
    }

    public boolean isCompletedEgg(UUID uuid, EasterEgg egg) {
        if (playerEggsList.get(uuid) == null) {
            return false;
        }
        return playerEggsList.get(uuid).stream()
                .anyMatch(easterEgg -> (egg.getCategory().equals(easterEgg.getCategory()) && easterEgg.getId() == egg.getId()));
    }

    public boolean isCompleteCategory(UUID uuid, EasterEggCategory easterEggCategory) {
        return easterEggCategory.getEggs().values().stream()
                .allMatch(easterEgg -> playerEggsList.get(uuid).stream()
                        .anyMatch(playerEgg ->
                                (easterEggCategory.getShortCategoryName().equals(playerEgg.getCategory())
                                        && easterEgg.getId() == playerEgg.getId())));
    }

    public void addEggToPlayer(UUID uuid, EasterEgg egg) {
        List<EasterEgg> playerEasterEggs = playerEggsList.get(uuid);
        if (playerEasterEggs != null) {
            playerEasterEggs.add(egg);
        } else {
            playerEggsList.put(uuid, new ArrayList<>(List.of(egg)));
        }
        addToDatabase(uuid, egg);
    }

    public void loadPlayerData(UUID playerUUID) {
        CompletableFuture<List<EasterEgg>> playerEggs = CompletableFuture.supplyAsync(() -> database.loadPlayerData(playerUUID));
        playerEggs.thenAccept((easterEggList) -> playerEggsList.put(playerUUID, easterEggList));
    }

    public void removePlayerData(UUID uuid) {
        playerEggsList.remove(uuid);
    }

    private void addToDatabase(UUID playerUUID, EasterEgg egg) {
        CompletableFuture.runAsync(() -> database.addPlayerData(playerUUID, egg));
    }

    public void removeFromDatabase(EasterEggCategory easterEggCategory) {
        database.deleteCategory(easterEggCategory);
    }


    public void removePlayerEgg(UUID playerUUID, EasterEgg egg) {
        playerEggsList.get(playerUUID).removeIf(egg::equals);
        database.removePlayerEgg(playerUUID, egg);
    }

    public void removeFullPlayerData(UUID playerUUID) {
        playerEggsList.remove(playerUUID);
        database.removeFullPlayerData(playerUUID);
    }

    public void removePlayerCategory(UUID playerUUID, EasterEggCategory easterEggCategory) {
        playerEggsList.get(playerUUID).removeIf(egg -> easterEggCategory.getShortCategoryName().equals(egg.getCategory()));
        database.removePlayerCategory(playerUUID, easterEggCategory);
    }
}