package ru.brainrtp.eastereggs.serializer;

import io.leangen.geantyref.TypeToken;
import ru.brainrtp.eastereggs.data.action.ActionMessage;
import ru.brainrtp.eastereggs.providers.TokensProvider;

public final class ActionSerializer {

    private ActionSerializer(){}

    public static void init(){
//        com.google.common.reflect.TypeToken.of(ActionAddGroup.class), new ActionAddGroup.Serializer());
        TokensProvider.registerAction("message", TypeToken.get(ActionMessage.class), new ActionMessage.Serializer());
    }
}