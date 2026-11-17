package ru.brainrtp.eastereggs.data.eggs;

import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.data.EggTypes;
import ru.brainrtp.eastereggs.data.NamedLocation;
import ru.brainrtp.eastereggs.data.action.Action;
import ru.brainrtp.eastereggs.data.action.Actions;
import ru.brainrtp.eastereggs.services.EasterEggService;
import ru.brainrtp.eastereggs.services.PlayerEggsService;

// same identity as EggKey in storage
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
public abstract class EasterEgg {

  @EqualsAndHashCode.Include private int id;

  @EqualsAndHashCode.Include private String category;

  private EggTypes type;

  private NamedLocation location;

  private Actions actions;

  public void addAction(Action action) {
    if (actions == null) {
      actions = new Actions();
    }
    // actions are saved under their type name, so a second one of a type would overwrite the first
    actions.removeAction(action.getActionTitle());
    actions.addAction(action);
  }

  public void removeAction(String actionName) {
    if (actions == null) {
      actions = new Actions();
    }
    actions.removeAction(actionName);
  }

  public void activate(Player player) {
    if (!player.hasPermission("ee.type." + category)) {
      return;
    }

    EasterEggService service = EasterEggs.getEggService();
    PlayerEggsService players = service.getPlayerService();
    UUID uuid = player.getUniqueId();
    // decided at the click: with several saves pending, only the last missing egg may finish
    boolean last =
        service
            .getCategory(category)
            .filter(c -> players.countFound(uuid, c) == c.getEggs().size() - 1)
            .isPresent();
    // runs on the main thread after the find is saved, never for a failed save
    players.addEggToPlayer(
        uuid,
        this,
        () -> {
          if (!player.isOnline()) {
            return;
          }
          if (actions != null) {
            actions.activate(player);
          }
          if (last) {
            service.finishIfComplete(player, category);
          }
        });

    //        int founded =
    // EasterEggs.getEggService().getPlayerService().getPlayerData(player.getUniqueId())
    //                .get(category).getEggs().size();
    //        int count = EasterEggs.getEggService().getCategory(category).get().getEggs().size();
    //
    //        Placeholder ph = new Placeholder();
    //        ph.addData(Placeholder.PLAYER, player.getName());
    //        ph.addData(Placeholder.CATEGORY, category);
    //        ph.addData(Placeholder.FOUNDED_EGG, founded);
    //        ph.addData(Placeholder.EGG_COUNT, count);

  }
}
