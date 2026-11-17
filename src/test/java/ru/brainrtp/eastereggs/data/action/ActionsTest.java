package ru.brainrtp.eastereggs.data.action;

import static org.junit.jupiter.api.Assertions.assertEquals;

import api.logging.Logger;
import api.logging.handlers.JULHandler;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

class ActionsTest {

  @Test
  void failingActionDoesNotStopTheNextOne() {
    Logger.init(new JULHandler(java.util.logging.Logger.getLogger("test")));
    List<String> ran = new ArrayList<>();
    Actions actions = new Actions();
    actions.addAction(
        action(() -> {
          throw new IllegalStateException("broken action");
        }));
    actions.addAction(action(() -> ran.add("second")));

    actions.activate(null);

    assertEquals(List.of("second"), ran);
  }

  private static Action action(Runnable body) {
    return new Action() {
      @Override
      public String getActionTitle() {
        return "test";
      }

      @Override
      public void activate(Player player) {
        body.run();
      }
    };
  }
}
