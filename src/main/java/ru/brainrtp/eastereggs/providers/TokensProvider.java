package ru.brainrtp.eastereggs.providers;

import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import ru.brainrtp.eastereggs.data.action.Action;

import java.util.HashMap;
import java.util.Map;

public class TokensProvider {

    private static Map<String, TypeToken> actionTokens = new HashMap<>();

    public static <T> void registerAction(String key, TypeToken<T> token, TypeSerializer<T> serializer) {
//        TypeSerializer.of(token, serializer,);
        TypeSerializerCollection.defaults().childBuilder().register(token, serializer);
//        TypeSerializerCollection.builder()
//                .register(ActionMessage.class, new ActionMessage.Serializer())
//                .build();
//        TypeSerializers.getDefaultSerializers().registerType(token, serializer);
        actionTokens.put(key, token);
    }

    public static TypeToken<? extends Action> getActionType(String key) {
        return actionTokens.get(key);
    }

}
