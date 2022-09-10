package ru.brainrtp.eastereggs.util.json;

import api.logging.Logger;
import com.google.gson.*;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class AnnotatedDeserializer<T> implements JsonDeserializer<T> {

    public T deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        T pojo = new Gson().fromJson(je, type);

        Field[] fields = pojo.getClass().getDeclaredFields();
        for (Field f : fields) {
            if (f.getAnnotation(JsonRequired.class) != null) {
                try {
                    f.setAccessible(true);
                    if (f.get(pojo) == null) {
                        throw new JsonParseException("Missing field in JSON: " + f.getName());
                    }
                } catch (IllegalArgumentException | IllegalAccessException | JsonParseException exception) {
                    Logger.error(exception.getMessage(), exception);
                }
            }
        }
        return pojo;

    }
}