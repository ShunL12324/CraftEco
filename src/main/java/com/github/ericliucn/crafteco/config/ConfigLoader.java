package com.github.ericliucn.crafteco.config;

import com.github.ericliucn.crafteco.Main;
import io.leangen.geantyref.TypeToken;
import org.apache.logging.log4j.Level;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigLoader {

    public static ConfigLoader instance;

    private CraftEcoConfig config;
    private final Path configFile;
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private CommentedConfigurationNode rootNode;

    public ConfigLoader(final Path configDir) throws IOException {
        instance = this;
        // create dir directory
        configFile = configDir.resolve("setting.conf");
        if (!Files.exists(configDir)){
            Files.createDirectory(configDir);
        }
        // create setting file
        if (!Files.exists(configFile)){
            Files.createDirectory(configFile);
        }
        // try load config
        this.load();
    }

    public void load() throws ConfigurateException {
        this.loader = HoconConfigurationLoader.builder()
                .path(this.configFile)
                .defaultOptions(ConfigurationOptions.defaults().shouldCopyDefaults(true))
                .build();
        this.rootNode = this.loader.load();
        this.config = this.rootNode.get(TypeToken.get(CraftEcoConfig.class), new CraftEcoConfig());
        this.save();
    }

    public void save() throws ConfigurateException {
        this.loader.save(this.rootNode);
    }

    public CraftEcoConfig getConfig() {
        return config;
    }

}
