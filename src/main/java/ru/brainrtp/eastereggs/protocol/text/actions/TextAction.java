package ru.brainrtp.eastereggs.protocol.text.actions;

import com.google.gson.JsonObject;

public abstract class TextAction {

    private final String name;

    public String getName() {
        return name;
    }

    public TextAction(String name) {
        this.name = name;
    }

    public abstract JsonObject toJson();
}
