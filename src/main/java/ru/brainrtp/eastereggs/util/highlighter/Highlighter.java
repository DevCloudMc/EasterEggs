package ru.brainrtp.eastereggs.util.highlighter;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.data.EggTypes;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.data.eggs.EggEntity;
import ru.brainrtp.eastereggs.util.text.Colors;

public class Highlighter {

  private final List<Entity> displays = new ArrayList<>();
  private Entity glowingEntity;

  public Highlighter(EasterEgg easterEgg, Player player) {
    if (EggTypes.BLOCK.equals(easterEgg.getType())) {
      startBlock(easterEgg, player);
    } else {
      startEntity((EggEntity) easterEgg, player);
    }
  }

  private void startBlock(EasterEgg easterEgg, Player player) {
    Location location = easterEgg.getLocation().toLocation();
    if (location == null) {
      return;
    }
    BlockDisplay block =
        location
            .getWorld()
            .spawn(
                location.toBlockLocation(),
                BlockDisplay.class,
                display -> {
                  display.setBlock(location.getBlock().getBlockData());
                  display.setGlowing(true);
                  display.setGlowColorOverride(Color.fromRGB(0x37DB97));
                  hideByDefault(display);
                });
    displays.add(block);
    showHologram(location.toBlockLocation().add(0.5, 1.2, 0.5), easterEgg, player);
    player.showEntity(EasterEggs.getPlugin(), block);
  }

  private void startEntity(EggEntity easterEgg, Player player) {
    Location location = easterEgg.getLocation().toLocation();
    if (location == null) {
      return;
    }
    location.add(0, 0.2, 0);
    glowingEntity = Bukkit.getEntity(easterEgg.getEntityUUID());
    if (glowingEntity != null) {
      glowingEntity.setGlowing(true);
      if (glowingEntity instanceof ArmorStand armorStand) {
        location = armorStand.getEyeLocation();
      }
    }
    showHologram(location, easterEgg, player);
  }

  private void showHologram(Location location, EasterEgg easterEgg, Player player) {
    TextDisplay hologram =
        location
            .getWorld()
            .spawn(
                location,
                TextDisplay.class,
                display -> {
                  display.text(
                      Colors.component(
                          EasterEggs.getLanguage()
                              .getSingleMessageWithoutPrefix("edit", "hologram")
                              .replace("{number}", String.valueOf(easterEgg.getId()))));
                  display.setBillboard(Display.Billboard.CENTER);
                  hideByDefault(display);
                });
    displays.add(hologram);
    player.showEntity(EasterEggs.getPlugin(), hologram);
  }

  private static void hideByDefault(Entity display) {
    display.setPersistent(false);
    display.setVisibleByDefault(false);
  }

  public void clear() {
    displays.forEach(Entity::remove);
    if (glowingEntity != null) {
      glowingEntity.setGlowing(false);
    }
  }
}
