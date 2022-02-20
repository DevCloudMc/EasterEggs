package ru.brainrtp.eastereggs.services;

import api.logging.Logger;
import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.NPCPool;
import io.leangen.geantyref.TypeToken;
import org.bukkit.plugin.Plugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import ru.brainrtp.eastereggs.EasterEggs;
import ru.brainrtp.eastereggs.configuration.Configuration;
import ru.brainrtp.eastereggs.data.NPCData;
import ru.brainrtp.eastereggs.data.eggs.EasterEgg;
import ru.brainrtp.eastereggs.protocol.npcs.NPCBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NPCService {

    private final Configuration config;

    private final List<Integer> npcs = new ArrayList<>();

    private final NPCBuilder npcBuilder;
    private final NPCPool npcPool;

    public NPCService(Plugin plugin) throws Exception {
        npcPool = NPCPool.builder(plugin)
                .spawnDistance(60)
                .actionDistance(1)
                .tabListRemoveTicks(20)
                .build();
        this.npcBuilder = new NPCBuilder(plugin, npcPool);

        config = new Configuration("npc.conf", EasterEggs.getPluginDataFolder());
        loadAllNPC();
    }

    public void addNPC(int id) {
        npcs.add(id);
    }

    public void createNPC(NPCData npcData) {
        NPC npc = npcBuilder.appendNPC(npcData);
        npcData.setEntityId(npc.getEntityId());
        addNPC(npc.getEntityId());

        try {
//            config.get().node(npc.getUUID().toString()).set(TypeToken.get(INPC.class), npc);
            // TODO: (12.02 19:0) rofl is that node name == entityId, and the NPCData already has an Integer entityId
            Logger.info("Added a new NPC with id {0}", npc.getEntityId());
            config.get().node(npc.getEntityId()).set(TypeToken.get(NPCData.class), npcData);
//            config.get().node(npc.getEntityId()).set(npcData);
            config.save();
        } catch (SerializationException e) {
            e.printStackTrace();
        }
    }

    public void deleteNPC(NPCData npcData) {
        npcs.remove(npcData.getEntityId());
        try {
//            config.get().node(npc.getUUID().toString()).set(null);
            config.get().node(npcData.getEntityId()).set(null);
        } catch (SerializationException e) {
            e.printStackTrace();
        }
        config.save();

        List<EasterEgg> eggs = EasterEggs.getEggService().getAllEggs();

        for (EasterEgg egg : eggs) {
            if (egg.getLocation().equals(npcData.getLocation())) {
                EasterEggs.getEggService().deleteEgg(egg.getCategory(), egg);
            }
        }
    }

    public NPC getNPC(int entityId) {

        if (npcPool.getNpc(entityId).isPresent())
            return npcPool.getNpc(entityId).get();
        else
            return null;

//        return npcs.get(entityID);
//        for(INPC npc : npcs.values()){
//            if(npc.getEntityId() == entityID){
//                return npc;
//            }
//        }

//        return null;
    }

//    public INPC getNPC(String name){
//        for(INPC npc : npcs.values()){
//            if(npc.getName().equals(name)){
//                return npc;
//            }
//        }
//
//        return null;
//    }

    public Collection<NPC> getAllNPC() {
        return npcPool.getNPCs();
    }

    public void loadAllNPC() {
        int count = 0;

        npcs.forEach(npcPool::removeNPC);

        npcs.clear();
        config.load();

        try {
            Collection<? extends CommentedConfigurationNode> list = config.get().childrenMap().values();

            for (CommentedConfigurationNode node : list) {
                NPCData npcData = node.get(TypeToken.get(NPCData.class));
                Logger.info("NPC data is {0}", npcData.getName());
                npcBuilder.appendNPC(npcData);
                npcs.add(npcData.getEntityId());
                count++;
            }
        } catch (SerializationException e) {
            e.printStackTrace();
        }

        Logger.info("Loaded {0} NPCs", count);
    }

//    public void changeSkin(INPC npc, String target){
//        new Thread(()->{
//            String uuid = MojangAPI.getUUID(target);
//            Skin skin = MojangAPI.getPlayerSkin(uuid);
//
//            if(skin != null){
//                npc.setSkin(skin);
//                npc.destroy();
//                npc.spawn();
//
//                try{
//                    config.get().node(npc.getUUID().toString()).set(TypeToken.get(INPC.class), npc);
//                    config.save();
//                } catch (SerializationException e){
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }


}