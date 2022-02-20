package ru.brainrtp.eastereggs.util.highlighter.blockhighlight;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class BlockHighlightTimer {
    private final ArrayList<RunningAnimation> runningAnimations = new ArrayList<>();

    private final Plugin plugin;

    public BlockHighlightTimer(Plugin plugin) {
        this.plugin = plugin;
        start();
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                runningAnimations.forEach(RunningAnimation::render);
            }
        }.runTaskTimer(plugin, 1, 5);
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