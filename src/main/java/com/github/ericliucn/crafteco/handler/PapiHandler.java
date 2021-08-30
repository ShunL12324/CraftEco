package com.github.ericliucn.crafteco.handler;

import com.github.ericliucn.crafteco.eco.CraftEcoService;
import com.github.ericliucn.crafteco.utils.Util;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.placeholder.PlaceholderParser;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;

public class PapiHandler {

    public static PlaceholderParser BALANCE = PlaceholderParser.builder()
            .parser(context -> {
                String arg = context.argumentString().orElse("");
                CraftEcoService service = CraftEcoService.instance;
                ServerPlayer serverPlayer = (ServerPlayer) context.associatedObject().get();
                Account account = service.findOrCreateAccount(serverPlayer.uniqueId()).get();
                Currency currency = service.getCurrency(arg).orElse(service.defaultCurrency());
                return Util.toComponent(account.balance(currency).toString());
            })
            .build();

}
