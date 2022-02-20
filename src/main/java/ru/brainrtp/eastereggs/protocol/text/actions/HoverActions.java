package ru.brainrtp.eastereggs.protocol.text.actions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import ru.brainrtp.eastereggs.protocol.text.Text;

public abstract class HoverActions extends TextAction {

    private HoverActions(String name) {
        super(name);
    }

    private static class ShowText extends HoverActions {

        private final Text text;

        public ShowText(Text text) {
            super("show_text");
            this.text = text;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("action", getName());
            json.add("value", text.toJson());
            return json;
        }
    }

    private static class ShowAchievement extends HoverActions {

        private final Text text;

        public ShowAchievement(Text text) {
            super("show_achievement");
            this.text = text;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            return json;
        }
    }

    private static class ShowItem extends HoverActions {

        private final ItemStack item;

        public ShowItem(ItemStack item) {
            super("show_item");
            this.item = item;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("action", getName());

            JsonObject tag = new JsonObject();
            JsonObject display = new JsonObject();
            JsonArray lore = new JsonArray();

            for (String l : item.getItemMeta().getLore()) {
                lore.add(l);
            }

            display.addProperty("name", item.getItemMeta().getDisplayName());
            display.add("Lore", lore);
            tag.add("display", display);
            json.add("tag", tag);

            return json;
        }
    }

    private static class ShowEntity extends HoverActions {

        private final Entity entity;

        public ShowEntity(Entity entity) {
            super("show_entity");
            this.entity = entity;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("name", entity.getName());
            json.addProperty("type", entity.getType().name());
            json.addProperty("id", entity.getUniqueId().toString());
            return json;
        }
    }

    public static ShowText showText(Text text) {
        return new ShowText(text);
    }

    public static ShowAchievement showAchievement(Text text) {
        return new ShowAchievement(text);
    }

    public static ShowEntity showEntity(Entity entity) {
        return new ShowEntity(entity);
    }

    public static ShowItem showItem(ItemStack item) {
        return new ShowItem(item);
    }

}
