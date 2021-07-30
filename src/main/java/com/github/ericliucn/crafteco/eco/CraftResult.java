package com.github.ericliucn.crafteco.eco;

import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.*;

import java.math.BigDecimal;
import java.util.Set;

public class CraftResult implements TransactionResult {

    private final CraftAccount account;
    private final CraftCurrency currency;
    private final BigDecimal amount;
    private final TransactionType type;
    private final ResultType resultType;
    @Nullable
    private final Set<Context> contexts;
    @Nullable
    private final Cause cause;

    public static Builder builder(){
        return Sponge.game().builderProvider().provide(Builder.class);
    }

    private CraftResult(CraftAccount account, CraftCurrency currency, BigDecimal amount, @Nullable Set<Context> contexts,
                        TransactionType transactionType, ResultType resultType, @Nullable Cause cause){
        this.account = account;
        this.currency = currency;
        this.amount = amount;
        this.contexts = contexts;
        this.type = transactionType;
        this.resultType = resultType;
        this.cause = cause;
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

    @Nullable
    @Override
    public Set<Context> contexts() {
        return this.contexts;
    }

    @Override
    public ResultType result() {
        return this.resultType;
    }

    @Nullable
    @Override
    public TransactionType type() {
        return this.type;
    }

    @Nullable
    public Cause cause(){
        return this.cause;
    }

    public Builder toBuilder(){
        Builder builder = new Builder();
        builder.account(this.account);
        builder.amount(this.amount);
        builder.currency(this.currency);
        builder.type(this.type);
        builder.result(this.resultType);
        builder.cause(this.cause);
        builder.contexts(this.contexts);
        return builder;
    }

    public CraftTransferResult toTransferResult(CraftAccount accountTo){
        return new CraftTransferResult(this, accountTo);
    }

    public static class Builder implements org.spongepowered.api.util.Builder<CraftResult, Builder>{

        private CraftAccount account;
        private CraftCurrency currency;
        private BigDecimal amount;
        private Set<Context> contexts;
        private TransactionType type;
        private ResultType resultType;
        private Cause cause;

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

        public Builder cause(final Cause cause){
            this.cause = cause;
            return this;
        }

        @Override
        public @NotNull CraftResult build() {
            return new CraftResult(this.account, this.currency, this.amount, this.contexts, this.type, this.resultType, this.cause);
        }
    }

    public static class CraftTransferResult extends CraftResult implements TransferResult {

        private final CraftAccount accountTo;

        private CraftTransferResult(CraftResult result, CraftAccount accountTo) {
            super(result.account, result.currency, result.amount, result.contexts, result.type, result.resultType, result.cause);
            this.accountTo = accountTo;
        }

        @Override
        public Account accountTo() {
            return this.accountTo;
        }

    }


}
