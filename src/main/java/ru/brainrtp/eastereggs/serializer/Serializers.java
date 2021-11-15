package ru.brainrtp.eastereggs.serializer;

import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import ru.brainrtp.eastereggs.data.action.Actions;

public final class Serializers {

    private Serializers() { }

    public static void init() {
        TypeSerializerCollection serializerCollection = TypeSerializerCollection.defaults();
        serializerCollection.childBuilder().register(TypeToken.get(Actions.class), new Actions.Serializer());

    }

}
