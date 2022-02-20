package ru.brainrtp.eastereggs.services;

import org.bukkit.Bukkit;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.EditSession;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EasterEggEditor {

    private final Language language;

    private final Map<UUID, EditSession> sessions = new HashMap<>();

    public EasterEggEditor(Language language) {
        this.language = language;
    }

    public void startEditSession(UUID uuid, String category) {
        EditSession session = new EditSession(language, Bukkit.getPlayer(uuid), category);
        sessions.put(uuid, session);
        session.start();
    }

    public void endEditSession(UUID uuid) {
        if (sessions.containsKey(uuid)) {
            sessions.get(uuid).end();
            sessions.remove(uuid);
        }
    }

    public void endEditSessionByCategory(String category) {
        for (EditSession session : sessions.values()) {
            if (session.getEditableCategory().equals(category)) {
                session.end();
                sessions.remove(session.getPlayer().getUniqueId());
            }
        }
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
