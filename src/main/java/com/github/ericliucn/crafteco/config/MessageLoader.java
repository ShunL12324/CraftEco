package com.github.ericliucn.crafteco.config;

import com.github.ericliucn.crafteco.Main;
import com.github.ericliucn.crafteco.utils.ComponentUtil;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public class MessageLoader {

    public static MessageLoader instance;

    private Properties message;
    private final Path configDir;

    public MessageLoader(final Path configDir) throws IOException {
        instance = this;
        this.configDir = configDir;
        if (!Files.exists(configDir)){
            Files.createDirectory(configDir);
        }

        if (!Files.exists(configDir.resolve("message.properties"))){
            Optional<Asset> asset = Sponge.assetManager().asset(Main.instance.getContainer(), "message.properties");
            if (asset.isPresent()){
                asset.get().copyToDirectory(configDir);
            }else {
                Main.instance.printLog(Level.ERROR, "Cannot copy default messages file");
            }
        }

        // try load
        load();
    }

    private void load() throws IOException {
        message = new Properties();
        message.load(Files.newBufferedReader(configDir.resolve("message.properties"), StandardCharsets.UTF_8));
    }

    public Component getMessage(String key){
        return ComponentUtil.toComponent(message.getProperty(key));
    }


}
