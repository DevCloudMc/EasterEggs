package ru.brainrtp.eastereggs.util.highlighter.blockhighlight;

import org.bukkit.entity.Player;
import ru.brainrtp.eastereggs.util.BukkitTasks;

import java.util.ArrayList;

public class BlockHighlightTimer {
    private final ArrayList<RunningAnimation> runningAnimations = new ArrayList<>();

    public BlockHighlightTimer() {
        start();
    }

    public void start() {
        BukkitTasks.runTaskTimer(() -> runningAnimations.forEach(RunningAnimation::render), 1, 5);
    }

    public RunningAnimation startAnimation(Player pl, Animation animation) {
        RunningAnimation runningAnimation = new RunningAnimation(animation, pl);
        runningAnimations.add(runningAnimation);
        return runningAnimation;
    }

    public void stopAnimation(RunningAnimation runningAnimation) {
        runningAnimations.remove(runningAnimation);
    }
}