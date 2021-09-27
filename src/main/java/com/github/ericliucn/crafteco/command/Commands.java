package com.github.ericliucn.crafteco.command;

import com.github.ericliucn.crafteco.Main;
import com.github.ericliucn.crafteco.eco.CraftCurrency;
import com.github.ericliucn.crafteco.eco.CraftEcoService;
import com.github.ericliucn.crafteco.utils.Contexts;
import com.github.ericliucn.crafteco.utils.Util;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKey;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransferResult;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class Commands {

    public Commands(final RegisterCommandEvent<Command.Parameterized> event){


        // user parameter value
        final Parameter.Value<UUID> userPara = Parameter.user().key("user").optional().build();
        // currency parameter value
        final Parameter.Value<Currency> currencyPara = Parameter.builder(Currency.class)
                .key("currency")
                .optional()
                .addParser(new CraftCurrencyParser())
                .completer(new CraftCurrencyParser.CraftCurrencyCompleter())
                .build();
        // amount parameter value
        final Parameter.Value<BigDecimal> amountPara = Parameter.bigDecimal().key("amount").build();

        // The following commands are user commands
        // which means they are not sub commands of `eco`

        // pay command
        final Command.Parameterized pay = Command.builder()
                .permission("crafteco.command.pay")
                .addParameter(
                        // required user para
                        Parameter.user().build()
                )
                .addParameter(amountPara)
                .addParameter(currencyPara)
                .executor(context -> {
                    CraftEcoService service = CraftEcoService.instance;
                    ServerPlayer source = ((ServerPlayer) context.cause().root());
                    UUID uuid = context.requireOne(userPara);
                    // we get the source account and target account
                    UniqueAccount sourceAcc = service.findOrCreateAccount(source.uniqueId()).get();
                    UniqueAccount targetAcc = service.findOrCreateAccount(uuid).get();
                    // we get the currency using, if no specific currency we are going use default currency
                    Currency currency = context.one(currencyPara).orElse(service.defaultCurrency());
                    // get amount
                    BigDecimal amount = context.requireOne(amountPara);
                    // set cause
                    EventContext contexts = EventContext.builder()
                            .add(Contexts.TRANSFER_PAYEE, source.uniqueId())
                            .add(Contexts.TRANSFER_RECEIVER, uuid)
                            .build();
                    Cause cause = Cause.builder()
                            .append(source)
                            .build(contexts);
                    // try transfer
                    sourceAcc.transfer(targetAcc, currency, amount, cause);

                    return CommandResult.success();
                })
                .executionRequirements(
                        // the command cause root object must be a player not from console
                        commandCause -> (commandCause.root() instanceof ServerPlayer)
                )
                .build();

        final Command.Parameterized adminPay = Command.builder()
                .permission("crafteco.command.adminpay")
                .addParameter(userPara)
                .addParameter(amountPara)
                .addParameter(currencyPara)
                .executor(context -> {
                    CraftEcoService service = Main.instance.getCraftEcoService();
                    service.findOrCreateAccount(context.requireOne(userPara)).ifPresent(acc -> {
                        TransactionResult result = acc.deposit(context.requireOne(currencyPara), context.requireOne(amountPara));
                        if (result.result().equals(ResultType.SUCCESS)){
                            ((Audience) context.cause().root()).sendMessage(Util.toComponent("向账户添加了" + context.requireOne(amountPara).toString()));
                        }
                    });
                    return CommandResult.success();
                })
                .terminal(true)
                .build();

        final Command.Parameterized bal = Command.builder()
                .addParameter(userPara)
                .addParameter(currencyPara)
                .executor(context -> {
                    CraftEcoService service = Main.instance.getCraftEcoService();
                    CraftCurrency currency = context.one(currencyKey).orElse((CraftCurrency) service.defaultCurrency());
                    Optional<UUID> userUUID = context.one(userPara);
                    Object root = context.cause().root();
                    if (!userUUID.isPresent() && root instanceof SystemSubject){
                        ((Audience) root).sendMessage(message("command.error.nouser"));
                    }
                    if (!userUUID.isPresent() && root instanceof ServerPlayer){
                        service.findOrCreateAccount(((ServerPlayer) root).uniqueId()).ifPresent(acc -> ((ServerPlayer) root).sendMessage(message("")));
                    }
                    service.findOrCreateAccount(context.requireOne(userPara)).ifPresent(acc -> {
                        ((Audience) root).sendMessage(Util.toComponent(acc.balance(currency).toString()));
                    });
                    return CommandResult.success();
                })
                .build();

        event.register(Main.instance.getContainer(), pay, "pay");
        event.register(Main.instance.getContainer(), adminPay, "adminpay");
        event.register(Main.instance.getContainer(), bal, "bal", "balance");
    }

    private static Component message(String key){
        return MessageLoader.instance.getMessage(key);
    }


}
