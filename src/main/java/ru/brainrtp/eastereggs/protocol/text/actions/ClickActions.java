package ru.brainrtp.eastereggs.protocol.text.actions;

import com.google.gson.JsonObject;

public abstract class ClickActions extends TextAction {

    private ClickActions(String name) {
        super(name);
    }

    private static class RunCommand extends ClickActions {

        private final String command;

        public RunCommand(String command) {
            super("run_command");
            this.command = command;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("action", getName());
            json.addProperty("value", command);
            return json;
        }
    }

    private static class OpenURL extends ClickActions {

        private final String url;

        public OpenURL(String url) {
            super("open_url");
            this.url = url;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("action", getName());
            json.addProperty("value", url);
            return json;
        }
    }

    private static class SuggestCommand extends ClickActions {

        private final String command;

        public SuggestCommand(String command) {
            super("suggest_command");
            this.command = command;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("action", getName());
            json.addProperty("value", command);
            return json;
        }
    }

    private static class ChangePage extends ClickActions {

        private final int page;

        public ChangePage(int page) {
            super("change_page");
            this.page = page;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("action", getName());
            json.addProperty("value", page);
            return json;
        }
    }

    public static RunCommand runCommand(String command) {
        return new RunCommand(command);
    }

    public static OpenURL openURL(String url) {
        return new OpenURL(url);
    }

    public static SuggestCommand suggestCommand(String command) {
        return new SuggestCommand(command);
    }

    public static ChangePage changePage(int page) {
        return new ChangePage(page);
    }
}
