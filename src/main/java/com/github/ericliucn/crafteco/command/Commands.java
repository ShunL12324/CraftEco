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
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransferResult;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.configurate.ConfigurateException;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Commands {

    private static CraftEcoConfig.Messages messages;

    public Commands(final RegisterCommandEvent<Command.Parameterized> event){

        messages = ConfigLoader.instance.getConfig().messages;

        // The following commands are user commands
        // which means they are not sub commands of `eco`

        // pay command
        final Command.Parameterized pay = Command.builder()
                .permission("crafteco.command.pay")
                .addParameter(EcoParameters.ACCOUNT_PARA)
                .addParameter(EcoParameters.AMOUNT_PARA)
                .addParameter(EcoParameters.CURRENCY_PARA)
                .executor(context -> {
                    CraftEcoService service = CraftEcoService.instance;
                    ServerPlayer source = ((ServerPlayer) context.cause().root());
                    Account sourceAcc = service.findOrCreateAccount(source.uniqueId()).get();
                    Account targetAcc = context.requireOne(EcoParameters.ACCOUNT_PARA_KEY);
                    // we get the currency using, if no specific currency we are going use default currency
                    Currency currency = context.one(EcoParameters.CURRENCY_PARA_KEY).orElse(service.defaultCurrency());
                    // get amount
                    BigDecimal amount = context.requireOne(EcoParameters.AMOUNT_PARA_KEY);
                    // try transfer
                    TransferResult result  = sourceAcc.transfer(targetAcc, currency, amount);

                    switch (result.result()){
                        case SUCCESS:
                            Component messagePayee = PapiHandler.message(
                                    messages.transfer_success_payee,
                                    result, source, Util.toPlain(currency.displayName()));
                            source.sendMessage(messagePayee);
                            Component messageReceiver = PapiHandler.message(
                                    messages.transfer_success_receiver,
                                    result, source, Util.toPlain(currency.displayName()));
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
                .build();

        final Command.Parameterized adminPay = Command.builder()
                .permission("crafteco.command.adminpay")
                .addParameter(EcoParameters.ACCOUNT_PARA)
                .addParameter(EcoParameters.AMOUNT_PARA)
                .addParameter(EcoParameters.CURRENCY_PARA)
                .executor(context -> {
                    CraftEcoService service = Main.instance.getCraftEcoService();
                    Account acc = context.requireOne(EcoParameters.ACCOUNT_PARA_KEY);
                    Currency currency = context.one(EcoParameters.CURRENCY_PARA_KEY).orElse(service.defaultCurrency());
                    BigDecimal amount = context.requireOne(EcoParameters.AMOUNT_PARA_KEY);
                    TransactionResult result = acc.deposit(currency, amount);
                    Optional<ServerPlayer> player = Sponge.server().player(acc.identifier());
                    // message
                    if (result.result().equals(ResultType.SUCCESS)){
                        if (context.cause().root() instanceof Audience){
                            Audience audience = ((Audience) context.cause().root());
                            audience.sendMessage(
                                    PapiHandler.message(
                                            messages.deposit_success_depositor,
                                            result,
                                            player.orElse(null),
                                            Util.toPlain(currency.displayName())
                                    )
                            );
                        }
                        player.ifPresent(p -> p.sendMessage(
                                PapiHandler.message(
                                        messages.deposit_success_receiver,
                                        result,
                                        player.orElse(null),
                                        Util.toPlain(currency.displayName())
                                )
                        ));
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
                .permission("crafteco.command.balance")
                .addParameter(EcoParameters.ACCOUNT_PARA_OPTIONAL)
                .executor(context -> {
                    Object root = context.cause().root();
                    Account account = context.one(EcoParameters.ACCOUNT_PARA).orElse(null);
                    if (account == null && root instanceof SystemSubject){
                        ((Audience) root).sendMessage(
                                PapiHandler.message(messages.command_need_user, null, null, null)
                        );
                        return CommandResult.error(Util.toComponent("&4need specific account"));
                    }
                    if (account == null && root instanceof ServerPlayer){
                        account = CraftEcoService.instance.findOrCreateAccount(((ServerPlayer) root).uniqueId()).orElse(null);
                    }
                    if (account != null && root instanceof Audience) {
                        List<Component> components = new ArrayList<>();
                        for (Map.Entry<Currency, BigDecimal> entry : account.balances().entrySet()) {
                            String s = "&6" + Util.toPlain(entry.getKey().displayName()) + "&f: &e" +
                                    Util.toPlain(entry.getKey().symbol()) + " &4"
                                    + entry.getValue().toString();
                            components.add(Util.toComponent(s));
                        }
                        PaginationList.builder()
                                .contents(components)
                                .title(Util.toComponent("&5Balance"))
                                .padding(Util.toComponent("&a="))
                                .sendTo(((Audience) root));
                    }
                    return CommandResult.success();
                })
                .build();


        final Command.Parameterized ecoSet = Command.builder()
                .permission("crafteco.command.eco.set")
                .addParameter(EcoParameters.ACCOUNT_PARA)
                .addParameter(EcoParameters.AMOUNT_PARA)
                .addParameter(EcoParameters.CURRENCY_PARA)
                .executor(context -> {
                    CraftEcoService service = CraftEcoService.instance;
                    Account account = context.requireOne(EcoParameters.ACCOUNT_PARA_KEY);
                    BigDecimal amount = context.requireOne(EcoParameters.AMOUNT_PARA_KEY);
                    Currency currency = context.one(EcoParameters.CURRENCY_PARA_KEY).orElse(service.defaultCurrency());
                    TransactionResult result = account.setBalance(currency, amount);
                    if (context.cause().root() instanceof Audience){
                        Audience audience = ((Audience) context.cause().root());
                        if (result.result().equals(ResultType.SUCCESS)){
                            audience.sendMessage(
                                    PapiHandler.message(
                                            messages.eco_set_success,
                                            result,
                                            Sponge.server().player(account.identifier()).orElse(null),
                                            Util.toPlain(result.currency().displayName())
                                    )
                            );
                        }else {
                            audience.sendMessage(
                                    PapiHandler.message(
                                            messages.eco_set_failed,
                                            result,
                                            Sponge.server().player(account.identifier()).orElse(null),
                                            Util.toPlain(result.currency().displayName())
                                    )
                            );
                        }
                    }
                    return CommandResult.success();
                })
                .build();

        final Command.Parameterized ecoReload = Command.builder()
                .permission("crafteco.command.eco.reload")
                .executor(context -> {
                    try {
                        CraftEcoService.instance.saveCache();
                        ConfigLoader.instance.load();
                        messages = ConfigLoader.instance.getConfig().messages;
                        CraftEcoService.instance.loadCache();
                        context.cause().audience().sendMessage(Util.toComponent("&aReload success!"));
                    } catch (ConfigurateException e) {
                        e.printStackTrace();
                        context.cause().audience().sendMessage(Util.toComponent("&4Reload failed!"));
                    }
                    return CommandResult.success();
                })
                .build();

        final Command.Parameterized ecoBase = Command.builder()
                .addChild(ecoSet, "set")
                .addChild(ecoReload, "reload")
                .build();

        event.register(Main.instance.getContainer(), pay, "pay");
        event.register(Main.instance.getContainer(), adminPay, "adminpay");
        event.register(Main.instance.getContainer(), bal, "bal", "balance");
        event.register(Main.instance.getContainer(), ecoBase, "eco");
    }


}
