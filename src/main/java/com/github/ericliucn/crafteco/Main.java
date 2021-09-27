package com.github.ericliucn.crafteco;

import com.github.ericliucn.crafteco.command.Commands;
import com.github.ericliucn.crafteco.config.ConfigLoader;
import com.github.ericliucn.crafteco.eco.CraftCurrency;
import com.github.ericliucn.crafteco.eco.CraftEcoService;
import com.github.ericliucn.crafteco.eco.CraftResult;
import com.github.ericliucn.crafteco.handler.database.DBLoader;
import com.github.ericliucn.crafteco.handler.EventHandler;
import com.github.ericliucn.crafteco.handler.PapiHandler;
import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.lifecycle.*;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.placeholder.PlaceholderComponent;
import org.spongepowered.api.placeholder.PlaceholderContext;
import org.spongepowered.api.placeholder.PlaceholderParser;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

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
        new DBLoader(configDir);
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) throws IOException {
    }

    @Listener
    public void onServerStarted(final StartedEngineEvent<Server> event){
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

    @Listener
    public void onMessage(final MessageEvent event, @First ServerPlayer player){
        Component message = event.message().replaceText(TextReplacementConfig.builder()
                .replacement(PlaceholderComponent.builder()
                        .parser(PapiHandler.BALANCE)
                        .context(PlaceholderContext.builder()
                                .associatedObject(player)
                                .build())
                        .build().asComponent())
                .match("%balance%").build());
        for (CraftCurrency currency : ConfigLoader.instance.getConfig().currencies) {
            message = message.replaceText(TextReplacementConfig.builder()
                    .replacement(PlaceholderComponent.builder()
                            .parser(PapiHandler.BALANCE)
                            .context(PlaceholderContext.builder()
                                    .associatedObject(player)
                                    .argumentString(currency.toPlain())
                                    .build())
                            .build().asComponent())
                    .match("%balance_" + currency.toPlain() +"%").build());
        }
        event.setMessage(message);
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
