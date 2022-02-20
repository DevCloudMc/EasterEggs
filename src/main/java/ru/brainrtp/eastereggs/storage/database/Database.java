package ru.brainrtp.eastereggs.storage.database;

import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface Database {

    void initDatabase() throws IOException, SQLException;

    void testDataSource() throws SQLException;

    List<EasterEgg> loadPlayerData(UUID playerUUID);

    void deleteCategory(EasterEggCategory easterEggCategory);

    void addPlayerData(UUID playerUUID, EasterEgg egg);

    void removePlayerEgg(UUID playerUUID, EasterEgg egg);

    void removePlayerCategory(UUID playerUUID, EasterEggCategory easterEggCategory);

    void removeFullPlayerData(UUID playerUUID);
}
