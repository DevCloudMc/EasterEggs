package ru.brainrtp.eastereggs.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.configuration.Configuration;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.EasterEggCategory;
import ru.brainrtp.eastereggs.data.EditSession;
import ru.brainrtp.eastereggs.data.NamedLocation;
import ru.brainrtp.eastereggs.data.action.ActionFirework;
import ru.brainrtp.eastereggs.data.action.ActionMessage;
import ru.brainrtp.eastereggs.data.action.Actions;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.data.eggs.EggEntity;

public class EasterEggService {

  // the name becomes a file name and a storage key
  private static final Pattern CATEGORY_NAME = Pattern.compile("[A-Za-z0-9_-]{1,32}");

  private final Path eggsPath;
  // PlaceholderAPI reads it from its own thread
  private final Map<String, EasterEggCategory> categories = new ConcurrentHashMap<>();
  // entity eggs by entity UUID, block eggs by world name and block coordinates
  private final Map<Object, EasterEgg> eggIndex = new HashMap<>();

  private final EasterEggEditor editor;
  private final PlayerEggsService playerEggsService;
  private final Language language;

  public EasterEggEditor getEditor() {
    return editor;
  }

  public PlayerEggsService getPlayerService() {
    return playerEggsService;
  }

  public EasterEggService(Plugin plugin, Language language, PlayerEggsService playerEggsService)
      throws IOException {
    this.eggsPath = Paths.get(plugin.getDataFolder() + File.separator + "eggs");
    this.language = language;
    this.editor = new EasterEggEditor();
    this.playerEggsService = playerEggsService;

    if (!Files.exists(eggsPath)) {
      Files.createDirectory(eggsPath);
    }
  }

  /**
   * Get registered egg category
   *
   * @param shortCategoryName Name of required type
   * @return Optional<EasterEggCategory> if type is exist or Optional.EMPTY otherwise
   */
  public Optional<EasterEggCategory> getCategory(String shortCategoryName) {
    return Optional.ofNullable(categories.get(shortCategoryName));
  }

  /**
   * Return block easter egg by location
   *
   * @param location Any location inside the block
   * @return EasterEgg object in Optional wrapper or Optional.EMPTY
   */
  public Optional<EasterEgg> getEasterEgg(Location location) {
    return Optional.ofNullable(eggIndex.get(NamedLocation.of(location).toBlock()));
  }

  public Optional<EasterEgg> getEasterEgg(Entity entity) {
    return Optional.ofNullable(eggIndex.get(entity.getUniqueId()));
  }

  private static Object indexKey(EasterEgg egg) {
    return egg instanceof EggEntity entity ? entity.getEntityUUID() : egg.getLocation().toBlock();
  }

  public static boolean isValidCategoryName(String name) {
    return CATEGORY_NAME.matcher(name).matches();
  }

  public List<String> getCategoryNames() {
    return categories.values().stream().map(EasterEggCategory::getShortCategoryName).toList();
  }

  public Collection<EasterEggCategory> getAllCategories() {
    return categories.values();
  }

