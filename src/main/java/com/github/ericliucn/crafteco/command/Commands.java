package com.github.ericliucn.crafteco.command;

import com.github.ericliucn.crafteco.Main;
import com.github.ericliucn.crafteco.config.ConfigLoader;
import com.github.ericliucn.crafteco.config.CraftEcoConfig;
import com.github.ericliucn.crafteco.eco.CraftCurrency;
import com.github.ericliucn.crafteco.eco.CraftEcoService;
import com.github.ericliucn.crafteco.eco.CraftResult;
import com.github.ericliucn.crafteco.handler.PapiHandler;
import com.github.ericliucn.crafteco.utils.Contexts;
import com.github.ericliucn.crafteco.utils.Util;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.User;
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
import org.spongepowered.api.service.pagination.PaginationList;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Commands {

    public Commands(final RegisterCommandEvent<Command.Parameterized> event){

        CraftEcoConfig.Messages messages = ConfigLoader.instance.getConfig().messages;
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
                    // try transfer
                    CraftResult.CraftTransferResult result  = (CraftResult.CraftTransferResult) sourceAcc
                            .transfer(targetAcc, currency, amount);

                    switch (result.result()){
                        case SUCCESS:
                            Component messagePayee = PapiHandler.message(
                                    messages.transfer_success_payee,
                                    result, source, Util.toPlain(currency.displayName()));
                            source.sendMessage(messagePayee);
                            Component messageReceiver = PapiHandler.message(
                                    messages.transfer_success_receiver,
                                    result, source, Util.toPlain(currency.displayName()));
                            try {
                                Sponge.server().userManager().load(uuid).get().flatMap(User::player).ifPresent(player -> {
                                    player.sendMessage(messageReceiver);
                                });
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                            break;
                        case ACCOUNT_NO_FUNDS:
                            Component messagePayeeNoFunds = PapiHandler.message(
                                    messages.transfer_failed_no_funds,
                                    result, source, Util.toPlain(currency.displayName()));
                            source.sendMessage(messagePayeeNoFunds);
                            break;
                        default:
                            Component messagePayeeFailed = PapiHandler.message(
                                    messages.transfer_failed,
                                    result, source, Util.toPlain(currency.displayName()));
                            source.sendMessage(messagePayeeFailed);
                    }

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
                    UniqueAccount acc = service.findOrCreateAccount(context.requireOne(userPara)).get();
                    Currency currency = context.one(currencyPara).orElse(service.defaultCurrency());
                    BigDecimal amount = context.requireOne(amountPara);
                    TransactionResult result = acc.deposit(currency, amount);
                    ServerPlayer player = null;
                    if (Sponge.server().player(context.requireOne(userPara)).isPresent()){
                        player = Sponge.server().player(context.requireOne(userPara)).get();
                    }
                    // message
                    if (result.result().equals(ResultType.SUCCESS)){
                        if (context.cause().root() instanceof Audience){
                            Audience audience = ((Audience) context.cause().root());
                            audience.sendMessage(
                                    PapiHandler.message(
                                            messages.deposit_success_depositor,
                                            result,
                                            player,
                                            Util.toPlain(currency.displayName())
                                    )
                            );
                        }
                        if (player != null){
                            player.sendMessage(
                                    PapiHandler.message(
                                            messages.deposit_success_receiver,
                                            result,
                                            player,
                                            Util.toPlain(currency.displayName())
                                    )
                            );
                        }
                    }else {
                        if (context.cause().root() instanceof Audience){
                            Audience audience = ((Audience) context.cause().root());
                            audience.sendMessage(
                                    PapiHandler.message(
                                            messages.deposit_success_depositor,
                                            result,
                                            player,
                                            Util.toPlain(currency.displayName())
                                    )
                            );
                        }
                    }
                    return CommandResult.success();
                })
                .terminal(true)
                .build();

        final Command.Parameterized bal = Command.builder()
                .addParameter(
                        // optional user para
                        Parameter.user().optional().key("user").build()
                )
                .executor(context -> {
                    CraftEcoService service = Main.instance.getCraftEcoService();
                    Optional<UUID> userUUID = context.one(userPara);
                    Object root = context.cause().root();
                    if (!userUUID.isPresent() && root instanceof SystemSubject){
                        ((Audience) root).sendMessage(
                                PapiHandler.message(messages.command_need_user, null, null, null)
                        );
                    }
                    if (!userUUID.isPresent() && root instanceof ServerPlayer){
                        UniqueAccount acc = service.findOrCreateAccount(((ServerPlayer) root).uniqueId()).get();
                        List<Component> components = new ArrayList<>();
                        for (Map.Entry<Currency, BigDecimal> entry : acc.balances().entrySet()) {
                            String s = "&6" + Util.toPlain(entry.getKey().displayName()) + "&1: &e" +
                                    Util.toPlain(entry.getKey().symbol()) + "&a"
                                    + entry.getValue().toString();
                            components.add(Util.toComponent(s));
                        }
                        PaginationList.builder()
                                .contents(components)
                                .title(Util.toComponent("&5Balance"))
                                .padding(Util.toComponent("&a="))
                                .sendTo(((ServerPlayer) root));
                    }
                    return CommandResult.success();
                })
                .build();

        event.register(Main.instance.getContainer(), pay, "pay");
        event.register(Main.instance.getContainer(), adminPay, "adminpay");
        event.register(Main.instance.getContainer(), bal, "bal", "balance");
    }


}
