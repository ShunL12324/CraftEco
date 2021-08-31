package com.github.ericliucn.crafteco.handler;

import com.github.ericliucn.crafteco.eco.CraftEcoService;
import com.github.ericliucn.crafteco.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.spongepowered.api.placeholder.PlaceholderContext;
import org.spongepowered.api.placeholder.PlaceholderParser;
import org.spongepowered.api.placeholder.PlaceholderParsers;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.util.Identifiable;

import java.util.UUID;

public class PapiHandler {

    public static PlaceholderParser BALANCE = PlaceholderParser.builder()
            .parser(context -> {
                if (context.associatedObject().isPresent() && context.associatedObject().get() instanceof Identifiable){
                    Identifiable identifiable = ((Identifiable) context.associatedObject().get());
                    UUID uuid = identifiable.uniqueId();
                    String arg = context.argumentString().orElse("");
                    CraftEcoService service = CraftEcoService.instance;
                    if (service.hasAccount(uuid)) {
                        Account account = service.findOrCreateAccount(uuid).get();
                        Currency currency = service.getCurrency(arg).orElse(service.defaultCurrency());
                        return Util.toComponent(account.balance(currency).toString());
                    }
                }
                return Component.empty();
            })
            .build();

    public static Component message(Component text, Object o){
        Component component = text;
        component = component.replaceText(TextReplacementConfig.builder()
                .match("{balance}")
                .replacement(BALANCE.parse(PlaceholderContext.builder()
                    .associatedObject(o)
                    .build()))
                .build());
        component = component.replaceText(TextReplacementConfig.builder()
                .match("{name}")
                .replacement(PlaceholderParsers.NAME.get().parse(PlaceholderContext.builder()
                        .associatedObject(o)
                        .build()))
                .build());
        component = component.replaceText(TextReplacementConfig.builder()
                .match("{world}")
                .replacement(PlaceholderParsers.CURRENT_WORLD.get().parse(PlaceholderContext.builder()
                        .associatedObject(o)
                        .build()))
                .build());
        return component;
    }



}
