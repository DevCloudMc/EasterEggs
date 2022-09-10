package ru.brainrtp.eastereggs;


import api.logging.Logger;
import api.logging.handlers.JULHandler;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.Locales;
import co.aikar.commands.PaperCommandManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.sentry.Sentry;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.dependency.Libraries;
import org.bukkit.plugin.java.annotation.dependency.Library;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import ru.brainrtp.eastereggs.commandAikar.*;
import ru.brainrtp.eastereggs.commandAikar.action.AddActionCommand;
import ru.brainrtp.eastereggs.commandAikar.action.RemoveActionCommand;
import ru.brainrtp.eastereggs.commandAikar.category.CreateCategoryCommand;
import ru.brainrtp.eastereggs.commandAikar.category.DeleteCategoryCommand;
import ru.brainrtp.eastereggs.configuration.Configuration;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.placeholders.EggsPlaceholder;
import ru.brainrtp.eastereggs.listeners.*;
import ru.brainrtp.eastereggs.providers.TokensProvider;
import ru.brainrtp.eastereggs.serializer.ActionSerializer;
import ru.brainrtp.eastereggs.services.EasterEggService;
import ru.brainrtp.eastereggs.storage.database.Database;
import ru.brainrtp.eastereggs.storage.database.MySQL;
import ru.brainrtp.eastereggs.util.BukkitTasks;
import ru.brainrtp.eastereggs.util.highlighter.blockhighlight.BlockHighlightTimer;
import ru.brainrtp.eastereggs.util.highlighter.blockhighlight.Util;
import ru.brainrtp.eastereggs.util.json.AnnotatedDeserializer;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

@org.bukkit.plugin.java.annotation.plugin.Plugin(name = "EasterEggs", version = "3.2.0")
@Dependency("ProtocolLib")
@Dependency("Vault")
@Dependency("GlowAPI")
@Dependency("PacketListenerApi")
@SoftDependency("PlaceholderAPI")
@Author("BrainRTP")
@Libraries({
        @Library("org.spongepowered:configurate-hocon:4.1.2"),
        @Library("io.sentry:sentry:6.4.1")
})
@Commands(@Command(name = "eastereggs", desc = "EasterEggs command", aliases = {"ee"}))
@ApiVersion(ApiVersion.Target.v1_19)
public class EasterEggs extends JavaPlugin implements Listener {

    private Configuration mainConfig;
    @Getter
    private static Plugin plugin;
    @Getter
    private static Economy economy;
    private Database database;
    @Getter
    private static Language language;
    @Getter
    private static EasterEggService eggService;
    @Getter
    private static Path pluginDataFolder;
    @Getter
    private static BlockHighlightTimer blockHighlightTimer;

    private PaperCommandManager commandManager;
    private Gson gson;

