package ru.brainrtp.eastereggs.serializer;

import io.leangen.geantyref.TypeToken;
import ru.brainrtp.eastereggs.data.EggTypes;
import ru.brainrtp.eastereggs.data.action.*;
import ru.brainrtp.eastereggs.data.eggs.EggBlock;
import ru.brainrtp.eastereggs.data.eggs.EggEntity;
import ru.brainrtp.eastereggs.providers.TokensProvider;

public final class ActionSerializer {

    private ActionSerializer(){}

    public static void init() {
        TokensProvider.registerAction("message", TypeToken.get(ActionMessage.class));
        TokensProvider.registerAction("money", TypeToken.get(ActionMoney.class));
        TokensProvider.registerAction("sound", TypeToken.get(ActionSound.class));
        TokensProvider.registerAction("commands", TypeToken.get(ActionCommand.class));
        TokensProvider.registerAction("firework", TypeToken.get(ActionFirework.class));
        TokensProvider.registerEgg(EggTypes.BLOCK.getStringType(), TypeToken.get(EggBlock.class));
        TokensProvider.registerEgg(EggTypes.ENTITY.getStringType(), TypeToken.get(EggEntity.class));
    }
}