package ru.brainrtp.eastereggs.data.action;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ActionCommandTest {

  @Test
  void replacesPlayerAndStripsOneLeadingSlash() {
    assertEquals(
        "ban Steve now Steve", ActionCommand.prepare("/ban %player% now %player%", "Steve"));
    assertEquals("say hi", ActionCommand.prepare("say hi", "Steve"));
  }
}
