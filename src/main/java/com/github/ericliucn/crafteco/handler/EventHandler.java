package com.github.ericliucn.crafteco.handler;

import com.github.ericliucn.crafteco.Main;
import com.github.ericliucn.crafteco.eco.CraftEcoService;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.AudienceMessageEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

public class EventHandler {

    public static EventHandler instance;

    public EventHandler(){
        instance = this;
        Sponge.eventManager().registerListeners(Main.instance.getContainer(), this);
    }

    @Listener
    public void onJoin(final ServerSideConnectionEvent.Join event){
        CraftEcoService.instance.findOrCreateAccount(event.player().uniqueId());
    }

    @Listener
    public void onMessage(final AudienceMessageEvent event, @First Audience audience){
        event.setMessage(PapiHandler.message(event.message(), audience));
    }


}
