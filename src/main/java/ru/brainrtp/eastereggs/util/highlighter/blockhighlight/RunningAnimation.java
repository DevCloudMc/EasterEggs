package ru.brainrtp.eastereggs.util.highlighter.blockhighlight;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class RunningAnimation {
    @NonNull
    private Animation animation;
    @NonNull
    private Player player;

    public void render() {
        if (animation.blockHighlightList.isEmpty()) {
            return;
        }

        if (!player.getWorld().equals(animation.location.getWorld())) return;

        if (player.getLocation().distanceSquared(animation.location) < animation.viewDistanceSquared) {
            animation.blockHighlightList.forEach(blockHighlight -> Util.sendBlockHighlight(player, blockHighlight));
        }

    }
}
