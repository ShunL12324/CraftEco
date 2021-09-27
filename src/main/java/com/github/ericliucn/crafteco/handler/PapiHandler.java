package com.github.ericliucn.crafteco.handler;

import com.github.ericliucn.crafteco.eco.CraftEcoService;
import com.github.ericliucn.crafteco.eco.CraftResult;
import com.github.ericliucn.crafteco.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.placeholder.PlaceholderComponent;
import org.spongepowered.api.placeholder.PlaceholderContext;
import org.spongepowered.api.placeholder.PlaceholderParser;
import org.spongepowered.api.placeholder.PlaceholderParsers;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.TransferResult;
import org.spongepowered.api.util.Identifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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

    public static PlaceholderParser CUR_SYMBOL = PlaceholderParser.builder()
            .parser(context -> {
                if (context.associatedObject().isPresent() && context.associatedObject().get() instanceof CraftResult){
                    CraftResult result = ((CraftResult) context.associatedObject().get());
                    return result.currency().symbol();
                }
                return Component.empty();
            })
            .build();

    public static PlaceholderParser TRANSFER_TARGET = PlaceholderParser.builder()
            .parser(context -> {
                if (context.associatedObject().isPresent() && context.associatedObject().get() instanceof TransferResult){
                    TransferResult result = ((TransferResult) context.associatedObject().get());
                    try {
                        Optional<User> optionalUser = Sponge.server().userManager().load(result.accountTo().identifier()).get();
                        return Util.toComponent(optionalUser.get().name());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                return Component.empty();
            })
            .build();

    public static PlaceholderParser TRANSFER_SOURCE = PlaceholderParser.builder()
            .parser(context -> {
                if (context.associatedObject().isPresent() && context.associatedObject().get() instanceof TransferResult){
                    TransferResult result = ((TransferResult) context.associatedObject().get());
                    try {
                        Optional<User> optionalUser = Sponge.server().userManager().load(result.account().identifier()).get();
                        return Util.toComponent(optionalUser.get().name());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                return Component.empty();
            })
            .build();

    public static PlaceholderParser CUR_NAME = PlaceholderParser.builder()
            .parser(context -> {
                if (context.associatedObject().isPresent() && context.associatedObject().get() instanceof CraftResult){
                    CraftResult result = ((CraftResult) context.associatedObject().get());
                    return result.currency().displayName();
                }
                return Component.empty();
            })
            .build();

    public static PlaceholderParser AMOUNT = PlaceholderParser.builder()
            .parser(context -> {
                if (context.associatedObject().isPresent() && context.associatedObject().get() instanceof CraftResult){
                    CraftResult result = ((CraftResult) context.associatedObject().get());
                    return Util.toComponent(result.amount().toString());
                }
                return Component.empty();
            })
            .build();


    public static List<TextReplacementConfig> replaceConfigs(@Nullable Object resultObj,
                                                             @Nullable Object playerObj,
                                                             @Nullable String augment){
        List<TextReplacementConfig> list = new ArrayList<>();

        // result linked replace config
        PlaceholderContext resultContext = PlaceholderContext.builder()
                .associatedObject(resultObj)
                .argumentString(augment)
                .build();
        PlaceholderContext playerContext = PlaceholderContext.builder()
                .associatedObject(playerObj)
                .argumentString(augment)
                .build();
        TextReplacementConfig replaceCurSymbol =
                TextReplacementConfig.builder()
                        .match("{cur_symbol}")
                        .replacement(
                                PlaceholderComponent.builder()
                                        .context(resultContext)
                                        .parser(CUR_SYMBOL)
                                        .build()
                        )
                        .build();

        TextReplacementConfig replaceCurName =
                TextReplacementConfig.builder()
                        .match("{cur_name}")
                        .replacement(
                                PlaceholderComponent.builder()
                                        .context(resultContext)
                                        .parser(CUR_NAME)
                                        .build()
                        )
                        .build();

        TextReplacementConfig replaceAmount =
                TextReplacementConfig.builder()
                        .match("{amount}")
                        .replacement(
                                PlaceholderComponent.builder()
                                        .context(resultContext)
                                        .parser(AMOUNT)
                                        .build()
                        )
                        .build();

        TextReplacementConfig replaceTarget =
                TextReplacementConfig.builder()
                        .match("{transfer_target}")
                        .replacement(
                                PlaceholderComponent.builder()
                                        .context(resultContext)
                                        .parser(TRANSFER_TARGET)
                                        .build()
                        )
                        .build();

        TextReplacementConfig replaceSource =
                TextReplacementConfig.builder()
                        .match("{transfer_source}")
                        .replacement(
                                PlaceholderComponent.builder()
                                        .context(resultContext)
                                        .parser(TRANSFER_TARGET)
                                        .build()
                        )
                        .build();

        // player linked replace config

        TextReplacementConfig replaceBal =
                TextReplacementConfig.builder()
                        .match("{balance}")
                        .replacement(
                                PlaceholderComponent.builder()
                                        .context(playerContext)
                                        .parser(BALANCE)
                                        .build()
                        )
                        .build();

        TextReplacementConfig replacePlayerName =
                TextReplacementConfig.builder()
                        .match("{name}")
                        .replacement(
                                PlaceholderComponent.builder()
                                        .context(playerContext)
                                        .parser(PlaceholderParsers.NAME.get())
                                        .build()
                        )
                        .build();

        TextReplacementConfig replaceCurrentWorld =
                TextReplacementConfig.builder()
                        .match("{world}")
                        .replacement(
                                PlaceholderComponent.builder()
                                        .context(playerContext)
                                        .parser(PlaceholderParsers.CURRENT_WORLD.get())
                                        .build()
                        )
                        .build();

        list.add(replaceCurName);
        list.add(replaceCurSymbol);
        list.add(replaceAmount);
        list.add(replaceTarget);
        list.add(replaceSource);

        list.add(replaceBal);
        list.add(replacePlayerName);
        list.add(replaceCurrentWorld);
        return list;
    }

    public static Component message(Component text, @Nullable Object resultObj, @Nullable Object playerObj,
                                    @Nullable String augment){
        // if no augment string but with the resultObj we could know which currency is using.
        String aug;
        if (augment == null && resultObj instanceof CraftResult){
            CraftResult result = ((CraftResult) resultObj);
            aug = Util.toPlain(result.currency().displayName());
        }else {
            aug = augment;
        }

        // replace all placeholders
        Component component = text;
        for (TextReplacementConfig replaceConfig : replaceConfigs(resultObj, playerObj, aug)) {
            component = component.replaceText(replaceConfig);
        }
        return component;
    }

    public static Component message(String text, @Nullable Object resultObj, @Nullable Object playerObj,
                                    @Nullable String augment){
        return message(Util.toComponent(text), resultObj, playerObj, augment);
    }



}
