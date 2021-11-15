package ru.brainrtp.eastereggs;


import api.logging.Logger;
import api.logging.handlers.JULHandler;
import io.leangen.geantyref.TypeToken;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.dependency.Libraries;
import org.bukkit.plugin.java.annotation.dependency.Library;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import ru.brainrtp.eastereggs.configuration.Configuration;
import ru.brainrtp.eastereggs.data.action.Action;
import ru.brainrtp.eastereggs.data.action.ActionMessage;
import ru.brainrtp.eastereggs.data.action.Actions;
import ru.brainrtp.eastereggs.serializer.ActionSerializer;
import ru.brainrtp.eastereggs.serializer.Serializers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@org.bukkit.plugin.java.annotation.plugin.Plugin(name = "EasterEggs", version = "3.0")
//@Dependency("ProtocolLib")
//@Dependency("Vault")
//@SoftDependency("PlaceholderAPI")
@Author("BrainRTP")
@Libraries({@Library("org.spongepowered:configurate-hocon:4.1.1")})
//@Libraries({@Library("com.google.guava:guava:30.1.1-jre"),
//        @Library("com.typesafe:config:1.4.1"),
//        @Library("ninja.leaping.configurate:configurate-core:3.7.1"),
//        @Library("ninja.leaping.configurate:configurate-hocon:3.7.1")})
@Commands(@Command(name = "eastereggs", desc = "EasterEggs command", aliases = {"ee"}))
@ApiVersion(ApiVersion.Target.v1_17)
public class EasterEggs extends JavaPlugin {

    private Configuration configuration;

    @Override
    public void onEnable() {
        Logger.init(new JULHandler(getLogger()));
        Logger.info("Starting...");
        try {
            configuration = new Configuration("config.conf", this.getDataFolder().toPath(), this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Serializers.init();
        ActionSerializer.init();

        try {
            Path eggsPath = Paths.get(this.getDataFolder() + File.separator + "eggs");
            Path filePath = Paths.get(eggsPath.toString(), "category2.conf");
//            Files.createFile(filePath);

            Configuration ee = new Configuration("category2.conf", filePath.getParent());
            Action actionMessage = ee.get().node("0", "actions", "message").get(TypeToken.get(ActionMessage.class));
            Action actionMessage2 = ee.get().node("0", "actions").get(TypeToken.get(ActionMessage.class));
//            Actions actions = ee.get().node("0", "actions").get(TypeToken.get(Actions.class));
//            actions.iterator().forEachRemaining(Action::test);
            actionMessage.test();
            actionMessage2.test();
            System.out.println("ee.get().node(\"message\", \"messages\").getString() = " + ee.get().node("0", "actions", "message", "messages").getString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // load config.conf
        // setup database. use config settings.
        // load language.conf
        // serialize EasterEggs configurations.
        // register services (dependency plugins)
        // register listeners
        // register commands
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
