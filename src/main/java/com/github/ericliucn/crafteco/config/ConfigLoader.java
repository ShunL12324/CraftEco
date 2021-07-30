package com.github.ericliucn.crafteco.config;

import com.github.ericliucn.crafteco.config.serializer.ComponentSerializer;
import com.github.ericliucn.crafteco.config.serializer.CraftCurrencySerializer;
import com.github.ericliucn.crafteco.eco.CraftCurrency;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

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
            Files.createFile(configFile);
        }
        // try load config
        this.load();
    }

    private ConfigurationOptions getOptions(){
        ConfigurationOptions options = ConfigurationOptions.defaults().shouldCopyDefaults(true);
        TypeSerializerCollection collection = options.serializers();
        TypeSerializerCollection.Builder builder = collection.childBuilder();
        builder.register(TypeToken.get(Component.class), new ComponentSerializer());
        builder.register(TypeToken.get(CraftCurrency.class), new CraftCurrencySerializer());
        return options.serializers(builder.build());
    }

    public void load() throws ConfigurateException {
        this.loader = HoconConfigurationLoader.builder()
                .path(this.configFile)
                .defaultOptions(getOptions())
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
