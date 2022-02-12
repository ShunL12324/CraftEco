package com.github.ericliucn.crafteco.command;

import com.github.ericliucn.crafteco.config.ConfigLoader;
import com.github.ericliucn.crafteco.eco.CraftCurrency;
import com.github.ericliucn.crafteco.eco.CraftEcoService;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;

import java.math.BigDecimal;
import java.util.stream.Collectors;

public class EcoParameters {

    public static Parameter.Key<Account> ACCOUNT_PARA_KEY = Parameter.key("account", Account.class);
    public static Parameter.Value<Account> ACCOUNT_PARA = Parameter.builder(Account.class)
            .key(ACCOUNT_PARA_KEY)
            .completer((context, currentInput) -> {
                CraftEcoService service = CraftEcoService.instance;
                return service.identifiers().stream().map(CommandCompletion::of).collect(Collectors.toList());
            })
            .addParser((parameterKey, reader, context) -> {
                String identifier = reader.parseString();
                CraftEcoService service = CraftEcoService.instance;
                return service.searchAccountByIdentifier(identifier);
            })
            .build();
    public static Parameter.Value<Account> ACCOUNT_PARA_OPTIONAL = Parameter.builder(Account.class)
            .key(ACCOUNT_PARA_KEY)
            .completer((context, currentInput) -> {
                CraftEcoService service = CraftEcoService.instance;
                return service.identifiers().stream().map(CommandCompletion::of).collect(Collectors.toList());
            })
            .addParser((parameterKey, reader, context) -> {
                String identifier = reader.parseString();
                CraftEcoService service = CraftEcoService.instance;
                return service.searchAccountByIdentifier(identifier);
            })
            .optional()
            .build();

    public static final Parameter.Key<Currency> CURRENCY_PARA_KEY = Parameter.key("currency", Currency.class);
    public static Parameter.Value<Currency> CURRENCY_PARA = Parameter.builder(Currency.class)
            .key(CURRENCY_PARA_KEY)
            .completer((context, currentInput) -> ConfigLoader.instance.getConfig().currencies.stream()
                    .map(CraftCurrency::toPlain)
                    .map(CommandCompletion::of)
                    .collect(Collectors.toList()))
            .addParser((parameterKey, reader, context) -> {
                String name = reader.parseString();
                return CraftEcoService.instance.getCurrency(name);
            })
            .optional()
            .build();

    public static final Parameter.Key<BigDecimal> AMOUNT_PARA_KEY = Parameter.key("amount", BigDecimal.class);
    public static Parameter.Value<BigDecimal> AMOUNT_PARA = Parameter.bigDecimal()
            .key(AMOUNT_PARA_KEY)
            .build();

}
