package com.github.ericliucn.crafteco.eco;

import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransactionType;

import java.math.BigDecimal;
import java.util.Set;

public class CraftEcoResults implements TransactionResult {

    private final CraftAccount account;
    private final Currency currency;
    private final BigDecimal amount;
    private final Set<Context> contexts;
    private final TransactionType type;
    private final ResultType resultType;

    public CraftEcoResults(CraftAccount account, Currency currency, BigDecimal amount, Set<Context> contexts, TransactionType type, ResultType resultType){
        this.account = account;
        this.currency = currency;
        this.contexts = contexts;
        this.amount = amount;
        this.type = type;
        this.resultType = resultType;
    }

    @Override
    public Account account() {
        return (Account) this.account;
    }

    @Override
    public Currency currency() {
        return this.currency;
    }

    @Override
    public BigDecimal amount() {
        return this.amount;
    }

    @Override
    public Set<Context> contexts() {
        return this.contexts;
    }

    @Override
    public ResultType result() {
        return this.resultType;
    }

    @Override
    public TransactionType type() {
        return this.type;
    }


}
