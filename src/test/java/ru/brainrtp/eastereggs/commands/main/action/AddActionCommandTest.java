package ru.brainrtp.eastereggs.commands.main.action;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.leangen.geantyref.TypeToken;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.brainrtp.eastereggs.data.action.ActionCommand;
import ru.brainrtp.eastereggs.data.action.ActionMessage;

class AddActionCommandTest {

  @Test
  void readmeExamplesWithSpacesParse() throws Exception {
    ActionCommand commands =
        (ActionCommand)
            AddActionCommand.parseAction(
                "{player=[\"/home\", \"/say Hello\"], console=[\"broadcast Hi!\"]}",
                TypeToken.get(ActionCommand.class));
    ActionMessage message =
        (ActionMessage)
            AddActionCommand.parseAction(
                "{messages=[\"&eFirst\", \"&cSecond\"]}", TypeToken.get(ActionMessage.class));

    assertEquals(List.of("/home", "/say Hello"), commands.getCommands().values().iterator().next());
    assertEquals(List.of("&eFirst", "&cSecond"), message.getMessages());
  }
}
