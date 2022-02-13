package com.github.ericliucn.crafteco.handler;

import com.github.ericliucn.crafteco.Main;
import com.github.ericliucn.crafteco.eco.CraftEcoService;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.economy.EconomyTransactionEvent;
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
    public void onPlayerJoin(ServerSideConnectionEvent.Join event, @First ServerPlayer player){
        CraftEcoService.instance.findOrCreateAccount(player.uniqueId());
    }

}
