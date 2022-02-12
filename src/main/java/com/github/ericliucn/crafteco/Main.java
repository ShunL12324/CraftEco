package com.github.ericliucn.crafteco;

import com.github.ericliucn.crafteco.command.Commands;
import com.github.ericliucn.crafteco.config.ConfigLoader;
import com.github.ericliucn.crafteco.eco.CraftEcoService;
import com.github.ericliucn.crafteco.eco.CraftResult;
import com.github.ericliucn.crafteco.handler.DatabaseHandler;
import com.github.ericliucn.crafteco.handler.EventHandler;
import com.github.ericliucn.crafteco.handler.PapiHandler;
import com.github.ericliucn.crafteco.metric.Metrics;
import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.*;
import org.spongepowered.api.placeholder.PlaceholderParser;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

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
    Main(final PluginContainer container, final Logger logger, final Metrics.Factory factory) {
        instance = this;
        this.container = container;
        this.logger = logger;
        // the Metrics.class will check if metric enable
        factory.make(14279);
    }


    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) throws IOException, SQLException, ClassNotFoundException {
        new ConfigLoader(configDir);
        new DatabaseHandler();
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) {
        new EventHandler();
    }


    @Listener
    public void onRegisterService(final ProvideServiceEvent<EconomyService> event){
        craftEcoService = new CraftEcoService();
        event.suggest(() -> craftEcoService);
    }

    @Listener
    public void onServerStopping(final StoppingEngineEvent<Server> event) {
        CraftEcoService.instance.saveCache();
    }

    @Listener
    public void onRegisterBuilder(final RegisterBuilderEvent event){
        event.register(CraftResult.Builder.class, CraftResult.Builder::new);
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        new Commands(event);
    }

    @Listener
    public void onRegisterTransactionType(final RegisterRegistryValueEvent.GameScoped event){
        RegisterRegistryValueEvent.RegistryStep<PlaceholderParser> registry = event.registry(RegistryTypes.PLACEHOLDER_PARSER);
        registry.register(ResourceKey.of(this.container, "papi"), PapiHandler.BALANCE);
    }

    public PluginContainer getContainer() {
        return container;
    }

    public CraftEcoService getCraftEcoService() {
        return craftEcoService;
    }

    public Path getConfigDir() {
        return configDir;
    }
}
