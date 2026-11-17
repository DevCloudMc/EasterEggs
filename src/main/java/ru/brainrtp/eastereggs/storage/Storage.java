package ru.brainrtp.eastereggs.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.spongepowered.configurate.ConfigurationNode;

public final class Storage implements AutoCloseable {

  private final HikariDataSource dataSource;

  private Storage(HikariConfig config) throws Exception {
    dataSource = new HikariDataSource(config);
    try {
      createTables();
    } catch (Exception e) {
      dataSource.close();
      throw e;
    }
  }

  public static Storage open(ConfigurationNode database, Path dataFolder) throws Exception {
    String type = database.node("type").getString("sqlite");
    HikariConfig config = new HikariConfig();
    switch (type.toLowerCase(Locale.ROOT)) {
      case "sqlite" -> {
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + dataFolder.resolve("eastereggs.db"));
      }
      case "postgresql" -> {
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl(
            "jdbc:postgresql://"
                + database.node("host").getString("localhost")
                + ":"
                + database.node("port").getInt(5432)
                + "/"
                + database.node("database").getString("eastereggs"));
        config.setUsername(database.node("user").getString());
        config.setPassword(database.node("password").getString());
        // seconds: a hung query must not hold the single storage thread forever
        config.addDataSourceProperty("socketTimeout", 30);
      }
      default ->
          throw new IllegalArgumentException(
              "Unknown database.type \"" + type + "\" in config.conf, use sqlite or postgresql");
    }
    // registers the driver with DriverManager, so HikariCP finds it without falling back
    Class.forName(config.getDriverClassName());
    // all queries run on the single storage thread of PlayerEggsService
    config.setMaximumPoolSize(1);
    // well under the 10 s login wait, so one stuck query does not time out every login behind it
    config.setConnectionTimeout(5000);
    return new Storage(config);
  }

  private void createTables() throws Exception {
    String script;
    try (InputStream in = Storage.class.getResourceAsStream("/database/createTable.sql")) {
      script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      for (String sql : script.split(";")) {
        if (!sql.isBlank()) {
          statement.execute(sql);
        }
      }
    }
  }

  public List<EggKey> load(UUID player) throws SQLException {
    List<EggKey> eggs = new ArrayList<>();
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement =
            connection.prepareStatement(
                "SELECT category, egg_id FROM eastereggs WHERE player_uuid = ?")) {
      statement.setString(1, player.toString());
      try (ResultSet rows = statement.executeQuery()) {
        while (rows.next()) {
          eggs.add(new EggKey(rows.getString("category"), rows.getInt("egg_id")));
        }
      }
    }
    return eggs;
  }

  public void add(UUID player, EggKey egg) throws SQLException {
    update(
        "INSERT INTO eastereggs (player_uuid, category, egg_id) VALUES (?, ?, ?)"
            + " ON CONFLICT DO NOTHING",
        player.toString(),
        egg.category(),
        egg.id());
  }

  public void removePlayerEgg(UUID player, EggKey egg) throws SQLException {
    update(
        "DELETE FROM eastereggs WHERE player_uuid = ? AND category = ? AND egg_id = ?",
        player.toString(),
        egg.category(),
        egg.id());
  }

  public void removePlayerCategory(UUID player, String category) throws SQLException {
    update(
        "DELETE FROM eastereggs WHERE player_uuid = ? AND category = ?",
        player.toString(),
        category);
  }

  public void removePlayer(UUID player) throws SQLException {
    update("DELETE FROM eastereggs WHERE player_uuid = ?", player.toString());
  }

  public void deleteEgg(EggKey egg) throws SQLException {
    update("DELETE FROM eastereggs WHERE category = ? AND egg_id = ?", egg.category(), egg.id());
  }

  public void deleteCategory(String category) throws SQLException {
    update("DELETE FROM eastereggs WHERE category = ?", category);
  }

  private void update(String sql, Object... params) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      for (int i = 0; i < params.length; i++) {
        statement.setObject(i + 1, params[i]);
      }
      statement.executeUpdate();
    }
  }

  @Override
  public void close() {
    dataSource.close();
  }
}
