package ru.brainrtp.eastereggs.protocol.text;

import com.google.gson.JsonArray;
import ru.brainrtp.eastereggs.util.text.Colors;

public class TextGroup {

    private final JsonArray root;

    public TextGroup(Text... text) {
        root = new JsonArray();

        if (text.length > 0) {
            root.add(Colors.of(text[0].getText()));
        }

        for (Text t : text) {
            root.add(t.toJson());
        }
    }

    public TextGroup add(Text text) {
        root.add(text.toJson());
        return this;
    }

    public TextGroup add(String text) {
        return add(new Text(text));
    }

    public static TextGroup of(Text... text) {
        return new TextGroup(text);
    }

    public Text toText() {
        return new Text("").setExtra(toJson());
    }

    public JsonArray toJson() {
        return root;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

}
