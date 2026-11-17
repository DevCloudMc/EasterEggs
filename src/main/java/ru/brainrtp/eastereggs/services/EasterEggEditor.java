package ru.brainrtp.eastereggs.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.EditSession;

public class EasterEggEditor {

  private final Language language;

  private final Map<UUID, EditSession> sessions = new HashMap<>();

  public EasterEggEditor(Language language) {
    this.language = language;
  }

  public void startEditSession(UUID uuid, String category) {
    EditSession session = new EditSession(language, Bukkit.getPlayer(uuid), category);
    session.start();
    sessions.put(uuid, session);
  }

  public void endEditSession(UUID uuid) {
    if (sessions.containsKey(uuid)) {
      sessions.get(uuid).end();
      sessions.remove(uuid);
    }
  }

  public void endEditSessionByCategory(String category) {
    sessions
        .values()
        .removeIf(session -> {
          if (!session.getEditableCategory().equals(category)) return false;
          session.end();
          return true;
        });
  }

  public void endAllSessions() {
    sessions.values().forEach(EditSession::end);
    sessions.clear();
  }

  public String getEditCategory(UUID uuid) {
    if (sessions.containsKey(uuid)) {
      return sessions.get(uuid).getEditableCategory();
    }
    return null;
  }

  public EditSession getSession(UUID uuid) {
    return sessions.get(uuid);
  }

  public Map<UUID, EditSession> getSessions() {
    return sessions;
  }

  public boolean isEditMode(UUID uuid) {
    return sessions.containsKey(uuid);
  }
}
