package com.github.ericliucn.crafteco.eco;

import org.jetbrains.annotations.NotNull;
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

    private CraftAccount account;
    private Currency currency;
    private BigDecimal amount;
    private Set<Context> contexts;
    private TransactionType type;
    private ResultType resultType;

    public static Builder DEPOSIT_SUCCESS(){
        return new Builder()
                .result(ResultType.SUCCESS)
                .type(TransactionTypes.DEPOSIT.get());
    }

    public static Builder DEPOSIT_FAIL(){
        return new Builder()
                .result(ResultType.FAILED)
                .type(TransactionTypes.DEPOSIT.get());
    }

    public static Builder WITHDRAW_SUCCESS(){
        return new Builder()
                .result(ResultType.SUCCESS)
                .type(TransactionTypes.DEPOSIT.get());
    }

    public static Builder WITHDRAW_FAIL(){
        return new Builder()
                .result(ResultType.FAILED)
                .type(TransactionTypes.DEPOSIT.get());
    }

    public static Builder builder(){
        return new Builder();
    }

    private CraftEcoResults(){

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

        private final CraftEcoResults results;

        public Builder(){
            results = new CraftEcoResults();
        }

        public Builder account(CraftAccount account){
            results.account = account;
            return this;
        }

        public Builder currency(Currency currency){
            results.currency = currency;
            return this;
        }

        public Builder amount(BigDecimal amount){
            results.amount = amount;
            return this;
        }

        public Builder contexts(Set<Context> contexts){
            results.contexts = contexts;
            return this;
        }

        public Builder result(ResultType resultType){
            results.resultType = resultType;
            return this;
        }

        public Builder type(TransactionType transactionType){
            results.type = transactionType;
            return this;
        }

        @Override
        public @NotNull CraftEcoResults build() {
            return results;
        }
    }


}
