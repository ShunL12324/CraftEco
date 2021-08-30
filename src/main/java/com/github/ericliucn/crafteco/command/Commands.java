package com.github.ericliucn.crafteco.command;

import com.github.ericliucn.crafteco.Main;
import com.github.ericliucn.crafteco.config.MessageLoader;
import com.github.ericliucn.crafteco.eco.CraftCurrency;
import com.github.ericliucn.crafteco.eco.CraftEcoService;
import com.github.ericliucn.crafteco.utils.Util;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransferResult;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class Commands {

    public Commands(final RegisterCommandEvent<Command.Parameterized> event){

        final Parameter.Value<UUID> userPara = Parameter.user().key("user").optional().build();

        final Parameter.Key<CraftCurrency> currencyKey = Parameter.key("currency", CraftCurrency.class);
        final Parameter.Value<CraftCurrency> currencyPara = Parameter.builder(CraftCurrency.class)
                .key(currencyKey)
                .optional()
                .addParser(new CraftCurrencyParser())
                .completer(new CraftCurrencyParser.CraftCurrencyCompleter())
                .build();

        final Parameter.Value<BigDecimal> amountPara = Parameter.bigDecimal().key("amount").build();

        final Command.Parameterized pay = Command.builder()
                .permission("crafteco.command.pay")
                .addParameter(userPara)
                .addParameter(amountPara)
                .addParameter(currencyPara)
                .executor(context -> {
                    CraftEcoService service = CraftEcoService.instance;
                    ServerPlayer source = ((ServerPlayer) context.cause().root());
                    UUID uuid = context.requireOne(userPara);
                    UniqueAccount sourceAcc = service.findOrCreateAccount(source.uniqueId()).get();
                    UniqueAccount targetAcc = service.findOrCreateAccount(uuid).get();
                    TransferResult transferResult = sourceAcc.transfer(targetAcc, context.one(currencyKey)
                            .orElse(((CraftCurrency) service.defaultCurrency())), context.requireOne(amountPara));
                    if (transferResult.result().equals(ResultType.ACCOUNT_NO_FUNDS)){
                        source.sendMessage(Util.toComponent("&4你没有足够的钱"));
                    }else if (transferResult.result().equals(ResultType.FAILED)){
                        source.sendMessage(Util.toComponent("&4交易失败"));
                    }else if (transferResult.result().equals(ResultType.SUCCESS)){
                        source.sendMessage(Util.toComponent("&a交易成功"));
                    }
                    return CommandResult.success();
                })
                .executionRequirements(commandCause -> (commandCause.root() instanceof ServerPlayer))
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
