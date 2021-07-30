package com.github.ericliucn.crafteco;

import com.github.ericliucn.crafteco.command.Commands;
import com.github.ericliucn.crafteco.config.ConfigLoader;
import com.github.ericliucn.crafteco.config.MessageLoader;
import com.github.ericliucn.crafteco.eco.CraftEcoService;
import com.github.ericliucn.crafteco.eco.CraftResult;
import com.github.ericliucn.crafteco.handler.DBLoader;
import com.google.inject.Inject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.*;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The main class of your Sponge plugin.
 *
 * <p>All methods are optional -- some common event registrations are included as a jumping-off point.</p>
 */
@Plugin("crafteco")
public class Main {

    public static Main instance;

    private final PluginContainer container;
    private final Logger logger;
    private CraftEcoService craftEcoService;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    @Inject
    Main(final PluginContainer container, final Logger logger) {
        instance = this;
        this.container = container;
        this.logger = logger;
    }

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) throws IOException {
        new ConfigLoader(configDir);
        new MessageLoader(configDir);
        new DBLoader(configDir);
        craftEcoService = new CraftEcoService();
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) throws IOException {
    }

    @Listener
    public void onRegisterService(final ProvideServiceEvent<EconomyService> event){
        event.suggest(() -> craftEcoService);
    }

    @Listener
    public void onServerStopping(final StoppingEngineEvent<Server> event) {

    }

    @Listener
    public void onRegisterBuilder(final RegisterBuilderEvent event){
        event.register(CraftResult.Builder.class, CraftResult.Builder::new);
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.container, Commands.pay, "pay");
        event.register(this.container, Commands.test, "test");
    }

    public void printLog(Level level, String content){
        this.logger.log(level, content);
    }

    public PluginContainer getContainer() {
        return container;
    }

    public CraftEcoService getCraftEcoService() {
        return craftEcoService;
    }
}