  /**
   * Create new empty type of easter eggs
   *
   * @param categoryName Unique name for new egg type
   * @return true if type with this name not exist and type created or false otherwise
   */
  public boolean createNewCategory(String categoryName) {
    if (!isValidCategoryName(categoryName) || categories.containsKey(categoryName)) {
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

    try {
      // fails when the file exists, even if it holds a category with another name
      Files.createFile(eggsPath.resolve(categoryName + ".conf"));
      category.setConfig(new Configuration(categoryName + ".conf", eggsPath));
    } catch (IOException e) {
      EasterEggs.getPlugin()
          .getSLF4JLogger()
          .error("Could not create the file of category {}", categoryName, e);
      return false;
    }
    category.save();
    categories.put(categoryName, category);
    return true;
  }

  /**
   * Create new easter egg in specific category
   *
   * @param categoryName Name of the category
   * @param egg Easter egg object
   */
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
      eggIndex.put(indexKey(egg), egg);

      Collection<EditSession> sessions = editor.getSessions().values();

      for (EditSession session : sessions) {
        if (session.getEditableCategory().equals(categoryName)) {
          session.addNewEgg(egg);
        }
      }
    }
  }

  // the listeners share these three steps; true when an egg was there
  public boolean findEgg(Player player, Optional<EasterEgg> egg) {
    if (egg.isEmpty()) {
      return false;
    }
    if (playerEggsService.isCompletedEgg(player.getUniqueId(), egg.get())) {
      player.sendMessage(language.getSingleMessage("egg", "find", "already"));
    } else {
      egg.get().activate(player);
    }
    return true;
  }

  public void createEgg(Player player, String category, EasterEgg egg) {
    if (eggIndex.containsKey(indexKey(egg))) {
      player.sendMessage(language.getSingleMessage("egg", "create", "already"));
      return;
    }
    createNewEgg(category, egg);
    player.sendMessage(
        language
            .getSingleMessage("egg", "create", "success")
            .replace("{egg}", String.valueOf(egg.getId())));
  }

  public void deleteEggAt(Player player, Optional<EasterEgg> found) {
    String category = editor.getEditCategory(player.getUniqueId());
    found
        .filter(e -> e.getCategory().equals(category))
        .ifPresent(egg -> {
          deleteEgg(category, egg);
          player.sendMessage(
              language
                  .getSingleMessage("egg", "delete", "success")
                  .replace("{egg}", String.valueOf(egg.getId()))
                  .replace("{category}", egg.getCategory()));
        });
  }

  /**
   * Delete easter egg from specific category
   *
   * @param category Name of the category
   * @param egg Easter egg
   */
  public void deleteEgg(String category, EasterEgg egg) {
    Optional<EasterEggCategory> categ = getCategory(category);

    if (categ.isPresent()) {
      Collection<EditSession> sessions = editor.getSessions().values();

      for (EditSession session : sessions) {
        if (session.getEditableCategory().equals(category)) {
          session.removeHighlighter(egg.getId());
        }
      }

      categ.get().removeEgg(egg);
      eggIndex.remove(indexKey(egg), egg);
      playerEggsService.deleteEgg(egg);
    }
  }

  /**
   * Delete category with all eggs
   *
   * @param categoryName Name of the category
   * @return true if delete success and false otherwise
   */
  public boolean deleteCategory(String categoryName) {
    Optional<EasterEggCategory> category = getCategory(categoryName);

    if (category.isPresent()) {
      try {
        Files.delete(category.get().getConfig().getPath());
      } catch (IOException e) {
        return false;
      }

      categories.remove(categoryName);
      category.get().getEggs().values().forEach(egg -> eggIndex.remove(indexKey(egg), egg));
      playerEggsService.deleteCategory(categoryName);
      editor.endEditSessionByCategory(categoryName);
      return true;
    }

    return false;
  }

  public void finishIfComplete(Player player, String categoryName) {
    getCategory(categoryName)
        .filter(category -> playerEggsService.isCompleteCategory(player.getUniqueId(), category))
        .ifPresent(category -> category.finish(player));
  }

  /** Full reload all easter eggs from configs */
  public void reloadAllEggs() {
    Map<String, EasterEggCategory> loaded = new HashMap<>();
    File[] files =
        eggsPath.toFile().listFiles(file -> file.isFile() && file.getName().endsWith(".conf"));

    for (File file : files == null ? new File[0] : files) {
      try {
        Configuration config = new Configuration(file.getName(), eggsPath);
        // Configuration logs a parse error itself and leaves the node null
        EasterEggCategory category =
            Objects.requireNonNull(config.get(), "not a valid HOCON file")
                .require(EasterEggCategory.class);
        String name = Objects.requireNonNull(category.getShortCategoryName(), "no category key");
        category.setConfig(config);
        EasterEggCategory other = loaded.putIfAbsent(name, category);
        if (other != null) {
          EasterEggs.getPlugin()
              .getSLF4JLogger()
              .error(
                  "Skipped {}: category {} is already loaded from {}",
                  file.getName(),
                  name,
                  other.getConfig().getFileName());
        }
      } catch (Exception e) {
        EasterEggs.getPlugin()
            .getSLF4JLogger()
            .error("Skipped {}: {}", file.getName(), e.getMessage(), e);
      }
    }

    eggIndex.clear();
    for (EasterEggCategory category : loaded.values()) {
      for (EasterEgg egg : category.getEggs().values()) {
        EasterEgg other = eggIndex.putIfAbsent(indexKey(egg), egg);
        if (other != null) {
          EasterEggs.getPlugin()
              .getSLF4JLogger()
              .warn(
                  "Egg {} of category {} cannot be found: egg {} of category {} is on the same"
                      + " block or entity",
                  egg.getId(),
                  egg.getCategory(),
                  other.getId(),
                  other.getCategory());
        }
      }
      EasterEggs.getPlugin()
          .getSLF4JLogger()
          .info("Loaded {} eggs in category {}", category.getEggs().size(), category.getTitle());
    }
    categories.clear();
    categories.putAll(loaded);
  }

  public void stopEditCategory(Player player) {
    String category = getEditor().getEditCategory(player.getUniqueId());
    getEditor().endEditSession(player.getUniqueId());
    // TODO: (21.02 15:10) I think this functionality (PAPI) should be added.
    player.sendMessage(language.getSingleMessage("edit", "end").replace("{category}", category));
  }

  public void startEditCategory(Player player, String categoryTitle) {
    if (getCategory(categoryTitle).isPresent()) {
      getEditor().startEditSession(player.getUniqueId(), categoryTitle);
      player.sendMessage(
          language.getSingleMessage("edit", "start").replace("{category}", categoryTitle));
      return;
    }
    player.sendMessage(language.getSingleMessage("category", "not_exist"));
  }
}
