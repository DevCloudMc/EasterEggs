package ru.brainrtp.eastereggs.protocol.npcs;

import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.NPCPool;
import com.github.juliarn.npc.event.PlayerNPCHideEvent;
import com.github.juliarn.npc.event.PlayerNPCInteractEvent;
import com.github.juliarn.npc.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import ru.brainrtp.eastereggs.data.NPCData;
import ru.brainrtp.eastereggs.data.Skin;

import java.util.Random;
import java.util.UUID;

public class NPCBuilder implements Listener {

    private final NPCPool npcPool;

    private final Random random;

    public NPCBuilder(Plugin plugin, NPCPool npcPool) {

        this.npcPool = npcPool;
        this.random = new Random();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public NPC appendNPC(NPCData npcData) {
//        Logger.info("location {0} ", npcData.getLocation());
        return NPC.builder()
                .profile(this.createProfile(npcData))
                .location(npcData.getLocation())
                .imitatePlayer(false)
                .lookAtPlayer(false)
                // appending it to the NPC pool
                .build(this.npcPool);
    }

    public Profile createProfile(NPCData npcData) {
//        Profile profile = new Profile(UUID.fromString("fdef0011-1c58-40c8-bfef-0bdcb1495938"));
//        Profile profile = new Profile(new UUID(this.random.nextLong(), 0).toString());
        Skin skin = npcData.getSkin();
//        Logger.info("Skin value {0}", skin.getValue());
//        Logger.info("Skin signature {0}", skin.getSignature());
        Profile.Property property = new Profile.Property("textures", skin.getValue(), skin.getSignature());
        Profile profile = new Profile(new UUID(this.random.nextLong(), 0));
        profile.setProperty(property);

        // Synchronously completing the profile, as we only created the profile with a UUID.
        // Through this, the name and textures will be filled in.
        profile.complete();

        // we want to keep the textures, but change the name and UUID.
        profile.setName(npcData.getName());
        // with a UUID like this, the NPC can play LabyMod emotes!
        profile.setUniqueId(new UUID(this.random.nextLong(), 0));


//        Logger.info("Starting...");
//        Logger.info("Название имя {0}", profile.getName());
//        Logger.info("Название uuid {0}", profile.getUniqueId());
        return profile;
    }

//    /**
//     * Doing something when a NPC is shown for a certain player.
//     * Alternatively, {@link NPCBuilder.Builder#spawnCustomizer(SpawnCustomizer)} can be used.
//     *
//     * @param event The event instance
//     */
//    @EventHandler
//    public void handleNPCShow(PlayerNPCShowEvent event) {
//        NPC npc = event.getNPC();
//
//        // sending the data only to the player from the event
//        event.send(
//                // making the NPC swing its main arm
//                npc.animation()
//                        .queue(AnimationModifier.EntityAnimation.SWING_MAIN_ARM),
//                // making the NPC play the LabyMod "dab" emote.
//                npc.equipment()
//                        .queue(EquipmentModifier.CHEST, new ItemStack(Material.DIAMOND_CHESTPLATE, 1)),
//                // enabling the skin layers and letting the NPC sneak
//                npc.metadata()
//                        .queue(MetadataModifier.EntityMetadata.SKIN_LAYERS, true)
//                        .queue(MetadataModifier.EntityMetadata.SNEAKING, true));
//    }

    /**
     * Doing something when a NPC is hidden for a certain player.
     *
     * @param event The event instance
     */
    @EventHandler
    public void handleNPCHide(PlayerNPCHideEvent event) {
        Player player = event.getPlayer();
        NPC npc = event.getNPC();

        // if the player has been excluded from seeing the NPC, removing him from the excluded players
        if (event.getReason() == PlayerNPCHideEvent.Reason.EXCLUDED) {
            npc.removeExcludedPlayer(player);
        }
    }

    /**
     * Doing something when a player interacts with a NPC.
     *
     * @param event The event instance
     */
    @EventHandler
    public void handleNPCInteract(PlayerNPCInteractEvent event) {
        Player player = event.getPlayer();
        NPC npc = event.getNPC();

        // checking if the player hit the NPC
        if (event.getUseAction() == PlayerNPCInteractEvent.EntityUseAction.ATTACK) {
            player.sendMessage("Why are you hitting me? :(");
            // making the NPC look at the player
            npc.rotation()
                    .queueLookAt(player.getEyeLocation())
                    // sending the change only to the player who interacted with the NPC
                    .send(player);
        }
    }

}