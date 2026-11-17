package ru.brainrtp.eastereggs.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

class CommandResolveTest {

  private static class Stub extends Command {
    @Override
    public void execute(CommandSender sender, String[] args) {}

    @Override
    public List<String> onTabComplete(
        CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
      return null;
    }
  }

  @Test
  void unknownWordBetweenSubcommandsStopsAtTheParent() {
    Command root = new Stub();
    Command action = new Stub();
    Command add = new Stub();
    root.addSub("action", action.addSub("add", add));

    Command.Match match = root.resolve(new String[] {"action", "foo", "add"});

    assertSame(action, match.command());
    assertEquals(1, match.depth());
    assertSame(add, root.resolve(new String[] {"action", "add", "1"}).command());
    assertSame(root, root.resolve(new String[] {"nope"}).command());
  }
}
