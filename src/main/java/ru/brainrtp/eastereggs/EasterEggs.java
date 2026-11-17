package ru.brainrtp.eastereggs;

import api.logging.Logger;
import api.logging.handlers.JULHandler;
import java.nio.file.Path;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
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
import ru.brainrtp.eastereggs.services.PlayerEggsService;
import ru.brainrtp.eastereggs.storage.Storage;

public class EasterEggs extends JavaPlugin implements Listener {

  private Configuration mainConfig;
  @Getter private static Plugin plugin;
  private PlayerEggsService playerEggs;
  private EggsPlaceholder placeholder;
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

      ActionSerializer.init();
      playerEggs =
          new PlayerEggsService(Storage.open(mainConfig.get().node("database"), pluginDataFolder));
      eggService = new EasterEggService(this, language, playerEggs);
      eggService.reloadAllEggs();
      // plugin reload: players already online get their finds before any listener is registered
      for (Player player : Bukkit.getOnlinePlayers()) {
        playerEggs.loadPlayerData(player.getUniqueId());
      }
    } catch (Exception e) {
      Logger.error("Could not start: {0}. Disabling the plugin", e, e.getMessage());
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    registerListeners();
    registerCommands();
    registerServices();
  }

  private void registerServices() {
    getServer()
        .getServicesManager()
        .register(EasterEggService.class, eggService, this, ServicePriority.Normal);

    if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      placeholder = new EggsPlaceholder();
      placeholder.register();
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
    if (placeholder != null) {
      placeholder.unregister();
    }
    if (eggService != null) {
      Bukkit.getOnlinePlayers()
          .forEach(player -> {
            if (eggService.getEditor().isEditMode(player.getUniqueId()))
              eggService.stopEditCategory(player);
          });
    }
    if (playerEggs != null) {
      playerEggs.shutdown();
    }
  }
}