    @Override
    public void onEnable() {
        plugin = this;
        Logger.init(new JULHandler(getLogger()));
        Logger.info("Starting...");

        Sentry.init(options -> {
            options.setDsn("https://acf6bda24ea74969bfd7e45542595a07@o1322226.ingest.sentry.io/6579360");
            options.setRelease("3.2.0");
            options.setTracesSampleRate(1.0);
        });

        new Metrics(this, 4174);

        // TODO: (19.02 23:12) NPCs temporarily disabled
//        npcBuilder = new NPCBuilder(this,
//                NPCPool.builder(this)
//                        .spawnDistance(60)
//                        .actionDistance(1)
//                        .tabListRemoveTicks(20)
//                        .build());
//        try {
//            npcService = new NPCService(this);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        BukkitTasks.setPlugin(this);
        registerGsonAdapter();
        pluginDataFolder = this.getDataFolder().toPath();
        blockHighlightTimer = new BlockHighlightTimer();

        this.getServer().getPluginManager().registerEvents(this, this);
        try {
            mainConfig = new Configuration("config.conf", this.getDataFolder().toPath(), this);
            String languageFile = "lang_" + mainConfig.get().node("languageFile").getString();
            language = new Language(languageFile, this.getDataFolder().toPath(), this);
        } catch (IOException exception) {
            Logger.error("Can't loading configurations. Disable plugin. Error message: {0}", exception, exception.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }

        ActionSerializer.init();
        database = new MySQL(mainConfig, this);

        try {
            eggService = new EasterEggService(this, language, database);
        } catch (IOException exception) {
            Logger.error("Can't initialize EasterEgg service. Disable plugin. Error message: {0}", exception, exception.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
        registerListeners();
        registerAikarCommands();
        registerServices();

        eggService.reloadAllEggs();
        try {
            database.testDataSource();
        } catch (SQLException exception) {
            Logger.error(exception.getMessage(), exception);
        }
    }

    private void registerServices() {

        RegisteredServiceProvider<Economy> registration = getServer().getServicesManager().getRegistration(Economy.class);

        if (registration != null) {
            economy = registration.getProvider();
        } else {
            Logger.error("Economy plugin not installed!");
        }

        getServer().getServicesManager().register(EasterEggService.class, eggService, this, ServicePriority.Normal);

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new EggsPlaceholder().register();
            Logger.info("PlaceholderAPI enabled, registered our placeholders");
        } else {
            Logger.info("PlaceholderAPI disabled, placeholders not registered");
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BlocksListener(), this);
        getServer().getPluginManager().registerEvents(new DamageListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractEntityListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

    }

    private void registerAikarCommands() {

        commandManager = new PaperCommandManager(this);

        commandManager.getLocales().addMessageStrings(Locales.ENGLISH, Map.of("acf-core.error_prefix", "{message}"));
        commandManager.getLocales().addMessageStrings(Locales.ENGLISH, Map.of("acf-core.invalid_syntax", "<c2>{command}</c2> <c3>{syntax}</c3>"));
        commandManager.enableUnstableAPI("help");

        commandManager.getCommandCompletions().registerCompletion("playerList", c -> {
            CommandSender sender = c.getSender();
            if (sender.hasPermission("eastereggs.admin")) {
                return Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toList();
            }
            return null;
        });
        commandManager.getCommandCompletions().registerCompletion("categoryList", c -> {
            CommandSender sender = c.getSender();
            if (sender.hasPermission("eastereggs.admin")) {
                return eggService.getAllCategoriesValue().stream()
                        .map(EasterEggCategory::getShortCategoryName)
                        .toList();
            }
            return null;
        });
        commandManager.getCommandCompletions().registerCompletion("actionList", c -> {
            CommandSender sender = c.getSender();
            if (sender.hasPermission("eastereggs.admin")) {
                return TokensProvider.getActionTokens().keySet().stream().toList();
            }
            return null;
        });

        commandManager.getCommandConditions().addCondition(String.class, "action", (c, exec, actionName) -> {
            if (actionName == null) {
                return;
            }
            if (!TokensProvider.getActionTokens().containsKey(actionName)) {
                throw new ConditionFailedException(language.getSingleMessage("action", "not_exist"));
            }
        });

        // check if category not exist or drop error
        commandManager.getCommandConditions().addCondition(String.class, "categoryNotExist", (c, exec, categoryName) -> {
            if (eggService.getAllCategories().containsKey(categoryName)) {
                throw new ConditionFailedException(language.getSingleMessage("category", "create", "taken"));
            }
        });

        commandManager.getCommandConditions().addCondition(String.class, "categoryExist", (c, exec, categoryName) -> {
            if (!eggService.getAllCategories().containsKey(categoryName)) {
                throw new ConditionFailedException(language.getSingleMessage("category", "not_exist"));
            }
        });

        commandManager.getCommandConditions().addCondition("editMode", (context) -> {
            BukkitCommandIssuer issuer = context.getIssuer();
            if (issuer.isPlayer()) {
                Player player = issuer.getPlayer();
                if (!eggService.getEditor().isEditMode(player.getUniqueId())) {
                    throw new ConditionFailedException(language.getSingleMessage("edit", "not_edit"));
                }
            }
        });

        commandManager.registerDependency(Language.class, language);
        commandManager.registerDependency(EasterEggService.class, eggService);
        commandManager.registerDependency(Gson.class, gson);

        commandManager.registerCommand(new HelpCommand());
        commandManager.registerCommand(new ReloadCommand());
        commandManager.registerCommand(new TeleportCommand());
        commandManager.registerCommand(new ListCategoryCommand());
        commandManager.registerCommand(new EditCategoryCommand());
        commandManager.registerCommand(new DeleteCategoryCommand());
        commandManager.registerCommand(new CreateCategoryCommand());
        commandManager.registerCommand(new AddActionCommand());
        commandManager.registerCommand(new RemoveActionCommand());
        commandManager.registerCommand(new StatsCommand());

        commandManager.setDefaultExceptionHandler((command, registeredCommand, sender, args, t) -> {
            getLogger().warning("Error occurred while executing command " + command.getName());
            return true;
        });
    }

    private void registerGsonAdapter() {
        GsonBuilder builder = new GsonBuilder();

        TokensProvider.getActionTokens().forEach((s, typeToken) ->
                builder.registerTypeAdapter(typeToken.getType(), getDeserializer(typeToken.getType())));

        gson = builder.create();
    }

    private <T> AnnotatedDeserializer<T> getDeserializer(T type) {
        return new AnnotatedDeserializer<T>();
    }

    @Override
    public void onDisable() {
        Util.sendStopToAll();
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (eggService.getEditor().isEditMode(player.getUniqueId()))
                eggService.stopEditCategory(player);
        });
    }

    public void reload() {

        try {
            eggService.reloadAllEggs();
            language.reload();
            mainConfig.load();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("Error while reloading plugin: {0}", e, e.getMessage());
        }
    }
}
