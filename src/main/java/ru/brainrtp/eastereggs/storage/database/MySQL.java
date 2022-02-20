package ru.brainrtp.eastereggs.storage.database;

import api.logging.Logger;
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.spongepowered.configurate.ConfigurationNode;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.configuration.Configuration;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MySQL implements Database {

    @Getter
    private final MysqlDataSource dataSource;
    private final Plugin plugin;
    // TODO: (18.02 21:47) Должно быть такое название
//    private static final String TABLE_NAME = "eastereggs_player_data";
    private static final String TABLE_NAME = "eastereggs";

    public MySQL(Configuration configuration, Plugin plugin) {
        this.plugin = plugin;
        ConfigurationNode database = configuration.get().node("database");
        dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setServerName(database.node("host").getString());
        dataSource.setPortNumber(database.node("port").getInt());
        dataSource.setDatabaseName(database.node("database").getString());
        dataSource.setUser(database.node("user").getString());
        dataSource.setPassword(database.node("password").getString());
        try {
            initDatabase();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initDatabase() throws IOException, SQLException {
        String setup;
        try (InputStream in = plugin.getClass().getResourceAsStream("/database/createTable.sql")) {
            setup = new String(in.readAllBytes());
        } catch (IOException e) {
            Logger.error("Could not read db setup file. Error {0}", e);
            throw e;
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(setup)) {
            stmt.execute();
        }
        Logger.info("Database setup complete.");
    }


    public void testDataSource() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(1)) {
                throw new SQLException("Could not establish database connection.");
            }
            Logger.info("Connection to the database was successful.");
        }
    }

    @Override
    public List<EasterEgg> loadPlayerData(UUID playerUUID) {
        List<EasterEgg> playerEggs = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
//                     "SELECT category, egg_id FROM " + TABLE_NAME + " WHERE player = ?;"
                     "SELECT * FROM " + TABLE_NAME + " WHERE player_uuid = ?;"
             )) {
            stmt.setString(1, playerUUID.toString());
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                String category = resultSet.getString("category");
                int eggId = resultSet.getInt("egg_id");
                Optional<EasterEggCategory> eggCategory = EasterEggs.getEggService().getCategory(category);
                if (eggCategory.isPresent()) {
                    if (eggCategory.get().getEgg(eggId).isPresent()) {
                        playerEggs.add(eggCategory.get().getEgg(eggId).get());
                    }
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return playerEggs;
    }

    @Override
    public void deleteCategory(EasterEggCategory easterEggCategory) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM " + TABLE_NAME + " WHERE category = ?;"
             )) {
            stmt.setString(1, easterEggCategory.getShortCategoryName());
            stmt.execute();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    @Override
    public void addPlayerData(UUID playerUUID, EasterEgg egg) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO " + TABLE_NAME + "(player_uuid, category, egg_Id) VALUES (?, ?, ?);"
             )) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, egg.getCategory());
            stmt.setInt(3, egg.getId());
            stmt.execute();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void removePlayerEgg(UUID playerUUID, EasterEgg egg) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM " + TABLE_NAME + " WHERE player_uuid= ? AND category = ? AND egg_Id = ?;"
             )) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, egg.getCategory());
            stmt.setInt(3, egg.getId());
            stmt.execute();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void removePlayerCategory(UUID playerUUID, EasterEggCategory easterEggCategory) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM " + TABLE_NAME + " WHERE player_uuid= ? AND category = ?;"
             )) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, easterEggCategory.getShortCategoryName());
            stmt.execute();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    @Override
    public void removeFullPlayerData(UUID playerUUID) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM " + TABLE_NAME + " WHERE player_uuid= ?;"
             )) {
            stmt.setString(1, playerUUID.toString());
            stmt.execute();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


    }


}
