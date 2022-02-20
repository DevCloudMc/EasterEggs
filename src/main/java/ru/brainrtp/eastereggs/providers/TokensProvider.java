package ru.brainrtp.eastereggs.providers;

import io.leangen.geantyref.TypeToken;
import lombok.Getter;
import ru.brainrtp.eastereggs.data.action.Action;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;

import java.util.HashMap;
import java.util.Map;

public class TokensProvider {

    @Getter
    private static final Map<String, TypeToken> actionTokens = new HashMap<>();
    private static final Map<String, TypeToken> eggTypeTokens = new HashMap<>();

    public static <T> void registerAction(String key, TypeToken<T> token) {
        actionTokens.put(key, token);
    }

    public static <T> void registerEgg(String key, TypeToken<T> token) {
        eggTypeTokens.put(key, token);
    }

    public static TypeToken<? extends Action> getActionType(String key) {
        return actionTokens.get(key);
    }

    public static TypeToken<? extends EasterEgg> getEggType(String key) {
        return eggTypeTokens.get(key);
    }

}
