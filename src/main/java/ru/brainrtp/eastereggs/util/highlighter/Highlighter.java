package ru.brainrtp.eastereggs.util.highlighter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.inventivetalent.glow.GlowAPI;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.configuration.Language;
import ru.brainrtp.eastereggs.data.eggs.EggEntity;
import ru.brainrtp.eastereggs.protocol.hologram.Hologram;
import ru.brainrtp.eastereggs.protocol.hologram.Holograms;
import ru.brainrtp.eastereggs.util.highlighter.blockhighlight.Animation;
import ru.brainrtp.eastereggs.util.highlighter.blockhighlight.BlockHighlightTimer;
import ru.brainrtp.eastereggs.util.highlighter.blockhighlight.RunningAnimation;
import ru.brainrtp.eastereggs.util.highlighter.blockhighlight.Util;

public class Highlighter implements IHighlighter {

    private Hologram hologram;
    private Entity entity;
    private final Language language = EasterEggs.getLanguage();
    private int eggId;
    private Animation animation;
    private RunningAnimation runningAnimation;
    private final BlockHighlightTimer blockHighlightTimer = EasterEggs.getBlockHighlightTimer();

    public Highlighter(Animation animation, Player player) {
        this.animation = animation;
        startBlock(player);
    }

    public Highlighter(Location location, EggEntity easterEgg, Player player) {
        this.eggId = easterEgg.getId();
        startEntity(location, easterEgg, player);
    }

    @Override
    public void startBlock(Player player) {
        runningAnimation = blockHighlightTimer.startAnimation(player, animation);
    }

    @Override
    public void startEntity(Location location, EggEntity easterEgg, Player player) {
        entity = Bukkit.getEntity(easterEgg.getEntityUUID());

        Bukkit.getScheduler().runTaskLater(EasterEggs.getPlugin(), () -> GlowAPI.setGlowing(entity, GlowAPI.Color.GREEN, player), 2);
        location.add(0, 0.2, 0);
        if (EntityType.ARMOR_STAND.equals(entity.getType())) {
            ArmorStand armorStand = (ArmorStand) entity;
            location = armorStand.getEyeLocation();
//            location.add(0, armorStand.getHeadPose().getY() - location.getY(), 0);
        }
        hologram = Holograms.create(location);
        // TODO: (13.02 18:44) сделать нормальный формат, а не через replace
        hologram.setText(language.getSingleMessageWithoutPrefix("edit", "hologram")
                .replace("{number}", String.valueOf(eggId)));
        hologram.show(player);
    }

    @Override
    public void clear(Player player) {
        if (animation != null) {
            blockHighlightTimer.stopAnimation(runningAnimation);
            Util.sendStop(player);
        } else {
            hologram.destroy();
            GlowAPI.setGlowing(entity, null, player);
        }
    }
}
