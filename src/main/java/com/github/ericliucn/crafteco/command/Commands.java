package com.github.ericliucn.crafteco.command;

import com.github.ericliucn.crafteco.Main;
import com.github.ericliucn.crafteco.config.MessageLoader;
import com.github.ericliucn.crafteco.eco.CraftCurrency;
import com.github.ericliucn.crafteco.eco.CraftEcoService;
import com.github.ericliucn.crafteco.handler.DBLoader;
import com.github.ericliucn.crafteco.utils.ComponentUtil;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.account.VirtualAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;

import java.math.BigDecimal;
import java.util.UUID;

public class Commands {

    static Parameter.Value<User> userPara = Parameter.builder(User.class)
            .key("user")
            .addParser(new UserParser())
            .completer(new UserParser.UserCompleter())
            .build();
    static Parameter.Value<CraftCurrency> currencyPara = Parameter.builder(CraftCurrency.class)
            .key("currency")
            .addParser(new CraftCurrencyParser())
            .completer(new CraftCurrencyParser.CraftCurrencyCompleter())
            .build();
    static Parameter.Value<BigDecimal> amountPara = Parameter.bigDecimal().key("amount").build();

    public static Command.Parameterized pay = Command.builder()
            .permission("crafteco.command.pay")
            .addParameter(userPara)
            .addParameter(amountPara)
            .addParameter(currencyPara)
            .executor(context -> {
                CraftEcoService service = Main.instance.getCraftEcoService();
                ServerPlayer source = ((ServerPlayer) context.cause().root());
                User target = context.requireOne(userPara);
                BigDecimal amount = context.requireOne(amountPara);
                CraftCurrency currency = context.one(currencyPara).orElse((CraftCurrency) service.defaultCurrency());
                UniqueAccount sourceAccount = service.findOrCreateAccount(source.uniqueId()).get();
                UniqueAccount targetAccount = service.findOrCreateAccount(target.uniqueId()).get();
                TransactionResult result = sourceAccount.transfer(targetAccount, currency, amount, context.contextCause());
                if (result.result() == ResultType.SUCCESS){
                    source.sendMessage(MessageLoader.instance.getMessage("transaction.success"));
                    target.player().ifPresent(player -> player.sendMessage(MessageLoader.instance.getMessage("transaction.success")));
                    return CommandResult.success();
                }else {
                    return CommandResult.error(MessageLoader.instance.getMessage("transaction.failed"));
                }
            })
            .executionRequirements(commandCause -> (commandCause.root() instanceof ServerPlayer))
            .build();

    public static Command.Parameterized test = Command.builder()
            .executor(context -> {
                try {
                    //create account
                    DBLoader.instance.getDbHandler().createAccount(UUID.randomUUID());
                }catch (Exception e){
                    e.printStackTrace();
                }
                return CommandResult.success();
            })
            .build();

}
