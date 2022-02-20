package ru.brainrtp.eastereggs.util.highlighter.blockhighlight;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

@Accessors(chain = true)
@Data
public class Animation {
    public Location location;
    public int viewDistanceSquared;
    public final List<BlockHighlight> blockHighlightList = new ArrayList<>();

    public void addBlock(BlockHighlight blockHighlight) {
        blockHighlightList.add(blockHighlight);
    }

}
