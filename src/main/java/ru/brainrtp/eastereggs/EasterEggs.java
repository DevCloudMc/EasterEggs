package ru.brainrtp.eastereggs;

import api.logging.Logger;
import api.logging.handlers.JULHandler;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import ru.brainrtp.eastereggs.commands.MainCommand;
import ru.brainrtp.eastereggs.commands.main.*;
import ru.brainrtp.eastereggs.commands.main.action.ActionCommand;
import ru.brainrtp.eastereggs.commands.main.action.AddActionCommand;
import ru.brainrtp.eastereggs.commands.main.action.RemoveActionCommand;
import ru.brainrtp.eastereggs.commands.main.category.CategoryCommand;
import ru.brainrtp.eastereggs.commands.main.category.CreateCategoryCommand;
import ru.brainrtp.eastereggs.commands.main.category.DeleteCategoryCommand;
import ru.brainrtp.eastereggs.configuration.Configuration;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.placeholders.EggsPlaceholder;
import ru.brainrtp.eastereggs.listeners.*;
import ru.brainrtp.eastereggs.serializer.ActionSerializer;
import ru.brainrtp.eastereggs.services.EasterEggService;
import ru.brainrtp.eastereggs.storage.database.Database;
import ru.brainrtp.eastereggs.storage.database.MySQL;

public class EasterEggs extends JavaPlugin implements Listener {

  private Configuration mainConfig;
  @Getter private static Plugin plugin;
  @Getter private static Economy economy;
  private Database database;
  @Getter private static Language language;
  @Getter private static EasterEggService eggService;
  @Getter private static Path pluginDataFolder;

  @Override
  public void onEnable() {
    plugin = this;
    Logger.init(new JULHandler(getLogger()));
    Logger.info("Starting...");
    pluginDataFolder = this.getDataFolder().toPath();

    this.getServer().getPluginManager().registerEvents(this, this);
    try {
      mainConfig = new Configuration("config.conf", this.getDataFolder().toPath(), this);
      String languageFile = "lang_" + mainConfig.get().node("languageFile").getString();
      language = new Language(languageFile, this.getDataFolder().toPath(), this);
    } catch (IOException e) {
      e.printStackTrace();
    }

    ActionSerializer.init();
    database = new MySQL(mainConfig, this);

    try {
      eggService = new EasterEggService(this, language, database);
    } catch (IOException e) {
      Logger.error("Cant initialize EasterEgg service. Disable plugin");
      e.printStackTrace();
      getServer().getPluginManager().disablePlugin(this);
    }
    registerListeners();
    registerCommands();
    registerServices();

    eggService.reloadAllEggs();
    try {
      database.testDataSource();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void registerServices() {

    RegisteredServiceProvider<Economy> registration =
        getServer().getServicesManager().getRegistration(Economy.class);

    if (registration != null) {
      economy = registration.getProvider();
    } else {
      Logger.error("Economy plugin not installed!");
    }

    getServer()
        .getServicesManager()
        .register(EasterEggService.class, eggService, this, ServicePriority.Normal);

    if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      new EggsPlaceholder().register();
      getLogger().info("PlaceholderAPI enabled, registered our placeholders");
    } else {
      getLogger().info("PlaceholderAPI disabled, placeholders not registered");
    }
  }

  private void registerListeners() {
    getServer().getPluginManager().registerEvents(new BlocksListener(), this);
    getServer().getPluginManager().registerEvents(new DamageListener(), this);
    getServer().getPluginManager().registerEvents(new PlayerInteractEntityListener(), this);
    getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
    getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
  }

  private void registerCommands() {
    ru.brainrtp.eastereggs.commands.Command ee =
        new MainCommand("ee.admin", language)
            // TODO: (19.02 22:44) Kludge, because params - static)
            .addSub("clear", new ClearPlayerDataCommand(language, eggService))
            .addSub("reload", new ReloadCommand(language, mainConfig))
            .addSub("edit", new EditCategoryCommand(language, eggService))
            .addSub("tp", new TeleportCommand(language, eggService, plugin))
            .addSub(
                "action",
                new ActionCommand()
                    .addSub("add", new AddActionCommand(language, eggService))
                    .addSub("remove", new RemoveActionCommand(language, eggService)))
            .addSub(
                "category",
                new CategoryCommand()
                    .addSub("create", new CreateCategoryCommand(language, eggService))
                    .addSub("delete", new DeleteCategoryCommand(language, eggService)))
            .addSub("list", new ListCategoryCommand(language, eggService));

    getServer().getPluginCommand("ee").setExecutor(ee);
  }

  @Override
  public void onDisable() {
    Bukkit.getOnlinePlayers()
        .forEach(player -> {
          if (eggService.getEditor().isEditMode(player.getUniqueId()))
            eggService.stopEditCategory(player);
        });
  }
}
