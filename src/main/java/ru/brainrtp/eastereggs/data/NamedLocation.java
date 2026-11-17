package ru.brainrtp.eastereggs.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.Nullable;

// world kept by name and resolved on use, so worlds loaded after the plugin (Multiverse) work
public record NamedLocation(String world, double x, double y, double z) {

  public static NamedLocation of(Location location) {
    return new NamedLocation(
        location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
  }

  public NamedLocation toBlock() {
    return new NamedLocation(
        world, Location.locToBlock(x), Location.locToBlock(y), Location.locToBlock(z));
  }

  // null while the world is not loaded
  public @Nullable Location toLocation() {
    World loaded = Bukkit.getWorld(world);
    return loaded == null ? null : new Location(loaded, x, y, z);
  }
}
