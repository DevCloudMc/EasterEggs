package ru.brainrtp.eastereggs.util.highlighter;

import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.data.EggTypes;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.data.eggs.EggEntity;
import ru.brainrtp.eastereggs.util.highlighter.blockhighlight.Animation;
import ru.brainrtp.eastereggs.util.highlighter.blockhighlight.BlockHighlight;

import java.awt.*;

public class Highlighters {

    public static Highlighter create(EasterEgg easterEgg, Player player) {
        if (EggTypes.BLOCK.equals(easterEgg.getType())) {
            Animation animation = new Animation()
                    .setLocation(player.getLocation())
                    .setViewDistanceSquared(2048);
            BlockHighlight blockHighlight = new BlockHighlight(
                    easterEgg.getLocation(),
                    hex2Rgb("#37db97", 100).getRGB(),
                    EasterEggs.getLanguage().getSingleMessageWithoutPrefix("edit", "hologram")
                            .replace("{number}", String.valueOf(easterEgg.getId())),
                    250
            );
            animation.addBlock(blockHighlight);
            return new Highlighter(animation, player);
        } else {
            return new Highlighter(easterEgg.getLocation().clone(), (EggEntity) easterEgg, player);
        }
    }

    public static Color hex2Rgb(String colorStr, int transparency) {
        return new Color(
                Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16), transparency);
    }
}
