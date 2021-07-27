package com.github.ericliucn.crafteco.eco;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransactionType;
import org.spongepowered.api.service.economy.transaction.TransactionTypes;

import java.math.BigDecimal;
import java.util.Set;

public class CraftEcoResults implements TransactionResult {

    private final CraftAccount account;
    private final Currency currency;
    private final BigDecimal amount;
    private final Set<Context> contexts;
    private final TransactionType type;
    private final ResultType resultType;

    public static Builder builder(){
        return Sponge.game().builderProvider().provide(Builder.class);
    }

    private CraftEcoResults(CraftAccount account, CraftCurrency currency, BigDecimal amount, Set<Context> contexts,
                            TransactionType transactionType, ResultType resultType){
        this.account = account;
        this.currency = currency;
        this.amount = amount;
        this.contexts = contexts;
        this.type = transactionType;
        this.resultType = resultType;
    }


    @Override
    public Account account() {
        return this.account;
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

    public static class Builder implements org.spongepowered.api.util.Builder<CraftEcoResults, Builder>{

        private CraftAccount account;
        private CraftCurrency currency;
        private BigDecimal amount;
        private Set<Context> contexts;
        private TransactionType type;
        private ResultType resultType;

        public Builder account(final CraftAccount account){
            this.account = account;
            return this;
        }

        public Builder currency(final CraftCurrency currency){
            this.currency = currency;
            return this;
        }

        public Builder amount(final BigDecimal amount){
            this.amount = amount;
            return this;
        }

        public Builder contexts(final Set<Context> contexts){
            this.contexts = contexts;
            return this;
        }

        public Builder result(final ResultType resultType){
            this.resultType = resultType;
            return this;
        }

        public Builder type(final TransactionType transactionType){
            this.type = transactionType;
            return this;
        }

        @Override
        public @NotNull CraftEcoResults build() {
            return new CraftEcoResults(this.account, this.currency, this.amount, this.contexts, this.type, this.resultType);
        }
    }


}
