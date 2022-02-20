package ru.brainrtp.eastereggs.protocol.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import ru.brainrtp.eastereggs.protocol.text.actions.TextAction;
import ru.brainrtp.eastereggs.util.text.Colors;

public class Text {

    private final String text;
    private final JsonObject root;
    private JsonArray extra;

    public Text(String text) {
        this.text = text;
        root = new JsonObject();
        root.addProperty("text", Colors.of(text));
    }

    public Text(String text, BaseComponent... components) {
        this.text = text;
        String jsonString = ComponentSerializer.toString(components);
        root = new JsonParser().parse(jsonString).getAsJsonObject();

        if (root.has("extra")) {
            extra = root.get("extra").getAsJsonArray();
        }
    }

    public String getText() {
        return text;
    }

    public static Text of(String text) {
        return new Text(text, TextComponent.fromLegacyText(text));
    }

    public Text add(String text) {
        return add(new Text(text));
    }

    public Text add(Text text) {
        if (extra == null) {
            extra = new JsonArray();
        }

        extra.add(text.toJson());
        return this;
    }

    public Text onHover(TextAction action) {
        root.add("hoverEvent", action.toJson());
        return this;
    }

    public Text onClick(TextAction action) {
        root.add("clickEvent", action.toJson());
        return this;
    }

    public Text setExtra(JsonArray extra) {
        this.extra = extra;
        return this;
    }

    public JsonObject toJson() {
        if (extra != null) {
            root.add("extra", extra);
        }
        return root;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public BaseComponent[] toBaseComponent() {
        return ComponentSerializer.parse(toJson().toString());
    }

    public String toPlainText() {
        return TextComponent.toPlainText(toBaseComponent());
    }
}
