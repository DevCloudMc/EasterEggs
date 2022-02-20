package ru.brainrtp.eastereggs.services;

import api.logging.Logger;
import io.leangen.geantyref.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.brainrtp.eastereggs.configuration.Configuration;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.EditSession;
import ru.brainrtp.eastereggs.data.EggTypes;
import ru.brainrtp.eastereggs.data.action.ActionFirework;
import ru.brainrtp.eastereggs.data.action.ActionMessage;
import ru.brainrtp.eastereggs.data.action.Actions;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.storage.database.Database;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class EasterEggService {

    private final Path eggsPath;
    private final Map<String, EasterEggCategory> categories = new HashMap<>();

    private final EasterEggEditor editor;
    private final PlayerEggsService playerEggsService;
    private final Language language;

    public EasterEggEditor getEditor() {
        return editor;
    }

    public PlayerEggsService getPlayerService() {
        return playerEggsService;
    }

    // TODO: (13.02 20:43) Language is static :)
    public EasterEggService(Plugin plugin, Language language, Database database) throws IOException {
        this.eggsPath = Paths.get(plugin.getDataFolder() + File.separator + "eggs");
        this.language = language;
        this.editor = new EasterEggEditor(language);
        this.playerEggsService = new PlayerEggsService(database);

        if (!Files.exists(eggsPath)) {
            Files.createDirectory(eggsPath);
        }
    }

    /**
     * Get registered egg category
     *
     * @param shortCategoryName Name of required type
     * @return Optional<EasterEggCategory> if type is exist or Optional.EMPTY otherwise
     **/
    public Optional<EasterEggCategory> getCategory(String shortCategoryName) {
        return Optional.ofNullable(categories.get(shortCategoryName));
    }

    /**
     * Return easter egg by location
     *
     * @param location Location of egg
     * @return EasterEgg object in Optional wrapper or Optional.EMPTY
     **/
    public Optional<EasterEgg> getEasterEgg(Location location) {
        List<EasterEgg> eggs = getAllEggs();

        for (EasterEgg egg : eggs) {
            if (egg.getLocation().equals(location)) {
                Optional<EasterEgg> easterEgg = Optional.of(egg);
                return Optional.of(egg);
            }
        }

        return Optional.empty();
    }

    /**
     * Get list of all eggs
     *
     * @return List of registered eggs
     **/
    public List<EasterEgg> getAllEggs() {
        List<EasterEgg> eggs = new ArrayList<>();

        for (EasterEggCategory c : categories.values()) {
            eggs.addAll(c.getEggs().values());
        }

        return eggs;
    }

    /**
     * Get list of easter eggs with type filter
     *
     * @param type Registered egg type
     * @return List of registered eggs with required type
     **/
    public List<EasterEgg> getEggsOfType(EggTypes type) {
        List<EasterEgg> eggs = new ArrayList<>();

        for (EasterEggCategory c : categories.values()) {
            for (EasterEgg e : c.getEggs().values()) {
                if (type.equals(e.getType())) {
                    eggs.add(e);
                }
            }
        }

        return eggs;
    }

    public Collection<EasterEggCategory> getAllCategories() {
        return categories.values();
    }

    /**
     * Create new empty type of easter eggs
     *
     * @param categoryName Unique name for new egg type
     * @return true if type with this name not exist and type created or false otherwise
     **/
    public boolean createNewCategory(String categoryName) {
        if (categories.containsKey(categoryName)) {
            return false;
        }

        EasterEggCategory category = new EasterEggCategory(categoryName);
        Actions actions = new Actions();

        ActionFirework firework = new ActionFirework();
        actions.addAction(firework);

        ActionMessage message = new ActionMessage();
        message.setTitle("&aFinish!");
        message.setSubtitle("&eyou have found them all :)");
        message.setFadeIn(10);
        message.setFadeOut(10);
        message.setStay(50);
        actions.addAction(message);

        category.setFinishAction(actions);
        category.setOutput(language.getSingleMessageWithoutPrefix("player", "stats", "info"));

        categories.put(categoryName, category);
        createCategoryFile(categoryName);
        return true;
    }

    private void createCategoryFile(String categoryName) {
        try {
            Path filePath = Paths.get(eggsPath.toString(), categoryName + ".conf");
            Files.createFile(filePath);

            Configuration config = new Configuration(categoryName + ".conf", filePath.getParent());

            categories.get(categoryName).setConfig(config);
            categories.get(categoryName).save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create new easter egg in specific category
     *
     * @param categoryName Name of the category
     * @param egg          Easter egg object
     **/
    public void createNewEgg(String categoryName, EasterEgg egg) {
        Optional<EasterEggCategory> category = getCategory(categoryName);

        if (category.isPresent()) {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                if (!category.get().getEggs().containsKey(i)) {
                    egg.setId(i);
                    break;
                }
            }

            category.get().addEgg(egg);
            category.get().save();

            Collection<EditSession> sessions = editor.getSessions().values();

            for (EditSession session : sessions) {
                if (session.getEditableCategory().equals(categoryName)) {
                    session.addNewEgg(egg);
                }
            }


        }
    }

    /**
     * Delete easter egg from specific category
     *
     * @param category Name of the category
     * @param egg      Easter egg
     **/
    public void deleteEgg(String category, EasterEgg egg) {
        Optional<EasterEggCategory> categ = getCategory(category);

        if (categ.isPresent()) {
            Collection<EditSession> sessions = editor.getSessions().values();

            for (EditSession session : sessions) {
                if (session.getEditableCategory().equals(category)) {
                    session.removeHologram(egg.getId());
                }
            }

            categ.get().removeEgg(egg);
        }
    }

    /**
     * Delete category with all eggs
     *
     * @param categoryName Name of the category
     * @return true if delete success and false otherwise
     **/
    public boolean deleteCategory(String categoryName) {
        Optional<EasterEggCategory> category = getCategory(categoryName);

        if (category.isPresent()) {
            Path filePath = Paths.get(eggsPath.toString(), categoryName + ".conf");
            try {
                Files.delete(filePath);
            } catch (IOException e) {
                return false;
            }

            categories.remove(categoryName);
            playerEggsService.removeFromDatabase(category.get());
            editor.endEditSessionByCategory(categoryName);
            return true;
        }

        return false;
    }

    /**
     * Add egg to player database
     **/
    public void addEggToPlayer(Player player, EasterEgg egg) {
        playerEggsService.addEggToPlayer(player.getUniqueId(), egg);

        EasterEggCategory category = getCategory(egg.getCategory()).get();
        boolean isCompleteCategory = playerEggsService.isCompleteCategory(player.getUniqueId(), category);
        if (isCompleteCategory) {
            category.finish(player);
        }
    }

    /**
     * Full reload all easter eggs from configs
     **/
    public void reloadAllEggs() {
        categories.clear();

        try {
            FileFilter fileFilter = file -> !file.isDirectory() && file.getName()
                    .endsWith(".conf");

            File[] files = eggsPath.toFile().listFiles(fileFilter);

            for (File file : files) {
                Configuration config = new Configuration(file.getName(), file.toPath().getParent());
                EasterEggCategory category = config.get().get(TypeToken.get(EasterEggCategory.class));

                category.setConfig(config);
                categories.put(category.getShortCategoryName(), category);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        categories.values().forEach(category -> {
            int count = category.getEggs().size();
            Logger.info("Loaded {0} eggs in category {1}", count, category.getTitle());
        });
        Bukkit.getOnlinePlayers().forEach(player -> playerEggsService.removePlayerData(player.getUniqueId()));
        Bukkit.getOnlinePlayers().forEach(player -> playerEggsService.loadPlayerData(player.getUniqueId()));
    }


    public void stopEditCategory(Player player) {
        String category = getEditor().getEditCategory(player.getUniqueId());
        getEditor().endEditSession(player.getUniqueId());
        // TODO: (13.02 16:6) Переделать на PAPI
        player.sendMessage(language.getSingleMessage("edit", "end").replace("{category}", category));
    }

    public void startEditCategory(Player player, String categoryTitle) {
        if (getCategory(categoryTitle).isPresent()) {
            getEditor().startEditSession(player.getUniqueId(), categoryTitle);
            player.sendMessage(language.getSingleMessage("edit", "start").replace("{category}", categoryTitle));
            return;
        }
        player.sendMessage(language.getSingleMessage("category", "not_exist"));
    }

}
