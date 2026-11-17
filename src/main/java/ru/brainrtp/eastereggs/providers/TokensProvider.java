package ru.brainrtp.eastereggs.providers;

import io.leangen.geantyref.TypeToken;
import java.util.Map;
import lombok.Getter;
import ru.brainrtp.eastereggs.data.EggTypes;
import ru.brainrtp.eastereggs.data.action.Action;
import ru.brainrtp.eastereggs.data.action.ActionCommand;
import ru.brainrtp.eastereggs.data.action.ActionFirework;
import ru.brainrtp.eastereggs.data.action.ActionMessage;
import ru.brainrtp.eastereggs.data.action.ActionMoney;
import ru.brainrtp.eastereggs.data.action.ActionSound;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.data.eggs.EggBlock;
import ru.brainrtp.eastereggs.data.eggs.EggEntity;

public class TokensProvider {

  @Getter
  private static final Map<String, TypeToken<? extends Action>> actionTokens =
      Map.of(
          "message", TypeToken.get(ActionMessage.class),
          "money", TypeToken.get(ActionMoney.class),
          "sound", TypeToken.get(ActionSound.class),
          "commands", TypeToken.get(ActionCommand.class),
          "firework", TypeToken.get(ActionFirework.class));

  private static final Map<String, TypeToken<? extends EasterEgg>> eggTypeTokens =
      Map.of(
          EggTypes.BLOCK.name(), TypeToken.get(EggBlock.class),
          EggTypes.ENTITY.name(), TypeToken.get(EggEntity.class));

  public static TypeToken<? extends Action> getActionType(String key) {
    return actionTokens.get(key);
  }

  public static TypeToken<? extends EasterEgg> getEggType(String key) {
    return eggTypeTokens.get(key);
  }
}
