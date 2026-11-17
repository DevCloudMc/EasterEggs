package ru.brainrtp.eastereggs;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
import ru.brainrtp.eastereggs.services.EasterEggService;
import ru.brainrtp.eastereggs.services.PlayerEggsService;
import ru.brainrtp.eastereggs.storage.Storage;

public class EasterEggs extends JavaPlugin {

  private Configuration mainConfig;
  @Getter private static Plugin plugin;
  private PlayerEggsService playerEggs;
  private EggsPlaceholder placeholder;
  @Getter private static Language language;
  @Getter private static EasterEggService eggService;

  @Override
  public void onEnable() {
    plugin = this;
    getSLF4JLogger().info("Starting...");

    try {
      mainConfig = new Configuration("config.conf", this.getDataFolder().toPath(), this);
      String languageFile = "lang_" + mainConfig.get().node("languageFile").getString();
      language = new Language(languageFile, this.getDataFolder().toPath(), this);

      playerEggs =
          new PlayerEggsService(
              Storage.open(mainConfig.get().node("database"), getDataFolder().toPath()),
              // a disabled plugin cannot schedule: rewards of finds saved in onDisable are dropped
              task -> {
                if (isEnabled()) {
                  Bukkit.getScheduler().runTask(this, task);
                }
              });
      eggService = new EasterEggService(this, language, playerEggs);
      eggService.reloadAllEggs();
      // plugin reload: players already online get their finds before any listener is registered
      for (Player player : Bukkit.getOnlinePlayers()) {
        playerEggs.addConnection(player.getUniqueId());
        playerEggs.loadPlayerData(player.getUniqueId());
      }
    } catch (Exception e) {
      getSLF4JLogger().error("Could not start: {}. Disabling the plugin", e.getMessage(), e);
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
      getSLF4JLogger().info("PlaceholderAPI enabled, registered our placeholders");
    } else {
      getSLF4JLogger().info("PlaceholderAPI disabled, placeholders not registered");
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
